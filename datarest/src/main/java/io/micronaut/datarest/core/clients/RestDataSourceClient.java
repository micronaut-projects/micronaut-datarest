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
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.HttpClientConfiguration;
import io.micronaut.inject.qualifiers.Qualifiers;

/**
 * HTTP client bound to a {@link RestDataSourceConfiguration}.
 * A bean is created for every configured REST data source.
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
@EachBean(RestDataSourceConfiguration.class)
public final class RestDataSourceClient implements AutoCloseable {
    private static final String HTTP_CLIENT_ID_PREFIX = "restdatasource";
    private final HttpClient httpClient;

    /**
     * @param restDataSourceConfiguration configuration of the REST data source
     * @param beanContext                 bean context used to look up an optional {@link HttpClientConfiguration}
     *                                    named {@code restdatasource<name>}
     */
    public RestDataSourceClient(RestDataSourceConfiguration restDataSourceConfiguration, BeanContext beanContext) {
        HttpClientConfiguration httpClientConfiguration = beanContext.findBean(HttpClientConfiguration.class,
            Qualifiers.byName(HTTP_CLIENT_ID_PREFIX + restDataSourceConfiguration.getName())).orElse(null);
        this.httpClient = httpClientConfiguration != null
            ? HttpClient.create(restDataSourceConfiguration.getUrl(), httpClientConfiguration)
            : HttpClient.create(restDataSourceConfiguration.getUrl());
    }

    /**
     * @return the HTTP client pointing at the REST data source URL
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    public void close() throws Exception {
        httpClient.close();
    }
}
