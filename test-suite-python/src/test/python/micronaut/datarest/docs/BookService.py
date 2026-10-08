# tag::class[]
from jakarta.inject import Singleton
from micronaut.data.model import Page, Pageable
from micronaut.datarest.core.repositories import RestGenericRepository

from .Book import Book


@Singleton
class BookService:

    def __init__(self, repository: RestGenericRepository):  # <1>
        self.repository = repository

    def save(self, book: Book) -> Book:
        return self.repository.save("books", book, Book)  # <2>

    def find_all(self, pageable: Pageable) -> Page[Book]:
        return self.repository.findAll("books", pageable, Book)
# end::class[]
