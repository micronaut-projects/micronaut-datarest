from datetime import date

from jakarta.inject import Singleton
from micronaut.data.model import Page, Sort
from micronaut.datarest.core.repositories import RestGenericRepository
from micronaut.datarest.core.repositories.postgrest import PostgrestQuery

from .Book import Book


@Singleton
class BookQueries:

    def __init__(self, repository: RestGenericRepository):
        self.repository = repository

    # tag::queries[]
    def by_author(self, author: str) -> Page[Book]:
        query = (PostgrestQuery.filter("author", "eq." + author)  # <1>
                 .withOrder(Sort.of(Sort.Order.desc("published")))  # <2>
                 .withPage(10, 0)
                 .withSelect("id,title,author,published"))  # <3>
        return self.repository.findAll("books", query, Book)

    def count_by_author(self, author: str) -> int:
        return self.repository.count("books", PostgrestQuery.filter("author", "eq." + author))

    def delete_published_before(self, day: date) -> int:
        return self.repository.deleteAll("books", PostgrestQuery.filter("published", "lt." + day.isoformat()))  # <4>
    # end::queries[]
