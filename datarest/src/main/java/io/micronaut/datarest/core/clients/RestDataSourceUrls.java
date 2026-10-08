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

import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.Internal;
import io.micronaut.datarest.core.conf.RestDataSourceConfiguration;

import java.net.URL;

/**
 * Validates the URL of a REST data source before it is used.
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
@Internal
final class RestDataSourceUrls {

    private RestDataSourceUrls() {
    }

    /**
     * @param configuration a REST data source
     * @return its URL
     * @throws ConfigurationException when the data source has no URL, naming the property to set
     */
    static URL require(RestDataSourceConfiguration configuration) {
        URL url = configuration.getUrl();
        if (url == null) {
            throw new ConfigurationException("REST data source '" + configuration.getName()
                + "' has no URL. Set restdatasources." + configuration.getName() + ".url");
        }
        return url;
    }
}
