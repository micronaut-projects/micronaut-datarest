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
package io.micronaut.datarest.ords;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns <a href="https://docs.oracle.com/en/database/oracle/oracle-rest-data-services/">ORDS AutoREST</a>
 * JSON documents into rows and pages.
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
@Internal
public final class OrdsResponses {
    private static final String ITEMS = "items";
    private static final String HAS_MORE = "hasMore";
    private static final String LIMIT = "limit";
    private static final String OFFSET = "offset";
    private static final String ROWS_DELETED = "rowsDeleted";
    private static final String LINKS = "links";
    private static final Argument<Map<String, Object>> MAP = Argument.mapOf(String.class, Object.class);
    private final JsonMapper jsonMapper;

    /**
     * @param jsonMapper mapper used to bind rows
     */
    public OrdsResponses(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /**
     * @param document a collection document
     * @param type     type to bind each row to
     * @param <T>      row type
     * @return the collection
     */
    public <T> Collection<T> collection(JsonNode document, Class<T> type) {
        List<T> items = new ArrayList<>();
        JsonNode array = document.get(ITEMS);
        if (array != null) {
            for (JsonNode item : array.values()) {
                items.add(row(item, type));
            }
        }
        return new Collection<>(items,
            longOf(document, OFFSET, 0),
            (int) longOf(document, LIMIT, items.size()),
            booleanOf(document, HAS_MORE));
    }

    /**
     * @param document a row document
     * @param type     type to bind the row to
     * @param <T>      row type
     * @return the row, with the ORDS {@code links} dropped
     */
    public <T> T row(JsonNode document, Class<T> type) {
        try {
            return jsonMapper.readValueFromTree(withoutLinks(document), Argument.of(type));
        } catch (IOException e) {
            throw new IllegalStateException("Could not bind the ORDS row to " + type.getName(), e);
        }
    }

    /**
     * @param document a row document
     * @return the row columns, with the ORDS {@code links} dropped
     */
    public Map<String, Object> columns(JsonNode document) {
        try {
            return new LinkedHashMap<>(jsonMapper.readValueFromTree(withoutLinks(document), MAP));
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the ORDS row columns", e);
        }
    }

    /**
     * @param row any serializable row
     * @return the row columns
     */
    public Map<String, Object> columns(Object row) {
        try {
            return columns(jsonMapper.writeValueToTree(row));
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the columns of " + row.getClass().getName(), e);
        }
    }

    /**
     * @param document the response of a {@code DELETE}
     * @return the number of deleted rows
     */
    public int rowsDeleted(JsonNode document) {
        return (int) longOf(document, ROWS_DELETED, 0);
    }

    private static JsonNode withoutLinks(JsonNode document) {
        if (!document.isObject() || document.get(LINKS) == null) {
            return document;
        }
        Map<String, JsonNode> fields = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> entry : document.entries()) {
            if (!LINKS.equals(entry.getKey())) {
                fields.put(entry.getKey(), entry.getValue());
            }
        }
        return JsonNode.createObjectNode(fields);
    }

    private static long longOf(JsonNode document, String field, long fallback) {
        JsonNode node = document.get(field);
        return node == null || node.isNull() ? fallback : node.getLongValue();
    }

    private static boolean booleanOf(JsonNode document, String field) {
        JsonNode node = document.get(field);
        return node != null && !node.isNull() && node.getBooleanValue();
    }

    /**
     * A collection document: {@code {"items":[...],"hasMore":true,"limit":25,"offset":0,"count":25}}.
     *
     * @param items   rows of the window
     * @param offset  number of rows skipped
     * @param limit   maximum number of rows requested
     * @param hasMore whether rows exist beyond this window
     * @param <T>     row type
     */
    public record Collection<T>(List<T> items, long offset, int limit, boolean hasMore) {
    }
}
