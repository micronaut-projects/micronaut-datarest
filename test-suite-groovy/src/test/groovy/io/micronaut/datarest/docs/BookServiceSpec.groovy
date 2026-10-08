package io.micronaut.datarest.docs

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import io.micronaut.datarest.core.repositories.RestGenericRepository
import io.micronaut.datarest.testutils.PostgrestTestPropertyProvider
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class BookServiceSpec extends Specification implements PostgrestTestPropertyProvider {

    @Inject
    BookService service

    @Inject
    RestGenericRepository repository

    void "saves and pages"() {
        when:
        Book saved = service.save(new Book(title: "Neuromancer", author: "William Gibson"))

        then:
        saved.id != null

        when:
        Page<Book> page = service.findAll(Pageable.from(0, 1, Sort.of(Sort.Order.desc("id"))))

        then:
        page.numberOfElements == 1
        page.content.first().id == saved.id

        cleanup:
        repository.deleteById("books", "id", saved.id)
    }
}
