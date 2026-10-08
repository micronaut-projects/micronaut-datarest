package io.micronaut.datarest.processor;

import io.micronaut.data.model.naming.NamingStrategies;
import io.micronaut.data.model.naming.NamingStrategy;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.processing.ProcessingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EntityMetadataTest {

    private static final ClassElement ENTITY = ClassElement.of(Object.class);

    @Test
    void mappedEntityValueWinsOverTheNamingStrategy() {
        assertEquals("books", EntityMetadata.tableName("Book", "books", NamingStrategy.DEFAULT));
    }

    @Test
    void defaultStrategyUnderscoresAndLowerCasesTheSimpleName() {
        assertEquals("book_author", EntityMetadata.tableName("BookAuthor", "", NamingStrategy.DEFAULT));
        assertEquals("book_author", EntityMetadata.tableName("BookAuthor", null, NamingStrategy.DEFAULT));
    }

    @Test
    void builtInStrategiesAreHonoured() {
        NamingStrategy raw = EntityMetadata.namingStrategy(ENTITY, NamingStrategies.Raw.class.getName());
        assertInstanceOf(NamingStrategies.Raw.class, raw);
        assertEquals("BookAuthor", EntityMetadata.tableName("BookAuthor", null, raw));
        assertInstanceOf(NamingStrategies.UnderScoreSeparatedLowerCase.class, EntityMetadata.namingStrategy(ENTITY, null));
    }

    @Test
    void customStrategiesAreRejected() {
        ProcessingException e = assertThrows(ProcessingException.class, () -> EntityMetadata.namingStrategy(ENTITY, "com.example.MyStrategy"));
        assertEquals(true, e.getMessage().contains("com.example.MyStrategy"), e.getMessage());
    }

    @Test
    void mappedPropertyValueWinsForColumns() {
        assertEquals("book_id", EntityMetadata.columnName("id", "book_id", NamingStrategy.DEFAULT));
        assertEquals("published_on", EntityMetadata.columnName("publishedOn", null, NamingStrategy.DEFAULT));
    }
}
