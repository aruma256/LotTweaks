package com.github.aruma256.lottweaks.palette;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ItemGroupParser {

    public static class ParseResult {
        private final Map<Item, Item> itemChain;
        private final List<String> warnings;

        public ParseResult(Map<Item, Item> itemChain, List<String> warnings) {
            this.itemChain = itemChain;
            this.warnings = warnings;
        }

        public Map<Item, Item> getItemChain() {
            return itemChain;
        }

        public List<String> getWarnings() {
            return warnings;
        }
    }

    public static ParseResult parseGroups(List<String> lines) {
        Map<Item, Item> itemChain = new HashMap<>();
        List<String> warnings = new ArrayList<>();

        int lineNumber = 0;
        for (String line : lines) {
            lineNumber++;
            if (line.isEmpty() || line.startsWith("//")) {
                continue;
            }

            List<Item> items = new ArrayList<>();
            for (String itemStr : line.split(",")) {
                itemStr = itemStr.trim();
                if (itemStr.isEmpty()) {
                    continue;
                }

                Identifier resourceLocation = Identifier.parse(itemStr);
                Optional<Holder.Reference<Item>> itemHolder = BuiltInRegistries.ITEM.get(resourceLocation);

                if (itemHolder.isEmpty()) {
                    warnings.add(String.format("'%s' is not supported. (Line %d)", itemStr, lineNumber));
                    continue;
                }

                Item item = itemHolder.get().value();
                if (item == null || item == Items.AIR) {
                    warnings.add(String.format("'%s' is not supported. (Line %d)", itemStr, lineNumber));
                    continue;
                }

                if (items.contains(item) || itemChain.containsKey(item)) {
                    warnings.add(String.format("'%s' is duplicated. (Line %d)", itemStr, lineNumber));
                    continue;
                }

                items.add(item);
            }

            if (items.size() <= 1) {
                warnings.add(String.format("The group size is %d. (Line %d)", items.size(), lineNumber));
                continue;
            }

            // Create circular chain
            for (int i = 0; i < items.size(); i++) {
                itemChain.put(items.get(i), items.get((i + 1) % items.size()));
            }
        }

        return new ParseResult(itemChain, warnings);
    }
}
