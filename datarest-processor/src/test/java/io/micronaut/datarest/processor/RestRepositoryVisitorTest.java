package io.micronaut.datarest.processor;

import io.micronaut.annotation.processing.test.JavaFileObjects;
import io.micronaut.annotation.processing.test.JavaParser;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestRepositoryVisitorTest {

    private static final String BOOK = """
        package test;

        import io.micronaut.data.annotation.Id;
        import io.micronaut.data.annotation.MappedEntity;

        @MappedEntity("books")
        public record Book(@Id Long id, String title) {
        }
        """;

    @Test
    void generatesAnImplementationAndItsBeanDefinition() {
        List<String> generated = generate(BOOK, """
            package test;

            import io.micronaut.datarest.core.annotations.RestRepository;
            import io.micronaut.datarest.core.repositories.RestCrudRepository;

            @RestRepository("catalog")
            public interface BookRepository extends RestCrudRepository<Book, Long> {
            }
            """);
        assertTrue(generated.stream().anyMatch(name -> name.endsWith("BookRepository$Impl.class")), generated.toString());
        assertTrue(generated.stream().anyMatch(name -> name.endsWith("BookRepository$Impl$Definition.class")), generated.toString());
    }

    @Test
    void failsWhenTheEntityHasNoIdentity() {
        RuntimeException e = assertThrows(RuntimeException.class, () -> generate("""
            package test;

            import io.micronaut.data.annotation.MappedEntity;

            @MappedEntity
            public record Book(Long id, String title) {
            }
            """, """
            package test;

            import io.micronaut.datarest.core.annotations.RestRepository;
            import io.micronaut.datarest.core.repositories.RestCrudRepository;

            @RestRepository
            public interface BookRepository extends RestCrudRepository<Book, Long> {
            }
            """));
        assertTrue(e.getMessage().contains("must declare exactly one @Id property"), e.getMessage());
    }

    @Test
    void failsWhenTheIdentityTypeDoesNotMatch() {
        RuntimeException e = assertThrows(RuntimeException.class, () -> generate(BOOK, """
            package test;

            import io.micronaut.datarest.core.annotations.RestRepository;
            import io.micronaut.datarest.core.repositories.RestCrudRepository;

            @RestRepository
            public interface BookRepository extends RestCrudRepository<Book, String> {
            }
            """));
        assertTrue(e.getMessage().contains("declares ID as [java.lang.String]"), e.getMessage());
    }

    @Test
    void failsForMethodsItCannotImplement() {
        RuntimeException e = assertThrows(RuntimeException.class, () -> generate(BOOK, """
            package test;

            import io.micronaut.datarest.core.annotations.RestRepository;
            import io.micronaut.datarest.core.repositories.RestCrudRepository;
            import java.util.List;

            @RestRepository
            public interface BookRepository extends RestCrudRepository<Book, Long> {
                List<Book> findByTitle(String title);
            }
            """));
        assertTrue(e.getMessage().contains("Method [findByTitle]"), e.getMessage());
    }

    private static List<String> generate(String entity, String repository) {
        try (JavaParser parser = new JavaParser()) {
            Iterable<? extends JavaFileObject> files = parser.generate(
                JavaFileObjects.forSourceString("test.Book", entity),
                JavaFileObjects.forSourceString("test.BookRepository", repository));
            List<String> names = new ArrayList<>();
            files.forEach(file -> names.add(file.getName()));
            return names;
        }
    }
}
