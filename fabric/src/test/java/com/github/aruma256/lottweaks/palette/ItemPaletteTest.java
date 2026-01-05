package com.github.aruma256.lottweaks.palette;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemPaletteTest {

    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    public void resetPalette() {
        // Reset palette state before each test
        ItemPalette.clear(PaletteGroup.PRIMARY);
        ItemPalette.clear(PaletteGroup.SECONDARY);
    }

    @Test
    public void testCanCycleReturnsFalseForEmptyPalette() {
        assertFalse(ItemPalette.canCycle(new ItemStack(Items.STONE), PaletteGroup.PRIMARY));
    }

    @Test
    public void testCanCycleReturnsFalseForNullInput() {
        assertFalse(ItemPalette.canCycle(null, PaletteGroup.PRIMARY));
    }

    @Test
    public void testCanCycleReturnsFalseForEmptyStack() {
        assertFalse(ItemPalette.canCycle(ItemStack.EMPTY, PaletteGroup.PRIMARY));
    }

    @Test
    public void testCanCycleReturnsFalseForAir() {
        assertFalse(ItemPalette.canCycle(new ItemStack(Items.AIR), PaletteGroup.PRIMARY));
    }

    @Test
    public void testCanCycleReturnsTrueForRegisteredItem() {
        Map<Item, Item> chain = new HashMap<>();
        chain.put(Items.STONE, Items.GRANITE);
        chain.put(Items.GRANITE, Items.STONE);
        ItemPalette.load(chain, PaletteGroup.PRIMARY);

        assertTrue(ItemPalette.canCycle(new ItemStack(Items.STONE), PaletteGroup.PRIMARY));
        assertTrue(ItemPalette.canCycle(new ItemStack(Items.GRANITE), PaletteGroup.PRIMARY));
    }

    @Test
    public void testCanCycleReturnsFalseForUnregisteredItem() {
        Map<Item, Item> chain = new HashMap<>();
        chain.put(Items.STONE, Items.GRANITE);
        chain.put(Items.GRANITE, Items.STONE);
        ItemPalette.load(chain, PaletteGroup.PRIMARY);

        assertFalse(ItemPalette.canCycle(new ItemStack(Items.DIORITE), PaletteGroup.PRIMARY));
    }

    @Test
    public void testGetAllCycleItemsReturnsNullForNullInput() {
        assertNull(ItemPalette.getAllCycleItems(null, PaletteGroup.PRIMARY));
    }

    @Test
    public void testGetAllCycleItemsReturnsNullForEmptyStack() {
        assertNull(ItemPalette.getAllCycleItems(ItemStack.EMPTY, PaletteGroup.PRIMARY));
    }

    @Test
    public void testGetAllCycleItemsReturnsNullForUnregisteredItem() {
        assertNull(ItemPalette.getAllCycleItems(new ItemStack(Items.STONE), PaletteGroup.PRIMARY));
    }

    @Test
    public void testGetAllCycleItemsReturnsCircularList() {
        Map<Item, Item> chain = new HashMap<>();
        chain.put(Items.STONE, Items.GRANITE);
        chain.put(Items.GRANITE, Items.DIORITE);
        chain.put(Items.DIORITE, Items.STONE);
        ItemPalette.load(chain, PaletteGroup.PRIMARY);

        List<ItemStack> result = ItemPalette.getAllCycleItems(new ItemStack(Items.STONE), PaletteGroup.PRIMARY);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(Items.STONE, result.get(0).getItem());
        assertEquals(Items.GRANITE, result.get(1).getItem());
        assertEquals(Items.DIORITE, result.get(2).getItem());
    }

    @Test
    public void testGetAllCycleItemsStartsFromGivenItem() {
        Map<Item, Item> chain = new HashMap<>();
        chain.put(Items.STONE, Items.GRANITE);
        chain.put(Items.GRANITE, Items.DIORITE);
        chain.put(Items.DIORITE, Items.STONE);
        ItemPalette.load(chain, PaletteGroup.PRIMARY);

        List<ItemStack> result = ItemPalette.getAllCycleItems(new ItemStack(Items.GRANITE), PaletteGroup.PRIMARY);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(Items.GRANITE, result.get(0).getItem());
        assertEquals(Items.DIORITE, result.get(1).getItem());
        assertEquals(Items.STONE, result.get(2).getItem());
    }

    @Test
    public void testPrimaryAndSecondaryAreIndependent() {
        Map<Item, Item> primaryChain = new HashMap<>();
        primaryChain.put(Items.STONE, Items.GRANITE);
        primaryChain.put(Items.GRANITE, Items.STONE);
        ItemPalette.load(primaryChain, PaletteGroup.PRIMARY);

        Map<Item, Item> secondaryChain = new HashMap<>();
        secondaryChain.put(Items.OAK_PLANKS, Items.SPRUCE_PLANKS);
        secondaryChain.put(Items.SPRUCE_PLANKS, Items.OAK_PLANKS);
        ItemPalette.load(secondaryChain, PaletteGroup.SECONDARY);

        assertTrue(ItemPalette.canCycle(new ItemStack(Items.STONE), PaletteGroup.PRIMARY));
        assertFalse(ItemPalette.canCycle(new ItemStack(Items.STONE), PaletteGroup.SECONDARY));

        assertTrue(ItemPalette.canCycle(new ItemStack(Items.OAK_PLANKS), PaletteGroup.SECONDARY));
        assertFalse(ItemPalette.canCycle(new ItemStack(Items.OAK_PLANKS), PaletteGroup.PRIMARY));
    }

    @Test
    public void testClearRemovesAllItems() {
        Map<Item, Item> chain = new HashMap<>();
        chain.put(Items.STONE, Items.GRANITE);
        chain.put(Items.GRANITE, Items.STONE);
        ItemPalette.load(chain, PaletteGroup.PRIMARY);

        assertTrue(ItemPalette.canCycle(new ItemStack(Items.STONE), PaletteGroup.PRIMARY));

        ItemPalette.clear(PaletteGroup.PRIMARY);

        assertFalse(ItemPalette.canCycle(new ItemStack(Items.STONE), PaletteGroup.PRIMARY));
    }

    @Test
    public void testLoadFromLines() {
        List<String> lines = Arrays.asList(
            "minecraft:stone,minecraft:granite,minecraft:diorite"
        );
        List<String> warnings = ItemPalette.loadFromLines(lines, PaletteGroup.PRIMARY);

        assertTrue(warnings.isEmpty());
        assertTrue(ItemPalette.canCycle(new ItemStack(Items.STONE), PaletteGroup.PRIMARY));
        assertTrue(ItemPalette.canCycle(new ItemStack(Items.GRANITE), PaletteGroup.PRIMARY));
        assertTrue(ItemPalette.canCycle(new ItemStack(Items.DIORITE), PaletteGroup.PRIMARY));
    }
}
