package io.micronaut.datarest.docs

// tag::class[]
import io.micronaut.context.annotation.Property
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.annotation.ClientFilter
import io.micronaut.http.annotation.RequestFilter

@ClientFilter(serviceId = ["restdatasource-default"]) // <1>
class BearerTokenFilter(@Property(name = "books.token") private val token: String) { // <2>

    @RequestFilter
    fun bearer(request: MutableHttpRequest<*>) {
        request.bearerAuth(token) // <3>
    }
}
// end::class[]
