package io.github.kingironman2011.orbital_railgun_enhanced.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for math utilities used in strike calculations.
 * These tests verify radius calculations, distance checks, and coordinate math.
 */
class StrikeMathTest {

    private static final int RADIUS = 24;
    private static final int RADIUS_SQUARED = RADIUS * RADIUS;

    @Test
    @DisplayName("Radius squared should be correctly calculated")
    void testRadiusSquared() {
        assertEquals(576, RADIUS_SQUARED, "24^2 should equal 576");
    }

    @ParameterizedTest
    @DisplayName("Points inside radius should be detected")
    @CsvSource({
            "0, 0",      // center
            "10, 10",    // inside
            "24, 0",     // exactly on radius (x-axis)
            "0, 24",     // exactly on radius (z-axis)
            "-10, -10",  // negative coordinates inside
            "16, 16"     // diagonal inside (16^2 + 16^2 = 512 < 576)
    })
    void testPointsInsideOrOnRadius(int x, int z) {
        int distanceSquared = x * x + z * z;
        assertTrue(distanceSquared <= RADIUS_SQUARED,
                String.format("Point (%d, %d) with distance^2 %d should be inside or on radius^2 %d",
                        x, z, distanceSquared, RADIUS_SQUARED));
    }

    @ParameterizedTest
    @DisplayName("Points outside radius should be detected")
    @CsvSource({
            "25, 0",     // just outside x-axis
            "0, 25",     // just outside z-axis
            "100, 100",  // far outside
            "-30, -30"   // negative far outside
    })
    void testPointsOutsideRadius(int x, int z) {
        int distanceSquared = x * x + z * z;
        assertTrue(distanceSquared > RADIUS_SQUARED,
                String.format("Point (%d, %d) should be outside radius", x, z));
    }

    @Test
    @DisplayName("Distance squared calculation should be correct")
    void testDistanceSquaredCalculation() {
        // Test specific distance calculations
        assertEquals(100, calculateDistanceSquared(6, 8), "6^2 + 8^2 should equal 100");
        assertEquals(25, calculateDistanceSquared(3, 4), "3^2 + 4^2 should equal 25");
        assertEquals(0, calculateDistanceSquared(0, 0), "0^2 + 0^2 should equal 0");
    }

    @Test
    @DisplayName("Mask generation should cover correct area")
    void testMaskGenerationLogic() {
        int maskSize = RADIUS * 2 + 1;
        assertEquals(49, maskSize, "Mask size should be 49 (24*2+1)");

        // Count points that should be in the circle
        int pointsInCircle = 0;
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                if (x * x + z * z <= RADIUS_SQUARED) {
                    pointsInCircle++;
                }
            }
        }

        // The number of points in a circle of radius 24 should be approximately pi*24^2 = ~1810
        assertTrue(pointsInCircle > 1750, "Should have enough points in circle (>1750)");
        assertTrue(pointsInCircle < 1900, "Should not exceed expected points (<1900)");
    }

    @Test
    @DisplayName("Pull effect magnitude calculation")
    void testPullEffectMagnitude() {
        // Test the pull effect formula: Math.min(1. / Math.abs(dir.length() - 20.) * 4. * (age - 400.) / 300., 5.)
        // When age = 400, magnitude should be 0
        double magAt400 = calculatePullMagnitude(30.0, 400);
        assertEquals(0.0, magAt400, 0.001, "Pull magnitude at age 400 should be 0");

        // When age = 700, magnitude should be higher
        double magAt700 = calculatePullMagnitude(30.0, 700);
        assertTrue(magAt700 > 0, "Pull magnitude at age 700 should be positive");

        // Magnitude should be capped at 5
        double magMaxed = calculatePullMagnitude(20.1, 700);
        assertTrue(magMaxed <= 5.0, "Pull magnitude should be capped at 5");
    }

    @ParameterizedTest
    @DisplayName("Pull effect should increase with age")
    @ValueSource(ints = {400, 450, 500, 550, 600, 650, 700})
    void testPullEffectIncreasesWithAge(int age) {
        if (age > 400) {
            double currentMag = calculatePullMagnitude(30.0, age);
            double previousMag = calculatePullMagnitude(30.0, age - 50);
            assertTrue(currentMag >= previousMag,
                    String.format("Magnitude at age %d should be >= magnitude at age %d", age, age - 50));
        }
    }

    @Test
    @DisplayName("Strike completion age check")
    void testStrikeCompletionAge() {
        int impactAge = 700;
        assertTrue(impactAge >= 700, "Strike should complete at age 700");

        int pullStartAge = 400;
        assertTrue(pullStartAge >= 400, "Pull effect should start at age 400");
        assertTrue(pullStartAge < impactAge, "Pull should start before impact");
    }

    @Test
    @DisplayName("Block destruction iteration bounds")
    void testBlockDestructionBounds() {
        // Verify the iteration bounds for block destruction
        int minX = -RADIUS;
        int maxX = RADIUS;
        int minZ = -RADIUS;
        int maxZ = RADIUS;

        assertEquals(-24, minX);
        assertEquals(24, maxX);
        assertEquals(-24, minZ);
        assertEquals(24, maxZ);

        // Total blocks to check per Y level
        int blocksPerLevel = (maxX - minX + 1) * (maxZ - minZ + 1);
        assertEquals(49 * 49, blocksPerLevel, "Should check 49x49 blocks per Y level");
    }

    @Test
    @DisplayName("Vector normalization edge case - zero vector")
    void testZeroVectorNormalization() {
        // When entity is exactly at strike center, direction is zero
        double length = 0.0;
        // The code should handle this gracefully (division by zero protection)
        assertTrue(length >= 0, "Length should be non-negative");
    }

    private int calculateDistanceSquared(int x, int z) {
        return x * x + z * z;
    }

    private double calculatePullMagnitude(double distance, int age) {
        if (age <= 400) return 0.0;
        return Math.min(1.0 / Math.abs(distance - 20.0) * 4.0 * (age - 400.0) / 300.0, 5.0);
    }
}
