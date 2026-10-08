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
package io.micronaut.datarest.core.repositories;

import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.Internal;
import io.micronaut.datarest.core.Dialect;
import io.micronaut.datarest.core.clients.RestDataSourceClient;
import io.micronaut.datarest.core.repositories.ords.OrdsReactiveRestGenericRepository;
import io.micronaut.datarest.core.repositories.ords.OrdsReactorRestGenericRepository;
import io.micronaut.datarest.core.repositories.ords.OrdsRestGenericRepository;
import io.micronaut.datarest.core.repositories.postgrest.PostgrestReactiveRestGenericRepository;
import io.micronaut.datarest.core.repositories.postgrest.PostgrestReactorRestGenericRepository;
import io.micronaut.datarest.core.repositories.postgrest.PostgrestRestGenericRepository;
import io.micronaut.json.JsonMapper;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

/**
 * Creates one repository per REST data source, picking the implementation from the data source dialect.
 *
 * <p>A single bean definition per contract is required: the dialect is a per data source property, so it cannot
 * be expressed as a {@link Requires} condition, and several definitions of the same type and name would be
 * ambiguous before any factory method runs.</p>
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
@Internal
@Factory
final class RestGenericRepositoryFactory {

    /**
     * @param client     REST data source client
     * @param jsonMapper JSON mapper
     * @return a blocking repository for the data source dialect
     */
    @EachBean(RestDataSourceClient.class)
    RestGenericRepository restGenericRepository(RestDataSourceClient client, JsonMapper jsonMapper) {
        return switch (dialect(client)) {
            case ORDS -> new OrdsRestGenericRepository(client, jsonMapper);
            case POSTGREST -> new PostgrestRestGenericRepository(client);
        };
    }

    /**
     * @param client     REST data source client
     * @param jsonMapper JSON mapper
     * @return a Reactor repository for the data source dialect
     */
    @Requires(classes = Mono.class)
    @EachBean(RestDataSourceClient.class)
    ReactorRestGenericRepository reactorRestGenericRepository(RestDataSourceClient client, JsonMapper jsonMapper) {
        return switch (dialect(client)) {
            case ORDS -> new OrdsReactorRestGenericRepository(client, jsonMapper);
            case POSTGREST -> new PostgrestReactorRestGenericRepository(client);
        };
    }

    /**
     * @param client     REST data source client
     * @param jsonMapper JSON mapper
     * @return a Reactive Streams repository for the data source dialect
     */
    @Requires(classes = Mono.class)
    @EachBean(RestDataSourceClient.class)
    ReactiveRestGenericRepository reactiveRestGenericRepository(RestDataSourceClient client, JsonMapper jsonMapper) {
        return switch (dialect(client)) {
            case ORDS -> new OrdsReactiveRestGenericRepository(client, jsonMapper);
            case POSTGREST -> new PostgrestReactiveRestGenericRepository(client);
        };
    }

    private static Dialect dialect(RestDataSourceClient client) {
        @Nullable Dialect dialect = client.getDialect();
        if (dialect == null) {
            throw new ConfigurationException("REST data source client '" + client.getServiceId()
                + "' has no dialect. Set restdatasources.<name>.dialect to one of ORDS or POSTGREST");
        }
        return dialect;
    }
}
