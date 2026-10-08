package io.micronaut.datarest.docs

import io.micronaut.data.model.Page
import io.micronaut.datarest.core.repositories.RestGenericRepository
import io.micronaut.datarest.testutils.PostgrestTestPropertyProvider
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

import java.time.LocalDate

@MicronautTest
class BookQueriesSpec extends Specification implements PostgrestTestPropertyProvider {

    @Inject
    BookQueries queries

    @Inject
    RestGenericRepository repository

    void "filters, orders, counts and deletes"() {
        given:
        Book dune = repository.save("books", new Book(title: "Dune", author: "Frank Herbert", published: LocalDate.of(1965, 8, 1)), Book)
        Book messiah = repository.save("books", new Book(title: "Dune Messiah", author: "Frank Herbert", published: LocalDate.of(1969, 10, 1)), Book)
        repository.save("books", new Book(title: "Frankenstein", author: "Mary Shelley", published: LocalDate.of(1818, 1, 1)), Book)

        when:
        Page<Book> herbert = queries.byAuthor("Frank Herbert")

        then:
        herbert.numberOfElements == 2
        herbert.content[0].id == messiah.id
        herbert.content[1].id == dune.id
        queries.countByAuthor("Frank Herbert") == 2
        queries.deletePublishedBefore(LocalDate.of(1900, 1, 1)) == 1

        cleanup:
        repository.deleteById("books", "id", dune.id)
        repository.deleteById("books", "id", messiah.id)
    }
}
