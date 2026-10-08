package io.micronaut.datarest.ords;

import io.micronaut.datarest.core.repositories.ords.OrdsResponses;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@MicronautTest(startApplication = false)
class OrdsResponsesDatesTest {

    @Inject
    JsonMapper jsonMapper;

    @Test
    void dateColumnsAreReadFromTheMetadataCatalog() throws IOException {
        JsonNode catalog = json("""
            {"name":"BOOKS","primarykey":["id"],"members":[
              {"name":"id","type":"NUMBER"},{"name":"title","type":"VARCHAR2"},
              {"name":"published","type":"DATE"},{"name":"UPDATED_AT","type":"DATE"},{"name":"created","type":"TIMESTAMP"}]}""");
        assertEquals(Set.of("published", "updated_at"), OrdsResponses.dateColumns(catalog));
    }

    @Test
    void isoDatesBecomeMidnightTimestampsOnWrite() throws IOException {
        JsonNode row = json("{\"title\":\"1997-06-26\",\"published\":\"1997-06-26\",\"created\":\"1997-06-26\",\"other\":\"1997-06-26T13:45:10Z\"}");
        JsonNode sent = OrdsResponses.toOrds(row, Set.of("published", "other"));
        assertEquals("1997-06-26T00:00:00Z", sent.get("published").getStringValue());
        assertEquals("1997-06-26", sent.get("title").getStringValue());
        assertEquals("1997-06-26", sent.get("created").getStringValue());
        assertEquals("1997-06-26T13:45:10Z", sent.get("other").getStringValue());
    }

    @Test
    void midnightTimestampsBecomeIsoDatesOnRead() throws IOException {
        JsonNode row = json("{\"published\":\"1997-06-26T00:00:00Z\",\"at\":\"1997-06-26T13:45:10Z\",\"created\":\"1997-06-26T00:00:00Z\",\"n\":null}");
        JsonNode bound = OrdsResponses.fromOrds(row, Set.of("published", "at", "n"));
        assertEquals("1997-06-26", bound.get("published").getStringValue());
        assertEquals("1997-06-26T13:45:10Z", bound.get("at").getStringValue());
        assertEquals("1997-06-26T00:00:00Z", bound.get("created").getStringValue());
    }

    @Test
    void rowsWithoutDateColumnsAreReturnedUntouched() throws IOException {
        JsonNode row = json("{\"title\":\"t\"}");
        assertSame(row, OrdsResponses.toOrds(row, Set.of()));
        assertSame(row, OrdsResponses.fromOrds(row, Set.of("published")));
    }

    private JsonNode json(String text) throws IOException {
        return jsonMapper.readValue(text, JsonNode.class);
    }
}
