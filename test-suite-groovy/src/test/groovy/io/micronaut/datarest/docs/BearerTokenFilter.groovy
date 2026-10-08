package io.micronaut.datarest.docs

// tag::class[]
import io.micronaut.context.annotation.Property
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.annotation.ClientFilter
import io.micronaut.http.annotation.RequestFilter

@ClientFilter(serviceId = "restdatasource-default") // <1>
class BearerTokenFilter {

    private final String token

    BearerTokenFilter(@Property(name = "books.token") String token) { // <2>
        this.token = token
    }

    @RequestFilter
    void bearer(MutableHttpRequest<?> request) {
        request.bearerAuth(token) // <3>
    }
}
// end::class[]
