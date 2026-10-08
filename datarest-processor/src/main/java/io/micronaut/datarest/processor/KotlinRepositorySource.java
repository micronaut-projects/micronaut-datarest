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
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.processing.ProcessingException;
import io.micronaut.inject.visitor.VisitorContext;

import java.io.IOException;
import java.util.Map;

/**
 * Renders the Kotlin implementation of a {@code @RestRepository} interface. Kotlin requires an overriding function to
 * declare the type parameters of the overridden one, and the SourceGen Kotlin writer does not emit function type
 * parameters, so this implementation is rendered from a template instead of a {@code ClassDef}.
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
@Internal
final class KotlinRepositorySource {

    private static final Map<String, String> KOTLIN_TYPES = Map.of(
        "java.lang.Long", "Long",
        "java.lang.Integer", "Int",
        "java.lang.Short", "Short",
        "java.lang.Byte", "Byte",
        "java.lang.Double", "Double",
        "java.lang.Float", "Float",
        "java.lang.Boolean", "Boolean",
        "java.lang.Character", "Char",
        "java.lang.String", "String",
        "java.lang.Object", "Any"
    );

    private KotlinRepositorySource() {
    }

    static void write(ClassElement element, ClassElement entity, ClassElement idType, EntityMetadata metadata, String dataSource, VisitorContext context) {
        String simpleName = element.getSimpleName() + RestRepositoryVisitor.SUFFIX;
        String source = render(element, entity, idType, metadata, dataSource, simpleName);
        context.visitGeneratedSourceFile(element.getPackageName(), simpleName, element)
            .ifPresent(file -> {
                try {
                    file.write(writer -> writer.write(source));
                } catch (IOException e) {
                    throw new ProcessingException(element, "Could not write " + simpleName + ": " + e.getMessage(), e);
                }
            });
    }

    static String render(ClassElement element, ClassElement entity, ClassElement idType, EntityMetadata metadata, String dataSource, String simpleName) {
        String repository = kotlinName(element);
        String e = kotlinName(entity);
        String id = kotlinName(idType);
        return """
            package %s

            import io.micronaut.core.annotation.Internal
            import io.micronaut.data.model.Page
            import io.micronaut.data.model.Pageable
            import io.micronaut.data.model.Sort
            import io.micronaut.datarest.core.repositories.RestCrudRepositories
            import io.micronaut.datarest.core.repositories.RestGenericRepository
            import jakarta.inject.Named
            import jakarta.inject.Singleton

            @Singleton
            @Internal
            class `%s`(@Named("%s") private val repository: RestGenericRepository) : %s {

                override fun findById(id: %s): %s? = repository.findById(TABLE, ID_COLUMN, id, %s::class.java)

                override fun <S : %s> save(entity: S): S = RestCrudRepositories.save(repository, TABLE, entity)

                override fun <S : %s> update(entity: S): S? = RestCrudRepositories.update(repository, TABLE, ID_COLUMN, entity.%s, entity)

                override fun findAll(): List<%s> = repository.findAll(TABLE, %s::class.java).content

                override fun findAll(sort: Sort): List<%s> = repository.findAll(TABLE, sort, %s::class.java).content

                override fun findAll(pageable: Pageable): Page<%s> = repository.findAll(TABLE, pageable, %s::class.java)

                override fun existsById(id: %s): Boolean = repository.existsById(TABLE, ID_COLUMN, id)

                override fun count(): Long = repository.count(TABLE)

                override fun deleteById(id: %s): Int = repository.deleteById(TABLE, ID_COLUMN, id)

                private companion object {
                    const val TABLE: String = "%s"
                    const val ID_COLUMN: String = "%s"
                }
            }
            """.formatted(
            element.getPackageName(),
            simpleName, escape(dataSource), repository,
            id, e, e,
            e,
            e, metadata.idProperty(),
            e, e,
            e, e,
            e, e,
            id,
            id,
            escape(metadata.table()), escape(metadata.idColumn()));
    }

    private static String kotlinName(ClassElement type) {
        String name = type.getName();
        String kotlin = KOTLIN_TYPES.get(name);
        return kotlin != null ? kotlin : name.replace('$', '.');
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("$", "\\$");
    }
}
