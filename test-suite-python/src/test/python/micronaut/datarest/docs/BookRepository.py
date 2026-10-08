# tag::class[]
from abc import ABC

from micronaut.datarest.core.annotations import RestRepository
from micronaut.datarest.core.repositories import RestCrudRepository

from .Book import Book


@RestRepository  # <1>
class BookRepository(RestCrudRepository[Book, int], ABC):  # <2>
    pass
# end::class[]
