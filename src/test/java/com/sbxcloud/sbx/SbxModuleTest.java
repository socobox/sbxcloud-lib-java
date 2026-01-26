package com.sbxcloud.sbx;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sbxcloud.sbx.annotation.SbxEntity;
import com.sbxcloud.sbx.annotation.SbxModel;
import com.sbxcloud.sbx.jackson.SbxModule;
import com.sbxcloud.sbx.model.SBXMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SbxModule and entity serialization.
 */
class SbxModuleTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new SbxModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    void shouldDeserializeKeyFromUnderscoreKEY() throws Exception {
        String json = """
                {"_KEY": "test-key-123", "name": "Test"}
                """;

        var entity = mapper.readValue(json, TestEntity.class);

        assertEquals("test-key-123", entity.key());
        assertEquals("Test", entity.name());
    }

    @Test
    void shouldDeserializeMetaFromUnderscoreMETA() throws Exception {
        String json = """
                {
                    "_KEY": "key-1",
                    "_META": {
                        "created_time": "2025-01-26T10:00:00Z"
                    },
                    "name": "Test"
                }
                """;

        var entity = mapper.readValue(json, TestEntity.class);

        assertEquals("key-1", entity.key());
        assertNotNull(entity.meta());
        assertEquals("Test", entity.name());
    }

    @Test
    void shouldSerializeKeyAsUnderscoreKEY() throws Exception {
        var entity = new TestEntity("my-key", null, "Test Name");

        String json = mapper.writeValueAsString(entity);

        assertTrue(json.contains("\"_KEY\""), "Should contain _KEY");
        assertTrue(json.contains("\"my-key\""), "Should contain key value");
    }

    @Test
    void shouldHandleNullKeyAndMeta() throws Exception {
        String json = """
                {"name": "Test"}
                """;

        var entity = mapper.readValue(json, TestEntity.class);

        assertNull(entity.key());
        assertNull(entity.meta());
        assertEquals("Test", entity.name());
    }

    @Test
    void shouldPreserveOtherFieldsUnchanged() throws Exception {
        String json = """
                {
                    "_KEY": "k1",
                    "name": "John",
                    "count": 42,
                    "active": true
                }
                """;

        var entity = mapper.readValue(json, TestEntityWithFields.class);

        assertEquals("k1", entity.key());
        assertEquals("John", entity.name());
        assertEquals(42, entity.count());
        assertTrue(entity.active());
    }

    @Test
    void shouldRoundTripCorrectly() throws Exception {
        var original = new TestEntity("round-trip-key", null, "Round Trip");

        String json = mapper.writeValueAsString(original);
        var deserialized = mapper.readValue(json, TestEntity.class);

        assertEquals(original.key(), deserialized.key());
        assertEquals(original.name(), deserialized.name());
    }

    // Test entities - single @SbxModel annotation does everything!

    @SbxModel("test")
    record TestEntity(
            String key,      // Auto-mapped to _KEY
            SBXMeta meta,    // Auto-mapped to _META
            String name
    ) implements SbxEntity {}

    @SbxModel("test_fields")
    record TestEntityWithFields(
            String key,
            SBXMeta meta,
            String name,
            Integer count,
            Boolean active
    ) implements SbxEntity {}
}
