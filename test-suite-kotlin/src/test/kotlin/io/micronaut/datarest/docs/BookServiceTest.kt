package io.micronaut.datarest.docs

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import io.micronaut.datarest.testutils.PostgrestTestPropertyProvider
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookServiceTest : PostgrestTestPropertyProvider {

    @Test
    fun savesAndPages(service: BookService, repository: BookRepository) {
        val saved = service.save(Book(null, "Neuromancer", "William Gibson", null))
        val id = saved.id!!

        val page = service.findAll(Pageable.from(0, 1, Sort.of(Sort.Order.desc("id"))))
        assertEquals(1, page.numberOfElements)
        assertEquals(id, page.content.first().id)

        assertEquals(1, repository.deleteById(id))
    }
}
