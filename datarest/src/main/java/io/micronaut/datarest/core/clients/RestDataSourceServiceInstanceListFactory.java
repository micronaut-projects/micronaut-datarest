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

import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.Internal;
import io.micronaut.datarest.core.conf.RestDataSourceConfiguration;
import io.micronaut.discovery.ServiceInstanceList;
import io.micronaut.discovery.StaticServiceInstanceList;

import java.net.URISyntaxException;
import java.util.List;

/**
 * Registers every REST data source as a Micronaut service, so the {@link io.micronaut.http.client.HttpClientRegistry}
 * can resolve the service id {@code restdatasource-<name>} to the configured URL.
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
@Internal
@Factory
final class RestDataSourceServiceInstanceListFactory {

    /**
     * @param configuration a REST data source
     * @return the single instance service backing its HTTP client
     */
    @EachBean(RestDataSourceConfiguration.class)
    ServiceInstanceList serviceInstanceList(RestDataSourceConfiguration configuration) {
        try {
            return new StaticServiceInstanceList(RestDataSourceClient.serviceId(configuration.getName()),
                List.of(configuration.getUrl().toURI()));
        } catch (URISyntaxException e) {
            throw new ConfigurationException("Invalid URL for REST data source '" + configuration.getName() + "'", e);
        }
    }
}
