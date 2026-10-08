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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

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
    private static final String MEMBERS = "members";
    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String DATE = "DATE";
    private static final String MIDNIGHT = "T00:00:00Z";
    private static final Pattern ISO_DATE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final Argument<Map<String, Object>> MAP = Argument.mapOf(String.class, Object.class);
    private final JsonMapper jsonMapper;

    /**
     * @param jsonMapper mapper used to bind rows
     */
    public OrdsResponses(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /**
     * @param catalog the {@code metadata-catalog} document of a table
     * @return the lower-cased names of its {@code DATE} columns
     */
    public static Set<String> dateColumns(JsonNode catalog) {
        Set<String> columns = new HashSet<>();
        JsonNode members = catalog.get(MEMBERS);
        if (members != null) {
            for (JsonNode member : members.values()) {
                JsonNode name = member.get(NAME);
                JsonNode type = member.get(TYPE);
                if (name != null && type != null && DATE.equalsIgnoreCase(type.getStringValue())) {
                    columns.add(name.getStringValue().toLowerCase(Locale.ROOT));
                }
            }
        }
        return columns;
    }

    /**
     * Prepares a row for ORDS: a {@code DATE} column given as an ISO date ({@code 1997-06-26}) becomes the
     * ISO timestamp ORDS requires ({@code 1997-06-26T00:00:00Z}). Other values are left as they are.
     *
     * @param row         row columns, as an object node
     * @param dateColumns lower-cased names of the table's {@code DATE} columns
     * @return the row to send
     */
    public static JsonNode toOrds(JsonNode row, Set<String> dateColumns) {
        return mapDates(row, dateColumns, value -> ISO_DATE.matcher(value).matches() ? value + MIDNIGHT : value);
    }

    /**
     * Reverses {@link #toOrds}: a {@code DATE} column holding a midnight timestamp ({@code 1997-06-26T00:00:00Z})
     * becomes the ISO date ({@code 1997-06-26}), so it binds to a {@code LocalDate} as it does with other
     * gateways. A {@code DATE} carrying a time of day is left as a timestamp.
     *
     * @param row         row document from ORDS
     * @param dateColumns lower-cased names of the table's {@code DATE} columns
     * @return the row to bind
     */
    public static JsonNode fromOrds(JsonNode row, Set<String> dateColumns) {
        return mapDates(row, dateColumns, value -> value.endsWith(MIDNIGHT) && ISO_DATE.matcher(value.substring(0, value.length() - MIDNIGHT.length())).matches()
            ? value.substring(0, value.length() - MIDNIGHT.length()) : value);
    }

    /**
     * @param document    a collection document
     * @param dateColumns lower-cased names of the table's {@code DATE} columns
     * @param type        type to bind each row to
     * @param <T>         row type
     * @return the collection
     */
    public <T> Collection<T> collection(JsonNode document, Set<String> dateColumns, Class<T> type) {
        List<T> items = new ArrayList<>();
        JsonNode array = document.get(ITEMS);
        if (array != null) {
            for (JsonNode item : array.values()) {
                items.add(row(item, dateColumns, type));
            }
        }
        return new Collection<>(items,
            longOf(document, OFFSET, 0),
            (int) longOf(document, LIMIT, items.size()),
            booleanOf(document, HAS_MORE));
    }

    /**
     * @param document    a row document
     * @param dateColumns lower-cased names of the table's {@code DATE} columns
     * @param type        type to bind the row to
     * @param <T>         row type
     * @return the row, with the ORDS {@code links} dropped and {@code DATE} columns as ISO dates
     */
    public <T> T row(JsonNode document, Set<String> dateColumns, Class<T> type) {
        try {
            return jsonMapper.readValueFromTree(fromOrds(withoutLinks(document), dateColumns), Argument.of(type));
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
     * @param row any serializable row
     * @return the row as an object node, with the ORDS {@code links} dropped
     */
    public JsonNode tree(Object row) {
        try {
            return withoutLinks(jsonMapper.writeValueToTree(row));
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

    private static JsonNode mapDates(JsonNode row, Set<String> dateColumns, UnaryOperator<String> mapping) {
        if (!row.isObject() || dateColumns.isEmpty()) {
            return row;
        }
        Map<String, JsonNode> fields = new LinkedHashMap<>();
        boolean changed = false;
        for (Map.Entry<String, JsonNode> entry : row.entries()) {
            JsonNode value = entry.getValue();
            if (value.isString() && dateColumns.contains(entry.getKey().toLowerCase(Locale.ROOT))) {
                String mapped = mapping.apply(value.getStringValue());
                if (!mapped.equals(value.getStringValue())) {
                    value = JsonNode.createStringNode(mapped);
                    changed = true;
                }
            }
            fields.put(entry.getKey(), value);
        }
        return changed ? JsonNode.createObjectNode(fields) : row;
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
