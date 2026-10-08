package io.micronaut.datarest.docs

// tag::class[]
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.serde.annotation.Serdeable
import java.time.LocalDate

@Serdeable
@MappedEntity("books") // <1>
data class Book(
    @field:Id val id: Long?, // <2>
    val title: String,
    val author: String?,
    val published: LocalDate?
)
// end::class[]
