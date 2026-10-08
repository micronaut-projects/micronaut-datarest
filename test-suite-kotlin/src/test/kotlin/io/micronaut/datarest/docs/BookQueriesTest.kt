package io.micronaut.datarest.docs

import io.micronaut.datarest.testutils.PostgrestTestPropertyProvider
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookQueriesTest : PostgrestTestPropertyProvider {

    @Test
    fun filtersOrdersCountsAndDeletes(queries: BookQueries, repository: BookRepository) {
        val dune = repository.save(Book(null, "Dune", "Frank Herbert", LocalDate.of(1965, 8, 1)))
        val messiah = repository.save(Book(null, "Dune Messiah", "Frank Herbert", LocalDate.of(1969, 10, 1)))
        repository.save(Book(null, "Frankenstein", "Mary Shelley", LocalDate.of(1818, 1, 1)))

        val herbert = queries.byAuthor("Frank Herbert")
        assertEquals(2, herbert.numberOfElements)
        assertEquals(messiah.id, herbert.content[0].id)
        assertEquals(dune.id, herbert.content[1].id)
        assertEquals(2, queries.countByAuthor("Frank Herbert"))

        assertEquals(1, queries.deletePublishedBefore(LocalDate.of(1900, 1, 1)))
        assertEquals(1, repository.deleteById(dune.id!!))
        assertEquals(1, repository.deleteById(messiah.id!!))
    }
}
