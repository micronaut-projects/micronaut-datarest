package io.micronaut.datarest.docs

// tag::class[]
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.serde.annotation.Serdeable
import org.jspecify.annotations.Nullable

import java.time.LocalDate

@Serdeable
@MappedEntity("books") // <1>
class Book {
    @Id @Nullable Long id // <2>
    String title
    @Nullable String author
    @Nullable LocalDate published
}
// end::class[]
