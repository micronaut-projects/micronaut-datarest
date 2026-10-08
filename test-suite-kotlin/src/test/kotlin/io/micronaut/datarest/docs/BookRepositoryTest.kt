package io.micronaut.datarest.docs

import io.micronaut.data.model.Sort
import io.micronaut.datarest.testutils.PostgrestTestPropertyProvider
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookRepositoryTest : PostgrestTestPropertyProvider {

    @Test
    fun crud(repository: BookRepository) {
        val saved = repository.save(Book(null, "Dune", "Frank Herbert", LocalDate.of(1965, 8, 1)))
        val id = saved.id!!

        val found = repository.findById(id)
        assertNotNull(found)
        assertEquals("Dune", found!!.title)
        assertTrue(repository.existsById(id))

        val updated = repository.update(Book(id, "Dune Messiah", saved.author, saved.published))
        assertNotNull(updated)
        assertEquals("Dune Messiah", updated!!.title)

        assertTrue(repository.findAll(Sort.of(Sort.Order.desc("id"))).any { it.id == id })
        assertTrue(repository.findAll().any { it.id == id })
        assertTrue(repository.count() >= 1)

        assertEquals(1, repository.deleteById(id))
        assertFalse(repository.existsById(id))
    }
}
