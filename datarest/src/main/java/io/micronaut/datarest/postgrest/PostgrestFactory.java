package io.micronaut.datarest.postgrest;

import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.datarest.core.Dialect;
import io.micronaut.datarest.core.clients.RestDataSourceClient;
import io.micronaut.datarest.core.repositories.ReactiveRestGenericRepository;
import io.micronaut.datarest.core.repositories.ReactorRestGenericRepository;
import io.micronaut.datarest.core.repositories.RestGenericRepository;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

@Factory
public class PostgrestFactory {
    @EachBean(RestDataSourceClient.class)
    @Nullable
    RestGenericRepository createRestGenericRepository(RestDataSourceClient restDataSourceClient) {
        if (restDataSourceClient.getDialect() == Dialect.POSTGREST) {
            return new PostgrestRestGenericRepository(restDataSourceClient);
        }
        return null;
    }

    @Nullable
    @Requires(classes = { Mono.class })
    @EachBean(RestDataSourceClient.class)
    ReactorRestGenericRepository createReactorRestGenericRepository(RestDataSourceClient restDataSourceClient) {
        if (restDataSourceClient.getDialect() == Dialect.POSTGREST) {
            return new PostgrestReactorRestGenericRepository(restDataSourceClient);
        }
        return null;
    }

    @Nullable
    @Requires(classes = { Mono.class })
    @EachBean(RestDataSourceClient.class)
    ReactiveRestGenericRepository createReactiveRestGenericRepository(RestDataSourceClient restDataSourceClient) {
        if (restDataSourceClient.getDialect() == Dialect.POSTGREST) {
            return new PostgrestReactiveRestGenericRepository(restDataSourceClient);
        }
        return null;
    }

}
