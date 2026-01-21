package com.github.aruma256.lottweaks.keybinding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.aruma256.lottweaks.MinecraftBootstrapExtension;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MinecraftBootstrapExtension.class)
public class PickHistoryTest {

    private PickHistory pickHistory;

    @BeforeEach
    public void setup() {
        pickHistory = new PickHistory();
    }

    @Test
    public void testIsEmptyWhenNew() {
        assertTrue(pickHistory.isEmpty());
    }

    @Test
    public void testIsNotEmptyAfterAdd() {
        pickHistory.add(new ItemStack(Items.STONE));
        assertFalse(pickHistory.isEmpty());
    }

    @Test
    public void testGetAllReturnsAddedItems() {
        ItemStack stone = new ItemStack(Items.STONE);
        ItemStack dirt = new ItemStack(Items.DIRT);

        pickHistory.add(stone);
        pickHistory.add(dirt);

        List<ItemStack> all = pickHistory.getAll();
        assertEquals(2, all.size());
        assertTrue(ItemStack.matches(dirt, all.get(0)));
        assertTrue(ItemStack.matches(stone, all.get(1)));
    }

    @Test
    public void testHistorySizeLimit() {
        for (int i = 0; i < PickHistory.HISTORY_SIZE + 5; i++) {
            pickHistory.add(new ItemStack(Items.STONE, i + 1));
        }

        List<ItemStack> all = pickHistory.getAll();
        assertEquals(PickHistory.HISTORY_SIZE, all.size());
    }

    @Test
    public void testAddNullDoesNothing() {
        pickHistory.add(null);
        assertTrue(pickHistory.isEmpty());
    }

    @Test
    public void testAddEmptyStackDoesNothing() {
        pickHistory.add(ItemStack.EMPTY);
        assertTrue(pickHistory.isEmpty());
    }

    @Test
    public void testDuplicateItemsAreDeduped() {
        ItemStack stone1 = new ItemStack(Items.STONE);
        ItemStack dirt = new ItemStack(Items.DIRT);
        ItemStack stone2 = new ItemStack(Items.STONE);

        pickHistory.add(stone1);
        pickHistory.add(dirt);
        pickHistory.add(stone2);

        List<ItemStack> all = pickHistory.getAll();
        assertEquals(2, all.size());
        assertTrue(ItemStack.matches(stone2, all.get(0)));
        assertTrue(ItemStack.matches(dirt, all.get(1)));
    }

    @Test
    public void testMostRecentItemIsFirst() {
        pickHistory.add(new ItemStack(Items.STONE));
        pickHistory.add(new ItemStack(Items.DIRT));
        pickHistory.add(new ItemStack(Items.COBBLESTONE));

        List<ItemStack> all = pickHistory.getAll();
        assertEquals(Items.COBBLESTONE, all.get(0).getItem());
        assertEquals(Items.DIRT, all.get(1).getItem());
        assertEquals(Items.STONE, all.get(2).getItem());
    }
}
