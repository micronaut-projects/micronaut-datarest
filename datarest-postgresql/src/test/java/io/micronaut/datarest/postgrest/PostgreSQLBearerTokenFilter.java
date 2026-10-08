package io.micronaut.datarest.postgrest;

import io.micronaut.datarest.core.clients.RestDataSourceClient;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.ClientFilter;
import io.micronaut.http.annotation.RequestFilter;

/**
 * Sends the PostgREST JWT with every request of the TCK data sources, by targeting their service ids.
 * This is what an application does with Micronaut Security's token propagation or a filter of its own.
 */
@ClientFilter(serviceId = {RestDataSourceClient.SERVICE_ID_PREFIX + "default", RestDataSourceClient.SERVICE_ID_PREFIX + "other"})
class PostgreSQLBearerTokenFilter {

    @RequestFilter
    void bearerToken(MutableHttpRequest<?> request) {
        request.bearerAuth(PostgreSQL.bearerToken());
    }
}
