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
package io.micronaut.datarest.tck;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Supplies the configuration of the REST data source the TCK runs against. A suite selects a provider with
 * {@link #select(RestDatasourceProvider)} before running the TCK; without a selection the single provider registered
 * through {@link ServiceLoader} is used.
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
public interface RestDatasourceProvider {

    /**
     * Holder of the provider selected by the running suite.
     */
    AtomicReference<@Nullable RestDatasourceProvider> SELECTED = new AtomicReference<>();

    /**
     * @return the {@code restdatasources.*} properties of the data source under test
     */
    Map<String, String> getProperties();

    /**
     * Selects the provider the TCK uses until the next selection.
     *
     * @param provider the provider, or {@code null} to fall back to {@link ServiceLoader}
     */
    static void select(@Nullable RestDatasourceProvider provider) {
        SELECTED.set(provider);
    }

    /**
     * @return the selected provider, otherwise the single registered one
     * @throws IllegalStateException if no provider is selected and not exactly one is registered
     */
    static RestDatasourceProvider getFirst() {
        RestDatasourceProvider selected = SELECTED.get();
        if (selected != null) {
            return selected;
        }
        List<RestDatasourceProvider> services = ServiceLoader.load(RestDatasourceProvider.class).stream()
            .map(ServiceLoader.Provider::get)
            .toList();
        if (services.size() != 1) {
            throw new IllegalStateException("Expected one registered " + RestDatasourceProvider.class.getSimpleName()
                + " or a selected one, found " + services.size());
        }
        return services.getFirst();
    }
}
