from typing import Annotated

from jakarta.inject import Inject
from micronaut.data.model import Pageable, Sort
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .Book import Book
from .BookRepository import BookRepository
from .BookService import BookService


@MicronautTest
class BookServiceTest:

    service: Annotated[BookService, Inject]
    repository: Annotated[BookRepository, Inject]

    @Test
    def test_saves_and_pages(self):
        saved = self.service.save(Book(title="Neuromancer", author="William Gibson"))
        assert saved.id is not None

        page = self.service.find_all(Pageable.from_(0, 1, Sort.of(Sort.Order.desc("id"))))
        assert page.getNumberOfElements() == 1
        assert page.getContent().getFirst().id == saved.id

        assert self.repository.deleteById(saved.id) == 1
