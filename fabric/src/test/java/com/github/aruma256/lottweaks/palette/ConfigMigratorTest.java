package com.github.aruma256.lottweaks.palette;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;

public class ConfigMigratorTest {

    @TempDir
    File tempDir;

    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    public void testHasLegacyConfig_returnsFalseWhenNoFiles() {
        assertFalse(ConfigMigrator.hasLegacyConfig(tempDir));
    }

    @Test
    public void testHasLegacyConfig_returnsTrueWhenPrimaryExists() throws IOException {
        createLegacyFile(ConfigMigrator.LEGACY_PRIMARY, "minecraft:stone,minecraft:granite");
        assertTrue(ConfigMigrator.hasLegacyConfig(tempDir));
    }

    @Test
    public void testHasLegacyConfig_returnsTrueWhenSecondaryExists() throws IOException {
        createLegacyFile(ConfigMigrator.LEGACY_SECONDARY, "minecraft:stone,minecraft:granite");
        assertTrue(ConfigMigrator.hasLegacyConfig(tempDir));
    }

    @Test
    public void testMigrate_parsesLegacyPrimaryFile() throws IOException {
        createLegacyFile(ConfigMigrator.LEGACY_PRIMARY,
                "minecraft:stone,minecraft:granite,minecraft:diorite\n" +
                "minecraft:oak_planks,minecraft:spruce_planks");

        ConfigMigrator.MigrationResult result = ConfigMigrator.migrate(tempDir);

        assertTrue(result.wasMigrated());
        assertTrue(result.getWarnings().isEmpty(), "Warnings: " + result.getWarnings());
        assertEquals(2, result.getGroups().size());

        // Group 0 (PRIMARY) should have 2 cycles
        List<List<ItemState>> primaryCycles = result.getGroups().get(0);
        assertEquals(2, primaryCycles.size());
        assertEquals(3, primaryCycles.get(0).size()); // stone, granite, diorite
        assertEquals(2, primaryCycles.get(1).size()); // oak_planks, spruce_planks
    }

    @Test
    public void testMigrate_parsesLegacySecondaryFile() throws IOException {
        createLegacyFile(ConfigMigrator.LEGACY_SECONDARY,
                "minecraft:white_wool,minecraft:orange_wool");

        ConfigMigrator.MigrationResult result = ConfigMigrator.migrate(tempDir);

        assertTrue(result.wasMigrated());
        assertEquals(2, result.getGroups().size());

        // Group 0 (PRIMARY) should be empty
        assertTrue(result.getGroups().get(0).isEmpty());

        // Group 1 (SECONDARY) should have 1 cycle
        List<List<ItemState>> secondaryCycles = result.getGroups().get(1);
        assertEquals(1, secondaryCycles.size());
        assertEquals(2, secondaryCycles.get(0).size());
    }

    @Test
    public void testMigrate_parsesBothFiles() throws IOException {
        createLegacyFile(ConfigMigrator.LEGACY_PRIMARY,
                "minecraft:stone,minecraft:granite");
        createLegacyFile(ConfigMigrator.LEGACY_SECONDARY,
                "minecraft:white_wool,minecraft:orange_wool");

        ConfigMigrator.MigrationResult result = ConfigMigrator.migrate(tempDir);

        assertTrue(result.wasMigrated());
        assertEquals(2, result.getGroups().size());
        assertEquals(1, result.getGroups().get(0).size()); // PRIMARY has 1 cycle
        assertEquals(1, result.getGroups().get(1).size()); // SECONDARY has 1 cycle
    }

    @Test
    public void testMigrate_skipsComments() throws IOException {
        createLegacyFile(ConfigMigrator.LEGACY_PRIMARY,
                "//This is a comment\n" +
                "minecraft:stone,minecraft:granite\n" +
                "//Another comment\n" +
                "minecraft:oak_planks,minecraft:spruce_planks");

        ConfigMigrator.MigrationResult result = ConfigMigrator.migrate(tempDir);

        assertTrue(result.getWarnings().isEmpty(), "Warnings: " + result.getWarnings());
        assertEquals(2, result.getGroups().get(0).size());
    }

    @Test
    public void testMigrate_skipsEmptyLines() throws IOException {
        createLegacyFile(ConfigMigrator.LEGACY_PRIMARY,
                "minecraft:stone,minecraft:granite\n" +
                "\n" +
                "minecraft:oak_planks,minecraft:spruce_planks\n" +
                "\n");

        ConfigMigrator.MigrationResult result = ConfigMigrator.migrate(tempDir);

        assertTrue(result.getWarnings().isEmpty(), "Warnings: " + result.getWarnings());
        assertEquals(2, result.getGroups().get(0).size());
    }

    @Test
    public void testMigrate_warnsOnInvalidItem() throws IOException {
        createLegacyFile(ConfigMigrator.LEGACY_PRIMARY,
                "minecraft:stone,minecraft:invalid_item,minecraft:granite");

        ConfigMigrator.MigrationResult result = ConfigMigrator.migrate(tempDir);

        assertFalse(result.getWarnings().isEmpty());
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("invalid_item")));
        // Should still have the valid items
        assertEquals(1, result.getGroups().get(0).size());
        assertEquals(2, result.getGroups().get(0).get(0).size());
    }

    @Test
    public void testMigrate_warnsOnDuplicate() throws IOException {
        createLegacyFile(ConfigMigrator.LEGACY_PRIMARY,
                "minecraft:stone,minecraft:granite,minecraft:stone");

        ConfigMigrator.MigrationResult result = ConfigMigrator.migrate(tempDir);

        assertFalse(result.getWarnings().isEmpty());
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("duplicated")));
    }

    @Test
    public void testMigrate_warnsOnSmallGroup() throws IOException {
        createLegacyFile(ConfigMigrator.LEGACY_PRIMARY,
                "minecraft:stone");

        ConfigMigrator.MigrationResult result = ConfigMigrator.migrate(tempDir);

        assertFalse(result.getWarnings().isEmpty());
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("less than 2")));
        assertTrue(result.getGroups().get(0).isEmpty());
    }

    @Test
    public void testMigrate_preservesCorrectItems() throws IOException {
        createLegacyFile(ConfigMigrator.LEGACY_PRIMARY,
                "minecraft:stone,minecraft:granite,minecraft:diorite");

        ConfigMigrator.MigrationResult result = ConfigMigrator.migrate(tempDir);

        List<ItemState> cycle = result.getGroups().get(0).get(0);
        assertEquals(Items.STONE, cycle.get(0).toItemStack().getItem());
        assertEquals(Items.GRANITE, cycle.get(1).toItemStack().getItem());
        assertEquals(Items.DIORITE, cycle.get(2).toItemStack().getItem());
    }

    private void createLegacyFile(String fileName, String content) throws IOException {
        File file = new File(tempDir, fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        }
    }
}
