package io.micronaut.datarest.core.annotations;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.data.annotation.Repository;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Documented
@Repository
public @interface RestRepository {
    /**
     * @return The datasource name.
     */
    @AliasFor(annotation = Repository.class, member = "value")
    String value() default "default";

    /**
     * @return The datasource name.
     */
    @AliasFor(annotation = Repository.class, member = "value")
    String dataSource() default "default";
}
