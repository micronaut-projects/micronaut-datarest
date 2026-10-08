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
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.annotation.Transient;
import io.micronaut.data.model.naming.NamingStrategies;
import io.micronaut.data.model.naming.NamingStrategy;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.MethodElement;
import io.micronaut.inject.ast.PropertyElement;
import io.micronaut.inject.processing.ProcessingException;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * What the generated repository needs to know about an entity: the table it maps to and how its identity is
 * named and read. Inferred the way Micronaut Data JDBC does it.
 *
 * @param table        persisted table name
 * @param idColumn     persisted identity column name
 * @param idProperty   name of the identity property
 * @param idReadMethod name of the entity method returning the identity, such as {@code id} or {@code getId}
 * @param idType       type of the identity property
 * @author Sergio del Amo
 * @since 1.0.0
 */
@Internal
record EntityMetadata(String table, String idColumn, String idProperty, String idReadMethod, ClassElement idType) {

    private static final Map<String, Supplier<NamingStrategy>> BUILT_IN_STRATEGIES = Map.of(
        NamingStrategies.UnderScoreSeparatedLowerCase.class.getName(), NamingStrategies.UnderScoreSeparatedLowerCase::new,
        NamingStrategies.UnderScoreSeparatedUpperCase.class.getName(), NamingStrategies.UnderScoreSeparatedUpperCase::new,
        NamingStrategies.KebabCase.class.getName(), NamingStrategies.KebabCase::new,
        NamingStrategies.LowerCase.class.getName(), NamingStrategies.LowerCase::new,
        NamingStrategies.UpperCase.class.getName(), NamingStrategies.UpperCase::new,
        NamingStrategies.Raw.class.getName(), NamingStrategies.Raw::new
    );

    /**
     * @param entity entity class
     * @return the metadata
     * @throws ProcessingException if the entity is not a usable mapped entity
     */
    static EntityMetadata of(ClassElement entity) {
        if (!entity.hasStereotype(MappedEntity.class)) {
            throw new ProcessingException(entity, "Entity [" + entity.getName() + "] must be annotated with @MappedEntity");
        }
        NamingStrategy strategy = namingStrategy(entity, entity.stringValue(io.micronaut.data.annotation.NamingStrategy.class).orElse(null));
        String table = tableName(entity.getSimpleName(), entity.stringValue(MappedEntity.class).orElse(null), strategy);
        List<PropertyElement> ids = entity.getBeanProperties().stream()
            .filter(property -> property.hasStereotype(Id.class) && !property.hasStereotype(Transient.class))
            .toList();
        if (ids.size() != 1) {
            throw new ProcessingException(entity, "Entity [" + entity.getName() + "] must declare exactly one @Id property, found " + ids.size());
        }
        PropertyElement id = ids.getFirst();
        String column = columnName(id.getName(), id.stringValue(MappedProperty.class).orElse(null), strategy);
        MethodElement readMethod = id.getReadMethod()
            .orElseThrow(() -> new ProcessingException(entity, "Identity property [" + id.getName() + "] of entity [" + entity.getName() + "] has no accessor"));
        return new EntityMetadata(table, column, id.getName(), readMethod.getName(), id.getType());
    }

    /**
     * @param simpleName entity class simple name
     * @param mapped     {@code @MappedEntity} value, possibly empty
     * @param strategy   naming strategy
     * @return the table name: the mapped value when given, otherwise the strategy applied to the simple name
     */
    static String tableName(String simpleName, @Nullable String mapped, NamingStrategy strategy) {
        return StringUtils.isNotEmpty(mapped) ? mapped : strategy.mappedName(simpleName);
    }

    /**
     * @param propertyName property name
     * @param mapped       {@code @MappedProperty} value, possibly empty
     * @param strategy     naming strategy
     * @return the column name: the mapped value when given, otherwise the strategy applied to the property name
     */
    static String columnName(String propertyName, @Nullable String mapped, NamingStrategy strategy) {
        return StringUtils.isNotEmpty(mapped) ? mapped : strategy.mappedName(propertyName);
    }

    /**
     * @param entity       entity class, for error reporting
     * @param strategyName naming strategy class name, possibly null
     * @return the built-in naming strategy of that name, or the Micronaut Data default when null
     * @throws ProcessingException for a strategy that is not one of {@link NamingStrategies}
     */
    static NamingStrategy namingStrategy(ClassElement entity, @Nullable String strategyName) {
        if (strategyName == null) {
            return NamingStrategy.DEFAULT;
        }
        Supplier<NamingStrategy> supplier = BUILT_IN_STRATEGIES.get(strategyName);
        if (supplier == null) {
            throw new ProcessingException(entity, "Naming strategy [" + strategyName + "] of entity [" + entity.getName()
                + "] is not supported at compilation time. Use one of " + NamingStrategies.class.getName() + " or set @MappedEntity.value");
        }
        return supplier.get();
    }
}
