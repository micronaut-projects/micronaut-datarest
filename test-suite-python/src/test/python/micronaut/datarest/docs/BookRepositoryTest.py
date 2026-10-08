from datetime import date
from typing import Annotated

from jakarta.inject import Inject
from micronaut.data.model import Sort
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .Book import Book
from .BookRepository import BookRepository


@MicronautTest
class BookRepositoryTest:

    repository: Annotated[BookRepository, Inject]

    @Test
    def test_crud(self):
        saved = self.repository.save(Book(title="Dune", author="Frank Herbert", published=date(1965, 8, 1)))
        book_id = saved.id
        assert book_id is not None

        assert self.repository.findById(book_id).title == "Dune"
        assert self.repository.existsById(book_id)

        updated = self.repository.update(Book(id=book_id, title="Dune Messiah", author=saved.author, published=saved.published))
        assert updated.title == "Dune Messiah"

        assert any(book.id == book_id for book in self.repository.findAll(Sort.of(Sort.Order.desc("id"))))
        assert any(book.id == book_id for book in self.repository.findAll())
        assert self.repository.count() >= 1

        assert self.repository.deleteById(book_id) == 1
        assert not self.repository.existsById(book_id)
