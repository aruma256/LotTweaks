package com.github.aruma256.lottweaks.palette;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.github.aruma256.lottweaks.MinecraftBootstrapExtension;
import com.google.gson.JsonParser;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

@ExtendWith(MinecraftBootstrapExtension.class)
public class ItemGroupsConfigLoaderTest {

    @TempDir
    File tempDir;

    // Small numbers must keep their declared types, even when a byte could hold them.
    private static final String TYPED_CUSTOM_DATA = """
            {byte:1b,short:2s,int:123,long:4L,float:5.0f,double:6.0d,
             bytes:[B;1b,2b],ints:[I;1,2,3],longs:[L;1L,2L],
             nested:{values:[1,2,3]},emptyInts:[I;]}
            """;

    @Test
    public void testLoad_parsesBooleanComponent() throws IOException {
        writeConfigFile("""
                {"config_version":1,"groups":[[[
                  {"id":"minecraft:stone","components":"{'minecraft:enchantment_glint_override':1b}"},
                  {"id":"minecraft:granite"}
                ]]]}
                """);

        var result = ItemGroupsConfigLoader.load(tempDir);
        assertTrue(result.getWarnings().isEmpty(), result.getWarnings().toString());
        ItemStack expected = new ItemStack(Items.STONE);
        expected.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        assertEquals(new ItemState(expected), result.getGroups().getFirst().getFirst().getFirst());
    }

    @Test
    public void testSaveAndLoad_roundTripsBooleanComponent() {
        ItemStack stack = new ItemStack(Items.STONE);
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        ItemState expected = new ItemState(stack);
        var groups = List.of(List.of(List.of(expected, new ItemState(new ItemStack(Items.GRANITE)))));

        ItemGroupsConfigLoader.save(tempDir, groups);
        var result = ItemGroupsConfigLoader.load(tempDir);
        assertTrue(result.getWarnings().isEmpty(), result.getWarnings().toString());
        assertEquals(expected, result.getGroups().getFirst().getFirst().getFirst());
    }

    @Test
    public void testLoad_preservesCustomDataNbtTypes() throws Exception {
        writeConfigFile("""
                {"config_version":1,"groups":[[[
                  {"id":"minecraft:stone","components":"{'minecraft:custom_data':%s}"},
                  {"id":"minecraft:granite"}
                ]]]}
                """.formatted(TYPED_CUSTOM_DATA.replace("\n", "")));

        var result = ItemGroupsConfigLoader.load(tempDir);
        assertTrue(result.getWarnings().isEmpty(), result.getWarnings().toString());
        ItemStack expected = new ItemStack(Items.STONE);
        expected.set(DataComponents.CUSTOM_DATA, CustomData.of(TagParser.parseCompoundFully(TYPED_CUSTOM_DATA)));
        assertEquals(new ItemState(expected), result.getGroups().getFirst().getFirst().getFirst());
    }

    @Test
    public void testSaveAndLoad_preservesCustomDataNbtTypes() throws Exception {
        CompoundTag data = TagParser.parseCompoundFully(TYPED_CUSTOM_DATA);
        ItemStack stack = new ItemStack(Items.STONE);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        ItemState expected = new ItemState(stack);
        var groups = List.of(List.of(List.of(expected, new ItemState(new ItemStack(Items.GRANITE)))));

        ItemGroupsConfigLoader.save(tempDir, groups);
        // Inspect the file separately: reciprocal conversion bugs must not pass a round trip.
        var root = JsonParser.parseString(Files.readString(
                new File(tempDir, ItemGroupsConfigLoader.CONFIG_FILE_NAME).toPath())).getAsJsonObject();
        String components = root.getAsJsonArray("groups").get(0).getAsJsonArray()
                .get(0).getAsJsonArray().get(0).getAsJsonObject().get("components").getAsString();
        CompoundTag savedPatch = TagParser.parseCompoundFully(components);
        assertEquals(data, savedPatch.get("minecraft:custom_data"));

        var result = ItemGroupsConfigLoader.load(tempDir);
        assertTrue(result.getWarnings().isEmpty(), result.getWarnings().toString());
        assertEquals(expected, result.getGroups().getFirst().getFirst().getFirst());
    }

    @Test
    public void testConfigExists_returnsFalseWhenNoFile() {
        assertFalse(ItemGroupsConfigLoader.configExists(tempDir));
    }

    @Test
    public void testConfigExists_returnsTrueWhenFileExists() throws IOException {
        File configFile = new File(tempDir, ItemGroupsConfigLoader.CONFIG_FILE_NAME);
        configFile.createNewFile();
        assertTrue(ItemGroupsConfigLoader.configExists(tempDir));
    }

    @Test
    public void testLoad_returnsWarningWhenFileNotFound() {
        ItemGroupsConfigLoader.LoadResult result = ItemGroupsConfigLoader.load(tempDir);
        assertFalse(result.getWarnings().isEmpty());
        assertTrue(result.getGroups().isEmpty());
    }

    @Test
    public void testLoad_parsesValidJson() throws IOException {
        String json = """
            {
              "config_version": 1,
              "groups": [
                [
                  [{"id": "minecraft:stone"}, {"id": "minecraft:granite"}, {"id": "minecraft:diorite"}]
                ]
              ]
            }
            """;
        writeConfigFile(json);

        ItemGroupsConfigLoader.LoadResult result = ItemGroupsConfigLoader.load(tempDir);

        assertTrue(result.getWarnings().isEmpty(), "Warnings: " + result.getWarnings());
        assertEquals(1, result.getGroups().size());
        assertEquals(1, result.getGroups().get(0).size());
        assertEquals(3, result.getGroups().get(0).get(0).size());
    }

    @Test
    public void testLoad_parsesMultipleGroups() throws IOException {
        String json = """
            {
              "config_version": 1,
              "groups": [
                [
                  [{"id": "minecraft:stone"}, {"id": "minecraft:granite"}]
                ],
                [
                  [{"id": "minecraft:oak_planks"}, {"id": "minecraft:spruce_planks"}]
                ]
              ]
            }
            """;
        writeConfigFile(json);

        ItemGroupsConfigLoader.LoadResult result = ItemGroupsConfigLoader.load(tempDir);

        assertTrue(result.getWarnings().isEmpty(), "Warnings: " + result.getWarnings());
        assertEquals(2, result.getGroups().size());
    }

    @Test
    public void testLoad_parsesMultipleCyclesInGroup() throws IOException {
        String json = """
            {
              "config_version": 1,
              "groups": [
                [
                  [{"id": "minecraft:stone"}, {"id": "minecraft:granite"}],
                  [{"id": "minecraft:oak_planks"}, {"id": "minecraft:spruce_planks"}]
                ]
              ]
            }
            """;
        writeConfigFile(json);

        ItemGroupsConfigLoader.LoadResult result = ItemGroupsConfigLoader.load(tempDir);

        assertTrue(result.getWarnings().isEmpty(), "Warnings: " + result.getWarnings());
        assertEquals(1, result.getGroups().size());
        assertEquals(2, result.getGroups().get(0).size());
    }

    @Test
    public void testLoad_warnsOnInvalidItemId() throws IOException {
        String json = """
            {
              "config_version": 1,
              "groups": [
                [
                  [{"id": "minecraft:stone"}, {"id": "minecraft:invalid_item_xyz"}, {"id": "minecraft:granite"}]
                ]
              ]
            }
            """;
        writeConfigFile(json);

        ItemGroupsConfigLoader.LoadResult result = ItemGroupsConfigLoader.load(tempDir);

        assertFalse(result.getWarnings().isEmpty());
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("invalid_item_xyz")));
        // Cycle should still be created with valid items (stone, granite = 2 items)
        assertEquals(1, result.getGroups().get(0).size());
        assertEquals(2, result.getGroups().get(0).get(0).size());
    }

    @Test
    public void testLoad_warnsOnDuplicateItemInCycle() throws IOException {
        String json = """
            {
              "config_version": 1,
              "groups": [
                [
                  [{"id": "minecraft:stone"}, {"id": "minecraft:granite"}, {"id": "minecraft:stone"}]
                ]
              ]
            }
            """;
        writeConfigFile(json);

        ItemGroupsConfigLoader.LoadResult result = ItemGroupsConfigLoader.load(tempDir);

        assertFalse(result.getWarnings().isEmpty());
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("duplicated")));
    }

    @Test
    public void testLoad_warnsOnDuplicateItemAcrossCyclesInSameGroup() throws IOException {
        // Same item (stone) appears in two different cycles within the same group
        String json = """
            {
              "config_version": 1,
              "groups": [
                [
                  [{"id": "minecraft:stone"}, {"id": "minecraft:granite"}],
                  [{"id": "minecraft:stone"}, {"id": "minecraft:diorite"}]
                ]
              ]
            }
            """;
        writeConfigFile(json);

        ItemGroupsConfigLoader.LoadResult result = ItemGroupsConfigLoader.load(tempDir);

        assertFalse(result.getWarnings().isEmpty());
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("stone") && w.contains("duplicated")));
    }

    @Test
    public void testLoad_allowsSameItemInDifferentGroups() throws IOException {
        // Same item (stone) appears in different groups - this should be allowed
        String json = """
            {
              "config_version": 1,
              "groups": [
                [
                  [{"id": "minecraft:stone"}, {"id": "minecraft:granite"}]
                ],
                [
                  [{"id": "minecraft:stone"}, {"id": "minecraft:diorite"}]
                ]
              ]
            }
            """;
        writeConfigFile(json);

        ItemGroupsConfigLoader.LoadResult result = ItemGroupsConfigLoader.load(tempDir);

        assertTrue(result.getWarnings().isEmpty(), "Should allow same item in different groups. Warnings: " + result.getWarnings());
        assertEquals(2, result.getGroups().size());
        // Both groups should have their cycles intact
        assertEquals(1, result.getGroups().get(0).size());
        assertEquals(1, result.getGroups().get(1).size());
    }

    @Test
    public void testLoad_warnsOnCycleWithLessThan2Items() throws IOException {
        String json = """
            {
              "config_version": 1,
              "groups": [
                [
                  [{"id": "minecraft:stone"}]
                ]
              ]
            }
            """;
        writeConfigFile(json);

        ItemGroupsConfigLoader.LoadResult result = ItemGroupsConfigLoader.load(tempDir);

        assertFalse(result.getWarnings().isEmpty());
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("less than 2")));
        // Empty cycles should not be added
        assertTrue(result.getGroups().get(0).isEmpty());
    }

    @Test
    public void testLoad_warnsOnMissingId() throws IOException {
        String json = """
            {
              "config_version": 1,
              "groups": [
                [
                  [{"id": "minecraft:stone"}, {"name": "no id field"}, {"id": "minecraft:granite"}]
                ]
              ]
            }
            """;
        writeConfigFile(json);

        ItemGroupsConfigLoader.LoadResult result = ItemGroupsConfigLoader.load(tempDir);

        assertFalse(result.getWarnings().isEmpty());
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("missing 'id'")));
    }

    @Test
    public void testSave_createsValidJson() throws IOException {
        List<List<List<ItemState>>> groups = new ArrayList<>();

        List<List<ItemState>> group0 = new ArrayList<>();
        List<ItemState> cycle0 = new ArrayList<>();
        cycle0.add(new ItemState(new ItemStack(Items.STONE)));
        cycle0.add(new ItemState(new ItemStack(Items.GRANITE)));
        group0.add(cycle0);
        groups.add(group0);

        ItemGroupsConfigLoader.save(tempDir, groups);

        assertTrue(ItemGroupsConfigLoader.configExists(tempDir));

        // Load it back and verify
        ItemGroupsConfigLoader.LoadResult result = ItemGroupsConfigLoader.load(tempDir);
        assertTrue(result.getWarnings().isEmpty(), "Warnings: " + result.getWarnings());
        assertEquals(1, result.getGroups().size());
        assertEquals(1, result.getGroups().get(0).size());
        assertEquals(2, result.getGroups().get(0).get(0).size());
    }

    @Test
    public void testSaveAndLoad_roundTrip() throws IOException {
        List<List<List<ItemState>>> originalGroups = new ArrayList<>();

        // Group 0 with 2 cycles
        List<List<ItemState>> group0 = new ArrayList<>();
        List<ItemState> cycle0_0 = new ArrayList<>();
        cycle0_0.add(new ItemState(new ItemStack(Items.STONE)));
        cycle0_0.add(new ItemState(new ItemStack(Items.GRANITE)));
        cycle0_0.add(new ItemState(new ItemStack(Items.DIORITE)));
        group0.add(cycle0_0);

        List<ItemState> cycle0_1 = new ArrayList<>();
        cycle0_1.add(new ItemState(new ItemStack(Items.OAK_PLANKS)));
        cycle0_1.add(new ItemState(new ItemStack(Items.SPRUCE_PLANKS)));
        group0.add(cycle0_1);
        originalGroups.add(group0);

        // Group 1 with 1 cycle
        List<List<ItemState>> group1 = new ArrayList<>();
        List<ItemState> cycle1_0 = new ArrayList<>();
        cycle1_0.add(new ItemState(new ItemStack(Items.WHITE_WOOL)));
        cycle1_0.add(new ItemState(new ItemStack(Items.ORANGE_WOOL)));
        group1.add(cycle1_0);
        originalGroups.add(group1);

        // Save
        ItemGroupsConfigLoader.save(tempDir, originalGroups);

        // Load back
        ItemGroupsConfigLoader.LoadResult result = ItemGroupsConfigLoader.load(tempDir);

        assertTrue(result.getWarnings().isEmpty(), "Warnings: " + result.getWarnings());
        assertEquals(2, result.getGroups().size());
        assertEquals(2, result.getGroups().get(0).size());
        assertEquals(1, result.getGroups().get(1).size());

        // Verify items
        assertEquals(Items.STONE, result.getGroups().get(0).get(0).get(0).toItemStack().getItem());
        assertEquals(Items.GRANITE, result.getGroups().get(0).get(0).get(1).toItemStack().getItem());
        assertEquals(Items.DIORITE, result.getGroups().get(0).get(0).get(2).toItemStack().getItem());
    }

    @Test
    public void testCreateDefaultGroups_returnsTwoEmptyGroups() {
        List<List<List<ItemState>>> defaults = ItemGroupsConfigLoader.createDefaultGroups();
        assertEquals(2, defaults.size());
        assertTrue(defaults.get(0).isEmpty());
        assertTrue(defaults.get(1).isEmpty());
    }

    private void writeConfigFile(String content) throws IOException {
        File configFile = new File(tempDir, ItemGroupsConfigLoader.CONFIG_FILE_NAME);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            writer.write(content);
        }
    }
}
