from datetime import date
from typing import Annotated

from jakarta.inject import Inject
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .Book import Book
from .BookQueries import BookQueries
from .BookRepository import BookRepository


@MicronautTest
class BookQueriesTest:

    queries: Annotated[BookQueries, Inject]
    repository: Annotated[BookRepository, Inject]

    @Test
    def test_filters_orders_counts_and_deletes(self):
        dune = self.repository.save(Book(title="Dune", author="Frank Herbert", published=date(1965, 8, 1)))
        messiah = self.repository.save(Book(title="Dune Messiah", author="Frank Herbert", published=date(1969, 10, 1)))
        self.repository.save(Book(title="Frankenstein", author="Mary Shelley", published=date(1818, 1, 1)))

        herbert = self.queries.by_author("Frank Herbert")
        assert herbert.getNumberOfElements() == 2
        assert herbert.getContent().get(0).id == messiah.id
        assert herbert.getContent().get(1).id == dune.id
        assert self.queries.count_by_author("Frank Herbert") == 2

        assert self.queries.delete_published_before(date(1900, 1, 1)) == 1
        assert self.repository.deleteById(dune.id) == 1
        assert self.repository.deleteById(messiah.id) == 1
