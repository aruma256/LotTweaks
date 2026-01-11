package com.github.aruma256.lottweaks.palette;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.aruma256.lottweaks.LotTweaks;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemPalette {

    private static final int MAX_CYCLE_ITERATIONS = 50000;

    // Cache for lookups: each group index maps to a Map<ItemState, List<ItemState>>
    private static final List<Map<ItemState, List<ItemState>>> GROUP_CACHES = new ArrayList<>();

    // Raw group data for serialization
    private static List<List<List<ItemState>>> groupData = new ArrayList<>();

    public static int getGroupCount() {
        return GROUP_CACHES.size();
    }

    public static boolean canCycle(ItemStack itemStack, int groupIndex) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        if (itemStack.getItem() == null || itemStack.getItem() == Items.AIR) {
            return false;
        }
        if (groupIndex < 0 || groupIndex >= GROUP_CACHES.size()) {
            return false;
        }

        Map<ItemState, List<ItemState>> cache = GROUP_CACHES.get(groupIndex);
        ItemState key = new ItemState(itemStack);

        // Try exact match first
        if (cache.containsKey(key)) {
            return true;
        }

        // Try fallback without components
        ItemState fallbackKey = key.withoutComponents();
        return cache.containsKey(fallbackKey);
    }

    public static List<ItemStack> getAllCycleItems(ItemStack itemStack, int groupIndex) {
        if (itemStack == null || itemStack.isEmpty()) {
            return null;
        }
        if (itemStack.getItem() == null || itemStack.getItem() == Items.AIR) {
            return null;
        }
        if (groupIndex < 0 || groupIndex >= GROUP_CACHES.size()) {
            return null;
        }

        Map<ItemState, List<ItemState>> cache = GROUP_CACHES.get(groupIndex);
        ItemState key = new ItemState(itemStack);

        // Stage 1: Try exact match (with components)
        List<ItemState> cycle = cache.get(key);

        // Stage 2: Fallback without components
        if (cycle == null) {
            key = key.withoutComponents();
            cycle = cache.get(key);
        }

        if (cycle == null) {
            return null;
        }

        // Find the starting position and build the result list
        return buildCycleListFromState(cycle, key, itemStack);
    }

    private static List<ItemStack> buildCycleListFromState(List<ItemState> cycle, ItemState startKey, ItemStack originalStack) {
        List<ItemStack> result = new ArrayList<>();

        // Find the starting index
        int startIndex = -1;
        for (int i = 0; i < cycle.size(); i++) {
            if (cycle.get(i).equals(startKey)) {
                startIndex = i;
                break;
            }
        }

        if (startIndex == -1) {
            // Key not found in cycle, shouldn't happen but handle gracefully
            startIndex = 0;
        }

        // Add the original stack first (preserving its components if any)
        result.add(originalStack);

        // Add remaining items in order
        for (int i = 1; i < cycle.size(); i++) {
            int index = (startIndex + i) % cycle.size();
            result.add(cycle.get(index).toItemStack());
        }

        return result;
    }

    public static void loadGroups(List<List<List<ItemState>>> groups) {
        groupData = groups;
        GROUP_CACHES.clear();

        for (List<List<ItemState>> cyclesInGroup : groups) {
            Map<ItemState, List<ItemState>> cache = new HashMap<>();

            for (List<ItemState> cycle : cyclesInGroup) {
                if (cycle.size() < 2) {
                    continue;
                }

                // Register each item in the cycle
                for (ItemState itemState : cycle) {
                    if (cache.containsKey(itemState)) {
                        LotTweaks.LOGGER.warn("Duplicate item in palette: {}", itemState);
                        continue;
                    }
                    cache.put(itemState, cycle);
                }
            }

            GROUP_CACHES.add(cache);
        }
    }

    public static List<List<List<ItemState>>> getGroupData() {
        return groupData;
    }

    public static void clear() {
        GROUP_CACHES.clear();
        groupData = new ArrayList<>();
    }

    public static void clearGroup(int groupIndex) {
        if (groupIndex >= 0 && groupIndex < GROUP_CACHES.size()) {
            GROUP_CACHES.get(groupIndex).clear();
        }
        if (groupIndex >= 0 && groupIndex < groupData.size()) {
            groupData.get(groupIndex).clear();
        }
    }

    // --- Deprecated methods for backward compatibility during migration ---

    @Deprecated
    public static boolean canCycle(ItemStack itemStack, PaletteGroup group) {
        return canCycle(itemStack, group.ordinal());
    }

    @Deprecated
    public static List<ItemStack> getAllCycleItems(ItemStack itemStack, PaletteGroup group) {
        return getAllCycleItems(itemStack, group.ordinal());
    }

    @Deprecated
    public static void clear(PaletteGroup group) {
        clearGroup(group.ordinal());
    }

    @Deprecated
    public static void load(Map<net.minecraft.world.item.Item, net.minecraft.world.item.Item> itemChain, PaletteGroup group) {
        // Convert old format to new format
        int groupIndex = group.ordinal();

        // Ensure we have enough groups
        while (groupData.size() <= groupIndex) {
            groupData.add(new ArrayList<>());
        }
        while (GROUP_CACHES.size() <= groupIndex) {
            GROUP_CACHES.add(new HashMap<>());
        }

        groupData.get(groupIndex).clear();
        GROUP_CACHES.get(groupIndex).clear();

        // Convert chain map to cycles
        Map<net.minecraft.world.item.Item, Boolean> visited = new HashMap<>();
        for (net.minecraft.world.item.Item startItem : itemChain.keySet()) {
            if (visited.containsKey(startItem)) {
                continue;
            }

            List<ItemState> cycle = new ArrayList<>();
            net.minecraft.world.item.Item current = startItem;
            int counter = 0;

            while (!visited.containsKey(current) && counter < MAX_CYCLE_ITERATIONS) {
                visited.put(current, true);
                cycle.add(new ItemState(new ItemStack(current)));
                current = itemChain.get(current);
                if (current == null) break;
                counter++;
            }

            if (cycle.size() >= 2) {
                groupData.get(groupIndex).add(cycle);
                for (ItemState itemState : cycle) {
                    GROUP_CACHES.get(groupIndex).put(itemState, cycle);
                }
            }
        }
    }

    @Deprecated
    public static List<String> loadFromLines(List<String> lines, PaletteGroup group) {
        ItemGroupParser.ParseResult result = ItemGroupParser.parseGroups(lines);
        load(result.getItemChain(), group);
        return result.getWarnings();
    }
}
