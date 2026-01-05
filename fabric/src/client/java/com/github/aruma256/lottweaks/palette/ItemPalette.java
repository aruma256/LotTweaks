package com.github.aruma256.lottweaks.palette;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.aruma256.lottweaks.LotTweaks;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemPalette {

    private static final int MAX_CYCLE_ITERATIONS = 50000;

    private static final Map<Item, Item> ITEM_CHAIN_PRIMARY = new HashMap<>();
    private static final Map<Item, Item> ITEM_CHAIN_SECONDARY = new HashMap<>();

    private static Map<Item, Item> getItemChain(PaletteGroup group) {
        return (group == PaletteGroup.PRIMARY) ? ITEM_CHAIN_PRIMARY : ITEM_CHAIN_SECONDARY;
    }

    public static boolean canCycle(ItemStack itemStack, PaletteGroup group) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        Item item = itemStack.getItem();
        if (item == null || item == Items.AIR) {
            return false;
        }
        return getItemChain(group).containsKey(item);
    }

    public static List<ItemStack> getAllCycleItems(ItemStack itemStack, PaletteGroup group) {
        if (itemStack == null || itemStack.isEmpty()) {
            return null;
        }
        Item srcItem = itemStack.getItem();
        if (srcItem == null || srcItem == Items.AIR) {
            return null;
        }
        Map<Item, Item> chain = getItemChain(group);
        if (!chain.containsKey(srcItem)) {
            return null;
        }

        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(itemStack);

        Item item = chain.get(srcItem);
        int counter = 0;
        while (item != srcItem) {
            stacks.add(new ItemStack(item));
            item = chain.get(item);
            counter++;
            if (counter >= MAX_CYCLE_ITERATIONS) {
                LotTweaks.LOGGER.error("infinite loop detected in palette cycle!");
                return null;
            }
        }
        return stacks;
    }

    public static void load(Map<Item, Item> itemChain, PaletteGroup group) {
        Map<Item, Item> chain = getItemChain(group);
        chain.clear();
        chain.putAll(itemChain);
    }

    public static void clear(PaletteGroup group) {
        getItemChain(group).clear();
    }

    public static List<String> loadFromLines(List<String> lines, PaletteGroup group) {
        ItemGroupParser.ParseResult result = ItemGroupParser.parseGroups(lines);
        load(result.getItemChain(), group);
        return result.getWarnings();
    }
}
