/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.datarest.core.clients;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.datarest.core.conf.RestDataSourceConfiguration;
import io.micronaut.http.HttpVersion;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.HttpClientConfiguration;
import io.micronaut.http.client.HttpClientRegistry;
import io.micronaut.http.client.HttpVersionSelection;
import io.micronaut.inject.qualifiers.Qualifiers;

import java.net.URL;

/**
 * HTTP client bound to a {@link RestDataSourceConfiguration}.
 * A bean is created for every configured REST data source.
 *
 * <p>The client is obtained from the {@link HttpClientRegistry} under the service id
 * {@code restdatasource-<name>}, so it behaves like a client injected with {@code @Client("restdatasource-<name>")}:
 * {@code micronaut.http.services.restdatasource-<name>.*} configures it, and client filters declared for that
 * service id, such as the ones Micronaut Security provides to propagate or obtain tokens, apply to every request.</p>
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
@EachBean(RestDataSourceConfiguration.class)
public final class RestDataSourceClient {
    /**
     * Prefix of the service id under which the client of a data source is registered.
     */
    public static final String SERVICE_ID_PREFIX = "restdatasource-";
    private final HttpClient httpClient;
    private final URL url;
    private final String serviceId;

    /**
     * @param restDataSourceConfiguration configuration of the REST data source
     * @param registry                    registry the client is obtained from
     * @param beanContext                 bean context used to look up the optional
     *                                    {@link HttpClientConfiguration} of the service id
     */
    public RestDataSourceClient(RestDataSourceConfiguration restDataSourceConfiguration,
                                HttpClientRegistry<?> registry,
                                BeanContext beanContext) {
        this.url = RestDataSourceUrls.require(restDataSourceConfiguration);
        this.serviceId = serviceId(restDataSourceConfiguration.getName());
        HttpVersionSelection version = beanContext.findBean(HttpClientConfiguration.class, Qualifiers.byName(serviceId))
            .map(HttpVersionSelection::forClientConfiguration)
            .orElseGet(() -> HttpVersionSelection.forLegacyVersion(HttpVersion.HTTP_1_1));
        this.httpClient = registry.getClient(version, serviceId, null);
    }

    /**
     * @param name name of a REST data source
     * @return the service id of its HTTP client
     */
    public static String serviceId(String name) {
        return SERVICE_ID_PREFIX + name;
    }

    /**
     * @return the HTTP client pointing at the REST data source URL
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * @return the base URL of the REST data source
     */
    public URL getUrl() {
        return url;
    }

    /**
     * @return the service id of the HTTP client, {@code restdatasource-<name>}
     */
    public String getServiceId() {
        return serviceId;
    }
}
