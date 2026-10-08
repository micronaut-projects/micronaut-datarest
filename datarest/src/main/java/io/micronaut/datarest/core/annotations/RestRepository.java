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
package io.micronaut.datarest.core.annotations;

import io.micronaut.context.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an interface extending {@link io.micronaut.datarest.core.repositories.RestCrudRepository} for compile-time
 * implementation. The {@code micronaut-datarest-processor} annotation processor generates a singleton bean that
 * implements every method against the table mapped by the entity, through the
 * {@link io.micronaut.datarest.core.repositories.RestGenericRepository} of the named REST data source.
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface RestRepository {

    /**
     * @return the name of the REST data source, as configured under {@code restdatasources.<name>}
     */
    String value() default "default";

    /**
     * @return the name of the REST data source, an alias for {@link #value()}
     */
    @AliasFor(member = "value")
    String dataSource() default "default";
}
