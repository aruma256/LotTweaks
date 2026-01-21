package com.github.aruma256.lottweaks.palette;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.aruma256.lottweaks.MinecraftBootstrapExtension;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

@ExtendWith(MinecraftBootstrapExtension.class)
public class ItemGroupParserTest {

    @Test
    public void testParseValidGroup() {
        List<String> lines = Arrays.asList(
            "minecraft:stone,minecraft:granite,minecraft:diorite"
        );
        ItemGroupParser.ParseResult result = ItemGroupParser.parseGroups(lines);

        assertTrue(result.getWarnings().isEmpty(), "No warnings expected");
        Map<Item, Item> chain = result.getItemChain();
        assertEquals(3, chain.size());
        assertEquals(Items.GRANITE, chain.get(Items.STONE));
        assertEquals(Items.DIORITE, chain.get(Items.GRANITE));
        assertEquals(Items.STONE, chain.get(Items.DIORITE)); // circular
    }

    @Test
    public void testSkipEmptyLines() {
        List<String> lines = Arrays.asList(
            "",
            "minecraft:stone,minecraft:granite",
            ""
        );
        ItemGroupParser.ParseResult result = ItemGroupParser.parseGroups(lines);

        assertTrue(result.getWarnings().isEmpty());
        assertEquals(2, result.getItemChain().size());
    }

    @Test
    public void testSkipCommentLines() {
        List<String> lines = Arrays.asList(
            "//This is a comment",
            "minecraft:stone,minecraft:granite",
            "//Another comment"
        );
        ItemGroupParser.ParseResult result = ItemGroupParser.parseGroups(lines);

        assertTrue(result.getWarnings().isEmpty());
        assertEquals(2, result.getItemChain().size());
    }

    @Test
    public void testDetectDuplicateItemsWithinSameLine() {
        List<String> lines = Arrays.asList(
            "minecraft:stone,minecraft:granite,minecraft:stone"
        );
        ItemGroupParser.ParseResult result = ItemGroupParser.parseGroups(lines);

        assertFalse(result.getWarnings().isEmpty(), "Should warn about duplicate");
        assertTrue(result.getWarnings().get(0).contains("duplicated"));
    }

    @Test
    public void testDetectDuplicateItemsAcrossLines() {
        List<String> lines = Arrays.asList(
            "minecraft:stone,minecraft:granite",
            "minecraft:diorite,minecraft:stone"
        );
        ItemGroupParser.ParseResult result = ItemGroupParser.parseGroups(lines);

        assertFalse(result.getWarnings().isEmpty(), "Should warn about duplicate");
        assertTrue(result.getWarnings().get(0).contains("duplicated"));
    }

    @Test
    public void testInvalidItemIdWarning() {
        List<String> lines = Arrays.asList(
            "minecraft:stone,minecraft:invalid_item_xyz,minecraft:granite"
        );
        ItemGroupParser.ParseResult result = ItemGroupParser.parseGroups(lines);

        assertFalse(result.getWarnings().isEmpty(), "Should warn about invalid item");
        assertTrue(result.getWarnings().get(0).contains("not supported"));
        // Valid items should still be parsed
        assertEquals(2, result.getItemChain().size());
    }

    @Test
    public void testGroupSizeOneWarning() {
        List<String> lines = Arrays.asList(
            "minecraft:stone"
        );
        ItemGroupParser.ParseResult result = ItemGroupParser.parseGroups(lines);

        assertFalse(result.getWarnings().isEmpty(), "Should warn about group size");
        assertTrue(result.getWarnings().get(0).contains("size is 1"));
        assertTrue(result.getItemChain().isEmpty(), "Group with size 1 should not be added");
    }

    @Test
    public void testGroupSizeZeroAfterInvalidItems() {
        List<String> lines = Arrays.asList(
            "minecraft:invalid_item_a,minecraft:invalid_item_b"
        );
        ItemGroupParser.ParseResult result = ItemGroupParser.parseGroups(lines);

        // Should warn about invalid items and group size
        assertTrue(result.getWarnings().size() >= 2);
        assertTrue(result.getItemChain().isEmpty());
    }

    @Test
    public void testEmptyInput() {
        List<String> lines = Collections.emptyList();
        ItemGroupParser.ParseResult result = ItemGroupParser.parseGroups(lines);

        assertTrue(result.getWarnings().isEmpty());
        assertTrue(result.getItemChain().isEmpty());
    }

    @Test
    public void testMultipleValidGroups() {
        List<String> lines = Arrays.asList(
            "minecraft:stone,minecraft:granite",
            "minecraft:oak_planks,minecraft:spruce_planks,minecraft:birch_planks"
        );
        ItemGroupParser.ParseResult result = ItemGroupParser.parseGroups(lines);

        assertTrue(result.getWarnings().isEmpty());
        assertEquals(5, result.getItemChain().size());
        // Check first group
        assertEquals(Items.GRANITE, result.getItemChain().get(Items.STONE));
        assertEquals(Items.STONE, result.getItemChain().get(Items.GRANITE));
        // Check second group
        assertEquals(Items.SPRUCE_PLANKS, result.getItemChain().get(Items.OAK_PLANKS));
        assertEquals(Items.BIRCH_PLANKS, result.getItemChain().get(Items.SPRUCE_PLANKS));
        assertEquals(Items.OAK_PLANKS, result.getItemChain().get(Items.BIRCH_PLANKS));
    }

    @Test
    public void testWarningIncludesLineNumber() {
        List<String> lines = Arrays.asList(
            "//comment",
            "minecraft:stone",
            "minecraft:invalid_xyz"
        );
        ItemGroupParser.ParseResult result = ItemGroupParser.parseGroups(lines);

        assertFalse(result.getWarnings().isEmpty());
        // Line numbers should be included
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("Line 2")));
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("Line 3")));
    }
}
