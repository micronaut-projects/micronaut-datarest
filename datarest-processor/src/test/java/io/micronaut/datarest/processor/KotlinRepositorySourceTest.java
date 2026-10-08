package io.micronaut.datarest.processor;

import io.micronaut.inject.ast.ClassElement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class KotlinRepositorySourceTest {

    @Test
    void rendersAGenericOverrideWithNullableReturnsAndPropertyAccess() {
        ClassElement repository = ClassElement.of("example.BookRepository");
        ClassElement entity = ClassElement.of("example.Book");
        ClassElement id = ClassElement.of(Long.class);
        EntityMetadata metadata = new EntityMetadata("books", "book_id", "id", "getId", id);

        String source = KotlinRepositorySource.render(repository, entity, id, metadata, "catalog", "BookRepository$Impl");

        assertTrue(source.contains("package example"), source);
        assertTrue(source.contains("class `BookRepository$Impl`(@Named(\"catalog\") private val repository: RestGenericRepository) : example.BookRepository"), source);
        assertTrue(source.contains("override fun findById(id: Long): example.Book? ="), source);
        assertTrue(source.contains("override fun <S : example.Book> save(entity: S): S ="), source);
        assertTrue(source.contains("override fun <S : example.Book> update(entity: S): S? = RestCrudRepositories.update(repository, TABLE, ID_COLUMN, entity.id, entity)"), source);
        assertTrue(source.contains("const val TABLE: String = \"books\""), source);
        assertTrue(source.contains("const val ID_COLUMN: String = \"book_id\""), source);
    }
}
