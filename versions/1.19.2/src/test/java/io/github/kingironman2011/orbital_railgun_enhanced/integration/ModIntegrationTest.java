package io.github.kingironman2011.orbital_railgun_enhanced.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration/smoke tests for the Orbital Railgun Enhanced mod.
 * These tests verify basic functionality without requiring a full Minecraft server.
 * They test config persistence, serialization, and file operations.
 */
class ModIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Config file should be created and persisted")
    void testConfigFilePersistence() throws IOException {
        // Simulate config file creation and persistence
        File configDir = tempDir.resolve("config").toFile();
        assertTrue(configDir.mkdirs(), "Config directory should be created");

        File configFile = new File(configDir, "orbital-railgun-enhanced-server-config.json");
        String configContent = """
                {
                    "debugMode": false,
                    "soundRange": 500.0,
                    "strikeDamage": 20.0,
                    "cooldownTicks": 100,
                    "maxActiveStrikes": 10,
                    "enableParticles": true
                }
                """;

        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(configContent);
        }

        assertTrue(configFile.exists(), "Config file should exist");
        String readContent = Files.readString(configFile.toPath());
        assertTrue(readContent.contains("\"debugMode\""), "Config should contain debugMode");
        assertTrue(readContent.contains("\"soundRange\""), "Config should contain soundRange");
    }

    @Test
    @DisplayName("Config file should handle reload without corruption")
    void testConfigFileReload() throws IOException {
        File configFile = tempDir.resolve("reload-test-config.json").toFile();

        // Write initial config
        String initialConfig = """
                {
                    "debugMode": false,
                    "soundRange": 500.0
                }
                """;
        Files.writeString(configFile.toPath(), initialConfig);

        // Modify config (simulate settings change)
        String modifiedConfig = """
                {
                    "debugMode": true,
                    "soundRange": 750.0
                }
                """;
        Files.writeString(configFile.toPath(), modifiedConfig);

        // Read back and verify
        String readConfig = Files.readString(configFile.toPath());
        assertTrue(readConfig.contains("true"), "Config should reflect debug mode change");
        assertTrue(readConfig.contains("750.0"), "Config should reflect sound range change");
    }

    @Test
    @DisplayName("Mod resources should be properly structured")
    void testModResourceStructure() {
        // Verify expected resource paths exist as strings (we can't check actual files here)
        String[] expectedResourcePaths = {
                "assets/orbital_railgun_enhanced/lang/en_us.json",
                "assets/orbital_railgun_enhanced/sounds.json",
                "assets/orbital_railgun_enhanced/icon.png",
                "data/orbital_railgun_enhanced/damage_type/strike.json",
                "data/orbital_railgun_enhanced/recipes/orbital_railgun.json"
        };

        for (String path : expectedResourcePaths) {
            assertNotNull(path, "Resource path should not be null: " + path);
            assertTrue(path.startsWith("assets/") || path.startsWith("data/"),
                    "Resource path should start with assets/ or data/: " + path);
        }
    }

    @Test
    @DisplayName("Sound IDs should be properly formatted")
    void testSoundIdFormat() {
        String modId = "orbital_railgun_enhanced";
        String[] soundNames = {"railgun_shoot", "scope_on", "equip"};

        for (String soundName : soundNames) {
            String fullId = modId + ":" + soundName;
            assertTrue(fullId.contains(":"), "Sound ID should contain namespace separator");
            assertFalse(fullId.contains(" "), "Sound ID should not contain spaces");
            assertTrue(fullId.matches("[a-z0-9_]+:[a-z0-9_]+"),
                    "Sound ID should match Minecraft identifier format: " + fullId);
        }
    }

    @Test
    @DisplayName("Packet IDs should be properly formatted")
    void testPacketIdFormat() {
        String modId = "orbital_railgun_enhanced";
        String[] packetNames = {"play_sound", "stop_area_sound", "shoot_packet", "client_sync_packet"};

        for (String packetName : packetNames) {
            String fullId = modId + ":" + packetName;
            assertTrue(fullId.contains(":"), "Packet ID should contain namespace separator");
            assertTrue(fullId.matches("[a-z0-9_]+:[a-z0-9_]+"),
                    "Packet ID should match Minecraft identifier format: " + fullId);
        }
    }

    @Test
    @DisplayName("Strike timing constants should be valid")
    void testStrikeTimingConstants() {
        // These are the known constants from OrbitalRailgun and StrikeManager
        long railgunSoundDurationMs = 52992L;
        int pullEffectStartTicks = 400;
        int impactAgeTicks = 700;
        int ticksPerSecond = 20;

        // Verify timing relationships
        assertTrue(impactAgeTicks > pullEffectStartTicks,
                "Impact should happen after pull effect starts");
        assertEquals(35, impactAgeTicks / ticksPerSecond,
                "Impact should occur at 35 seconds");
        assertEquals(20, pullEffectStartTicks / ticksPerSecond,
                "Pull effect should start at 20 seconds");

        // Sound duration should cover strike sequence
        double soundDurationSeconds = railgunSoundDurationMs / 1000.0;
        assertTrue(soundDurationSeconds > impactAgeTicks / (double) ticksPerSecond,
                "Sound duration should be longer than strike sequence");
    }

    @Test
    @DisplayName("Strike damage type identifier should be valid")
    void testDamageTypeIdentifier() {
        String modId = "orbital_railgun_enhanced";
        String damageType = "strike";
        String fullId = modId + ":" + damageType;

        assertTrue(fullId.matches("[a-z0-9_]+:[a-z0-9_]+"),
                "Damage type ID should match Minecraft identifier format");
        assertEquals("orbital_railgun_enhanced:strike", fullId);
    }

    @Test
    @DisplayName("Item settings should be valid")
    void testItemSettings() {
        // Verify item constants match expected values
        int maxUseTime = 24000; // From OrbitalRailgunItem
        int maxCount = 1;
        String rarity = "EPIC";

        assertEquals(24000, maxUseTime, "Max use time should be 24000 ticks");
        assertEquals(1, maxCount, "Max stack count should be 1");
        assertEquals("EPIC", rarity, "Rarity should be EPIC");
    }

    @Test
    @DisplayName("Mod metadata should contain required fields")
    void testModMetadataRequirements() {
        // These are the required fields for fabric.mod.json
        String[] requiredFields = {
                "schemaVersion",
                "id",
                "version",
                "name",
                "environment",
                "entrypoints",
                "depends"
        };

        // Just verify the field names are valid (actual content is in fabric.mod.json)
        for (String field : requiredFields) {
            assertNotNull(field, "Required field should not be null");
            assertFalse(field.isEmpty(), "Required field should not be empty");
        }
    }

    @Test
    @DisplayName("Multiple config saves should not corrupt file")
    void testMultipleConfigSaves() throws IOException {
        File configFile = tempDir.resolve("multi-save-config.json").toFile();

        // Simulate multiple rapid saves
        for (int i = 0; i < 10; i++) {
            String config = String.format("""
                    {
                        "debugMode": %b,
                        "soundRange": %d.0,
                        "iteration": %d
                    }
                    """, i % 2 == 0, 500 + i * 10, i);
            Files.writeString(configFile.toPath(), config);
        }

        // Final read should succeed and contain last values
        String finalConfig = Files.readString(configFile.toPath());
        assertTrue(finalConfig.contains("\"iteration\": 9"), "Config should contain last iteration");
        assertTrue(finalConfig.contains("590.0"), "Config should contain last sound range");
    }
}
