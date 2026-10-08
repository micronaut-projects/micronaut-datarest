package io.micronaut.datarest.postgresql;

import io.micronaut.serde.annotation.Serdeable;
import org.junit.jupiter.params.shadow.de.siegmar.fastcsv.util.Nullable;

import java.time.LocalDate;

@Serdeable
public record BookSave(
    String title,
    @Nullable String author,
    @Nullable LocalDate date
) {
}
