# tag::class[]
from typing import Annotated

from jakarta.inject import Singleton
from micronaut.context.annotation import Property
from micronaut.http import MutableHttpRequest
from micronaut.http.annotation import ClientFilter, RequestFilter


@Singleton
@ClientFilter(serviceId="restdatasource-default")  # <1>
class BearerTokenFilter:

    def __init__(self, token: Annotated[str, Property(name="books.token")]):  # <2>
        self.token = token

    @RequestFilter
    def bearer(self, request: MutableHttpRequest) -> None:
        request.bearerAuth(self.token)  # <3>
# end::class[]
