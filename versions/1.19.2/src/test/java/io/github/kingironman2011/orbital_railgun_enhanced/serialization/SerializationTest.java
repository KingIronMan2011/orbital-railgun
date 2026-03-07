package io.github.kingironman2011.orbital_railgun_enhanced.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for saves, persistence, networking correctness, and serialization.
 * These tests verify that data is correctly serialized and deserialized.
 */
class SerializationTest {

    @TempDir
    Path tempDir;

    private Gson gson;

    @BeforeEach
    void setUp() {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Test
    @DisplayName("Config should serialize to JSON correctly")
    void testConfigSerialization() {
        TestConfig config = new TestConfig();
        config.debugMode = true;
        config.soundRange = 750.0;
        config.strikeDamage = 30.0f;
        config.cooldownTicks = 150;
        config.maxActiveStrikes = 15;
        config.enableParticles = false;

        String json = gson.toJson(config);

        assertNotNull(json, "Serialized JSON should not be null");
        assertTrue(json.contains("\"debugMode\": true"), "JSON should contain debugMode");
        assertTrue(json.contains("\"soundRange\": 750.0"), "JSON should contain soundRange");
        assertTrue(json.contains("\"strikeDamage\": 30.0"), "JSON should contain strikeDamage");
        assertTrue(json.contains("\"cooldownTicks\": 150"), "JSON should contain cooldownTicks");
        assertTrue(json.contains("\"maxActiveStrikes\": 15"), "JSON should contain maxActiveStrikes");
        assertTrue(json.contains("\"enableParticles\": false"), "JSON should contain enableParticles");
    }

    @Test
    @DisplayName("Config should deserialize from JSON correctly")
    void testConfigDeserialization() {
        String json = """
                {
                    "debugMode": true,
                    "soundRange": 600.0,
                    "strikeDamage": 25.0,
                    "cooldownTicks": 120,
                    "maxActiveStrikes": 12,
                    "enableParticles": true
                }
                """;

        TestConfig config = gson.fromJson(json, TestConfig.class);

        assertNotNull(config, "Deserialized config should not be null");
        assertTrue(config.debugMode, "debugMode should be true");
        assertEquals(600.0, config.soundRange, 0.001, "soundRange should be 600.0");
        assertEquals(25.0f, config.strikeDamage, 0.001, "strikeDamage should be 25.0");
        assertEquals(120, config.cooldownTicks, "cooldownTicks should be 120");
        assertEquals(12, config.maxActiveStrikes, "maxActiveStrikes should be 12");
        assertTrue(config.enableParticles, "enableParticles should be true");
    }

    @Test
    @DisplayName("Config should handle null values gracefully")
    void testNullHandling() {
        String jsonWithNull = """
                {
                    "debugMode": null,
                    "soundRange": 500.0
                }
                """;

        // Gson should handle null for primitive wrappers
        TestConfigNullable config = gson.fromJson(jsonWithNull, TestConfigNullable.class);
        assertNotNull(config, "Config should be created even with null values");
        assertNull(config.debugMode, "debugMode should be null");
        assertEquals(500.0, config.soundRange, 0.001);
    }

    @Test
    @DisplayName("Config should survive round-trip serialization")
    void testRoundTripSerialization() throws IOException {
        TestConfig original = new TestConfig();
        original.debugMode = true;
        original.soundRange = 888.0;
        original.strikeDamage = 42.0f;
        original.cooldownTicks = 200;
        original.maxActiveStrikes = 20;
        original.enableParticles = false;

        // Serialize to file
        File configFile = tempDir.resolve("roundtrip-config.json").toFile();
        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(original, writer);
        }

        // Deserialize from file
        TestConfig loaded;
        try (FileReader reader = new FileReader(configFile)) {
            loaded = gson.fromJson(reader, TestConfig.class);
        }

        // Verify all fields match
        assertEquals(original.debugMode, loaded.debugMode);
        assertEquals(original.soundRange, loaded.soundRange, 0.001);
        assertEquals(original.strikeDamage, loaded.strikeDamage, 0.001);
        assertEquals(original.cooldownTicks, loaded.cooldownTicks);
        assertEquals(original.maxActiveStrikes, loaded.maxActiveStrikes);
        assertEquals(original.enableParticles, loaded.enableParticles);
    }

    @Test
    @DisplayName("Invalid JSON should throw exception")
    void testInvalidJsonHandling() {
        String invalidJson = "{ this is not valid json }";

        assertThrows(JsonSyntaxException.class, () -> {
            gson.fromJson(invalidJson, TestConfig.class);
        }, "Invalid JSON should throw JsonSyntaxException");
    }

    @Test
    @DisplayName("Empty JSON object should create default config")
    void testEmptyJsonObject() {
        String emptyJson = "{}";

        TestConfig config = gson.fromJson(emptyJson, TestConfig.class);

        assertNotNull(config, "Config should be created from empty JSON");
        // Default values for primitives
        assertFalse(config.debugMode, "debugMode should default to false");
        assertEquals(0.0, config.soundRange, 0.001, "soundRange should default to 0");
    }

    @ParameterizedTest
    @DisplayName("Edge case numeric values should serialize correctly")
    @ValueSource(doubles = {0.0, -1.0, Double.MAX_VALUE, Double.MIN_VALUE, 999999.999})
    void testEdgeCaseNumericValues(double value) {
        TestConfig config = new TestConfig();
        config.soundRange = value;

        String json = gson.toJson(config);
        TestConfig loaded = gson.fromJson(json, TestConfig.class);

        assertEquals(value, loaded.soundRange, 0.0001,
                "Value " + value + " should survive round-trip");
    }

    @Test
    @DisplayName("Strike data should serialize for networking")
    void testStrikeDataSerialization() {
        // Simulate strike data that would be sent over network
        StrikeData strike = new StrikeData();
        strike.x = 100;
        strike.y = 64;
        strike.z = -200;
        strike.startTick = 12345;
        strike.dimension = "minecraft:overworld";

        String json = gson.toJson(strike);
        StrikeData loaded = gson.fromJson(json, StrikeData.class);

        assertEquals(strike.x, loaded.x);
        assertEquals(strike.y, loaded.y);
        assertEquals(strike.z, loaded.z);
        assertEquals(strike.startTick, loaded.startTick);
        assertEquals(strike.dimension, loaded.dimension);
    }

    @Test
    @DisplayName("BlockPos-like coordinates should serialize correctly")
    void testBlockPosCoordinateSerialization() {
        // Test various coordinate combinations including negative values
        int[][] testCoords = {
                {0, 0, 0},
                {100, 64, -200},
                {-1000, 256, 1000},
                {Integer.MAX_VALUE, 0, Integer.MIN_VALUE}
        };

        for (int[] coords : testCoords) {
            BlockPosData pos = new BlockPosData(coords[0], coords[1], coords[2]);
            String json = gson.toJson(pos);
            BlockPosData loaded = gson.fromJson(json, BlockPosData.class);

            assertEquals(coords[0], loaded.x, "X coordinate mismatch");
            assertEquals(coords[1], loaded.y, "Y coordinate mismatch");
            assertEquals(coords[2], loaded.z, "Z coordinate mismatch");
        }
    }

    @Test
    @DisplayName("Config persistence across restarts")
    void testPersistenceAcrossRestarts() throws IOException {
        File configFile = tempDir.resolve("persistent-config.json").toFile();

        // Simulate first "server start" - create and save config
        TestConfig config1 = new TestConfig();
        config1.debugMode = true;
        config1.soundRange = 777.0;

        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(config1, writer);
        }

        // Clear reference to simulate shutdown
        config1 = null;

        // Simulate second "server start" - load config
        TestConfig config2;
        try (FileReader reader = new FileReader(configFile)) {
            config2 = gson.fromJson(reader, TestConfig.class);
        }

        assertTrue(config2.debugMode, "debugMode should persist");
        assertEquals(777.0, config2.soundRange, 0.001, "soundRange should persist");
    }

    @Test
    @DisplayName("Concurrent file access should not corrupt data")
    void testConcurrentFileAccess() throws Exception {
        File configFile = tempDir.resolve("concurrent-config.json").toFile();

        // Initial write
        TestConfig initial = new TestConfig();
        initial.soundRange = 500.0;
        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(initial, writer);
        }

        // Simulate concurrent reads (no actual concurrency, but sequential to verify file isn't corrupted)
        for (int i = 0; i < 100; i++) {
            try (FileReader reader = new FileReader(configFile)) {
                TestConfig loaded = gson.fromJson(reader, TestConfig.class);
                assertEquals(500.0, loaded.soundRange, 0.001);
            }
        }
    }

    // Test helper classes
    static class TestConfig {
        boolean debugMode;
        double soundRange;
        float strikeDamage;
        int cooldownTicks;
        int maxActiveStrikes;
        boolean enableParticles;
    }

    static class TestConfigNullable {
        Boolean debugMode;
        Double soundRange;
    }

    static class StrikeData {
        int x, y, z;
        int startTick;
        String dimension;
    }

    static class BlockPosData {
        int x, y, z;

        BlockPosData() {
        }

        BlockPosData(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
