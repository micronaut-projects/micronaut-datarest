# tag::class[]
from dataclasses import dataclass
from datetime import date
from typing import Annotated

from micronaut.data.annotation import Id, MappedEntity
from micronaut.serde.annotation import Serdeable


@Serdeable
@MappedEntity("books")  # <1>
@dataclass(frozen=True)
class Book:
    id: Annotated[int | None, Id] = None  # <2>
    title: str = ""
    author: str | None = None
    published: date | None = None
# end::class[]
