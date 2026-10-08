package io.micronaut.datarest.ords;

import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.datarest.core.Dialect;
import io.micronaut.datarest.core.clients.RestDataSourceClient;
import io.micronaut.datarest.core.repositories.ReactiveRestGenericRepository;
import io.micronaut.datarest.core.repositories.ReactorRestGenericRepository;
import io.micronaut.datarest.core.repositories.RestGenericRepository;
import io.micronaut.json.JsonMapper;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

@Factory
public class OrdsFactory {
    @EachBean(RestDataSourceClient.class)
    @Nullable
    RestGenericRepository createRestGenericRepository(RestDataSourceClient restDataSourceClient, JsonMapper jsonMapper) {
        if (restDataSourceClient.getDialect() == Dialect.ORDS) {
            return new OrdsRestGenericRepository(restDataSourceClient, jsonMapper);
        }
        return null;
    }

    @Nullable
    @Requires(classes = { Mono.class })
    @EachBean(RestDataSourceClient.class)
    ReactorRestGenericRepository createReactorRestGenericRepository(RestDataSourceClient restDataSourceClient, JsonMapper jsonMapper) {
        if (restDataSourceClient.getDialect() == Dialect.ORDS) {
            return new OrdsReactorRestGenericRepository(restDataSourceClient, jsonMapper);
        }
        return null;
    }

    @Nullable
    @Requires(classes = { Mono.class })
    @EachBean(RestDataSourceClient.class)
    ReactiveRestGenericRepository createReactiveRestGenericRepository(RestDataSourceClient restDataSourceClient, JsonMapper jsonMapper) {
        if (restDataSourceClient.getDialect() == Dialect.ORDS) {
            return new OrdsReactiveRestGenericRepository(restDataSourceClient, jsonMapper);
        }
        return null;
    }

}
