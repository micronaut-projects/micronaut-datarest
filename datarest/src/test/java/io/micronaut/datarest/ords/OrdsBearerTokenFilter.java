package io.micronaut.datarest.ords;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.type.Argument;
import io.micronaut.datarest.core.clients.RestDataSourceClient;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.ClientFilter;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.client.HttpClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Obtains an access token from the ORDS {@code oauth/token} endpoint with the client credentials grant and sends
 * it with every request of the TCK data source. Micronaut Security's client credentials filter does the same
 * for an application, driven by configuration.
 */
@Requires(property = "restdatasources.default.dialect", value = "ORDS")
@ClientFilter(serviceId = RestDataSourceClient.SERVICE_ID_PREFIX + "default")
class OrdsBearerTokenFilter {
    private static final Argument<Map<String, Object>> MAP = Argument.mapOf(String.class, Object.class);
    private volatile String accessToken;

    @RequestFilter
    void bearerToken(MutableHttpRequest<?> request) {
        request.bearerAuth(accessToken());
    }

    private String accessToken() {
        if (accessToken == null) {
            synchronized (this) {
                if (accessToken == null) {
                    accessToken = fetchAccessToken();
                }
            }
        }
        return accessToken;
    }

    /**
     * Uses a client created outside the registry, so this filter does not apply to the token request itself.
     */
    private static String fetchAccessToken() {
        try (HttpClient raw = HttpClient.create(new URL(Ords.getUrl()))) {
            HttpRequest<?> request = HttpRequest.POST(Ords.getUrl() + "/oauth/token", "grant_type=client_credentials")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .basicAuth(Ords.getClientId(), Ords.getClientSecret());
            Map<String, Object> body = raw.toBlocking().retrieve(request, MAP);
            return String.valueOf(body.get("access_token"));
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid ORDS URL", e);
        }
    }
}
