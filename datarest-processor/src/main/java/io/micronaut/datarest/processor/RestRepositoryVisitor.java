/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.datarest.processor;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.datarest.core.annotations.RestRepository;
import io.micronaut.datarest.core.repositories.RestCrudRepositories;
import io.micronaut.datarest.core.repositories.RestCrudRepository;
import io.micronaut.datarest.core.repositories.RestGenericRepository;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.ElementQuery;
import io.micronaut.inject.ast.MethodElement;
import io.micronaut.inject.ast.ParameterElement;
import io.micronaut.inject.processing.ProcessingException;
import io.micronaut.inject.visitor.TypeElementVisitor;
import io.micronaut.inject.visitor.VisitorContext;
import io.micronaut.sourcegen.generator.SourceGenerator;
import io.micronaut.sourcegen.generator.SourceGenerators;
import io.micronaut.sourcegen.model.AnnotationDef;
import io.micronaut.sourcegen.model.ClassDef;
import io.micronaut.sourcegen.model.ClassTypeDef;
import io.micronaut.sourcegen.model.ExpressionDef;
import io.micronaut.sourcegen.model.FieldDef;
import io.micronaut.sourcegen.model.MethodDef;
import io.micronaut.sourcegen.model.ParameterDef;
import io.micronaut.sourcegen.model.TypeDef;
import io.micronaut.sourcegen.model.VariableDef;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates, for every interface annotated with {@link RestRepository}, a singleton implementation that delegates
 * each {@link RestCrudRepository} operation to the {@link RestGenericRepository} of the configured data source,
 * using the table and identity column inferred from the entity.
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
@Internal
public final class RestRepositoryVisitor implements TypeElementVisitor<RestRepository, Object> {

    /**
     * Suffix appended to the interface name to form the generated class name.
     */
    public static final String SUFFIX = "$Impl";

    private static final String FIELD_REPOSITORY = "repository";
    private static final String FIELD_TABLE = "TABLE";
    private static final String FIELD_ID_COLUMN = "ID_COLUMN";
    private static final String ENTITY_VARIABLE = "E";
    private static final String ID_VARIABLE = "ID";
    private static final ClassTypeDef GENERIC_REPOSITORY = ClassTypeDef.of(RestGenericRepository.class);
    private static final ClassTypeDef HELPERS = ClassTypeDef.of(RestCrudRepositories.class);

    @Override
    public VisitorKind getVisitorKind() {
        return VisitorKind.ISOLATING;
    }

    @Override
    public Set<String> getSupportedAnnotationNames() {
        return Set.of(RestRepository.class.getName());
    }

    @Override
    public void visitClass(ClassElement element, VisitorContext context) {
        if (!element.hasAnnotation(RestRepository.class)) {
            return;
        }
        if (!element.isInterface()) {
            throw new ProcessingException(element, "@RestRepository can only be applied to interfaces");
        }
        Map<String, ClassElement> typeArguments = element.getTypeArguments(RestCrudRepository.class.getName());
        ClassElement entity = typeArguments.get(ENTITY_VARIABLE);
        ClassElement idType = typeArguments.get(ID_VARIABLE);
        if (entity == null || idType == null) {
            throw new ProcessingException(element, "@RestRepository interface [" + element.getName() + "] must extend "
                + RestCrudRepository.class.getName() + "<E, ID>");
        }
        EntityMetadata metadata = EntityMetadata.of(entity);
        if (!isAssignable(metadata.idType(), idType, context)) {
            throw new ProcessingException(element, "Identity property of entity [" + entity.getName() + "] has type ["
                + metadata.idType().getName() + "] but the repository declares ID as [" + idType.getName() + "]");
        }
        String dataSource = element.stringValue(RestRepository.class).orElse("default");
        ClassDef classDef = generate(element, entity, idType, metadata, dataSource);
        SourceGenerator sourceGenerator = SourceGenerators.findByLanguage(context.getLanguage()).orElse(null);
        if (sourceGenerator == null) {
            return;
        }
        try {
            sourceGenerator.write(classDef, context, element);
        } catch (ProcessingException e) {
            throw e;
        } catch (Exception e) {
            SourceGenerators.handleFatalException(element, RestRepository.class, e, ex -> {
                throw ex;
            });
        }
    }

    private static ClassDef generate(ClassElement element, ClassElement entity, ClassElement idType, EntityMetadata metadata, String dataSource) {
        String implName = element.getName() + SUFFIX;
        ClassTypeDef implType = ClassTypeDef.of(implName);
        ClassTypeDef entityType = ClassTypeDef.of(entity);
        TypeDef idTypeDef = TypeDef.of(idType);
        ExpressionDef entityClass = ExpressionDef.constant(entityType);
        FieldDef table = constant(FIELD_TABLE, metadata.table());
        FieldDef idColumn = constant(FIELD_ID_COLUMN, metadata.idColumn());
        FieldDef repository = FieldDef.builder(FIELD_REPOSITORY, GENERIC_REPOSITORY)
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .build();
        ParameterDef repositoryParameter = ParameterDef.builder(FIELD_REPOSITORY, GENERIC_REPOSITORY)
            .addAnnotation(AnnotationDef.builder(Named.class).addMember("value", dataSource).build())
            .build();
        ClassDef.ClassDefBuilder builder = ClassDef.builder(implName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addAnnotation(Singleton.class)
            .addAnnotation(Internal.class)
            .addSuperinterface(ClassTypeDef.of(element))
            .addField(table)
            .addField(idColumn)
            .addField(repository)
            .addConstructor(List.of(repositoryParameter), Modifier.PUBLIC);

        VariableDef.StaticField tableRef = implType.getStaticField(table);
        VariableDef.StaticField idColumnRef = implType.getStaticField(idColumn);
        TypeDef pageOfEntity = TypeDef.parameterized(ClassTypeDef.of(Page.class), entityType);
        TypeDef listOfEntity = TypeDef.parameterized(ClassTypeDef.of(List.class), entityType);

        for (MethodElement method : element.getEnclosedElements(ElementQuery.ALL_METHODS.onlyAbstract())) {
            ParameterElement[] parameters = method.getParameters();
            String name = method.getName();
            MethodDef methodDef = switch (name + "/" + parameters.length) {
                case "findById/1" -> MethodDef.overrideGeneric(method).build((self, params) ->
                    self.field(repository).invoke("findById", entityType, tableRef, idColumnRef, params.getFirst(), entityClass).returning());
                case "existsById/1" -> MethodDef.overrideGeneric(method).build((self, params) ->
                    self.field(repository).invoke("existsById", TypeDef.Primitive.BOOLEAN, tableRef, idColumnRef, params.getFirst()).returning());
                case "deleteById/1" -> MethodDef.overrideGeneric(method).build((self, params) ->
                    self.field(repository).invoke("deleteById", TypeDef.Primitive.INT, tableRef, idColumnRef, params.getFirst()).returning());
                case "count/0" -> MethodDef.overrideGeneric(method).build((self, params) ->
                    self.field(repository).invoke("count", TypeDef.Primitive.LONG, tableRef).returning());
                case "findAll/0" -> MethodDef.overrideGeneric(method).build((self, params) ->
                    self.field(repository).invoke("findAll", pageOfEntity, tableRef, entityClass).invoke("getContent", listOfEntity).returning());
                case "findAll/1" -> findAll(method, parameters[0], repository, tableRef, entityClass, pageOfEntity, listOfEntity);
                case "save/1" -> entityMethod("save", entityType, (self, params) ->
                    HELPERS.invokeStatic("save", params.getFirst().type(), self.field(repository), tableRef, params.getFirst()).returning());
                case "update/1" -> entityMethod("update", entityType, (self, params) ->
                    HELPERS.invokeStatic("update", params.getFirst().type(), self.field(repository), tableRef, idColumnRef,
                        params.getFirst().invoke(metadata.idReadMethod(), idTypeDef), params.getFirst()).returning());
                default -> throw new ProcessingException(method, "Method [" + name + "] of [" + element.getName()
                    + "] is not supported. Only the methods of " + RestCrudRepository.class.getName() + " are implemented");
            };
            builder.addMethod(methodDef);
        }
        return builder.build();
    }

    private static MethodDef findAll(MethodElement method, ParameterElement parameter, FieldDef repository, ExpressionDef tableRef,
                                     ExpressionDef entityClass, TypeDef pageOfEntity, TypeDef listOfEntity) {
        ClassElement argument = parameter.getGenericType();
        if (argument.isAssignable(Pageable.class)) {
            return MethodDef.overrideGeneric(method).build((self, params) ->
                self.field(repository).invoke("findAll", pageOfEntity, tableRef, params.getFirst(), entityClass).returning());
        }
        if (argument.isAssignable(Sort.class)) {
            return MethodDef.overrideGeneric(method).build((self, params) ->
                self.field(repository).invoke("findAll", pageOfEntity, tableRef, params.getFirst(), entityClass).invoke("getContent", listOfEntity).returning());
        }
        throw new ProcessingException(method, "Method [findAll] with a parameter of type [" + argument.getName() + "] is not supported");
    }

    /**
     * Builds {@code <S extends E> S name(S entity)}. The type variable is declared explicitly because a
     * {@link MethodDef#overrideGeneric(MethodElement)} erases it to its bound.
     */
    private static MethodDef entityMethod(String name, ClassTypeDef entityType, MethodDef.MethodBodyBuilder body) {
        TypeDef.TypeVariable s = TypeDef.variable("S", entityType);
        return MethodDef.builder(name)
            .addModifiers(Modifier.PUBLIC)
            .overrides()
            .addTypeVariable(s)
            .addParameter("entity", s)
            .returns(s)
            .build(body);
    }

    private static FieldDef constant(String name, String value) {
        return FieldDef.builder(name, String.class)
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer(ExpressionDef.constant(value))
            .build();
    }

    private static boolean isAssignable(ClassElement propertyType, ClassElement idType, VisitorContext context) {
        if (propertyType.isPrimitive()) {
            String boxed = switch (propertyType.getName()) {
                case "long" -> Long.class.getName();
                case "int" -> Integer.class.getName();
                case "short" -> Short.class.getName();
                case "byte" -> Byte.class.getName();
                case "char" -> Character.class.getName();
                case "boolean" -> Boolean.class.getName();
                case "double" -> Double.class.getName();
                case "float" -> Float.class.getName();
                default -> propertyType.getName();
            };
            return context.getClassElement(boxed).map(element -> element.isAssignable(idType)).orElse(false);
        }
        return propertyType.isAssignable(idType);
    }
}
