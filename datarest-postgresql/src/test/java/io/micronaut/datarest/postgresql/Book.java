package io.micronaut.datarest.postgresql;

import io.micronaut.serde.annotation.Serdeable;

import java.time.LocalDate;

@Serdeable
public record Book(
    Long id,
    String title,
    String author,
    LocalDate date
) {
}
