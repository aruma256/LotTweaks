package com.github.aruma256.lottweaks.palette;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.github.aruma256.lottweaks.LotTweaks;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Migrates legacy text-based config files to the new JSON format.
 */
public class ConfigMigrator {

    public static final String LEGACY_PRIMARY = "LotTweaks-BlockGroups.txt";
    public static final String LEGACY_SECONDARY = "LotTweaks-BlockGroups2.txt";

    public static class MigrationResult {
        private final List<List<List<ItemState>>> groups;
        private final List<String> warnings;
        private final boolean migrated;

        public MigrationResult(List<List<List<ItemState>>> groups, List<String> warnings, boolean migrated) {
            this.groups = groups;
            this.warnings = warnings;
            this.migrated = migrated;
        }

        public List<List<List<ItemState>>> getGroups() {
            return groups;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public boolean wasMigrated() {
            return migrated;
        }
    }

    public static boolean hasLegacyConfig(File configDir) {
        return new File(configDir, LEGACY_PRIMARY).exists()
            || new File(configDir, LEGACY_SECONDARY).exists();
    }

    public static MigrationResult migrate(File configDir) {
        List<List<List<ItemState>>> groups = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        File primaryFile = new File(configDir, LEGACY_PRIMARY);
        File secondaryFile = new File(configDir, LEGACY_SECONDARY);

        // Migrate PRIMARY group (index 0)
        List<List<ItemState>> primaryCycles = migrateLegacyFile(primaryFile, warnings, "PRIMARY");
        groups.add(primaryCycles);

        // Migrate SECONDARY group (index 1)
        List<List<ItemState>> secondaryCycles = migrateLegacyFile(secondaryFile, warnings, "SECONDARY");
        groups.add(secondaryCycles);

        LotTweaks.LOGGER.info("Migrated legacy config files to new JSON format");
        if (!warnings.isEmpty()) {
            LotTweaks.LOGGER.warn("Migration warnings: {}", warnings);
        }

        return new MigrationResult(groups, warnings, true);
    }

    private static List<List<ItemState>> migrateLegacyFile(File file, List<String> warnings, String groupName) {
        List<List<ItemState>> cycles = new ArrayList<>();

        if (!file.exists()) {
            LotTweaks.LOGGER.debug("Legacy file {} does not exist, skipping", file.getName());
            return cycles;
        }

        try {
            List<String> lines = readLegacyFile(file);
            Set<ItemState> globalRegistered = new HashSet<>();
            int lineNumber = 0;

            for (String line : lines) {
                lineNumber++;
                if (line.isEmpty() || line.startsWith("//")) {
                    continue;
                }

                List<ItemState> itemsInCycle = new ArrayList<>();
                Set<ItemState> cycleRegistered = new HashSet<>();

                for (String itemStr : line.split(",")) {
                    itemStr = itemStr.trim();
                    if (itemStr.isEmpty()) {
                        continue;
                    }

                    ItemState itemState = parseItemFromLegacyFormat(itemStr);
                    if (itemState == null) {
                        warnings.add(String.format("'%s' is not a valid item. (Line %d, %s)",
                                itemStr, lineNumber, groupName));
                        continue;
                    }

                    if (globalRegistered.contains(itemState) || cycleRegistered.contains(itemState)) {
                        warnings.add(String.format("'%s' is duplicated. (Line %d, %s)",
                                itemStr, lineNumber, groupName));
                        continue;
                    }

                    itemsInCycle.add(itemState);
                    cycleRegistered.add(itemState);
                }

                if (itemsInCycle.size() >= 2) {
                    cycles.add(itemsInCycle);
                    globalRegistered.addAll(itemsInCycle);
                } else if (!line.trim().isEmpty() && !line.startsWith("//")) {
                    warnings.add(String.format("Group has less than 2 valid items. (Line %d, %s)",
                            lineNumber, groupName));
                }
            }

        } catch (IOException e) {
            warnings.add(String.format("Failed to read legacy file %s: %s", file.getName(), e.getMessage()));
            LotTweaks.LOGGER.error("Failed to read legacy file", e);
        }

        return cycles;
    }

    private static ItemState parseItemFromLegacyFormat(String itemStr) {
        Identifier resourceLocation = Identifier.parse(itemStr);
        Optional<Holder.Reference<Item>> itemHolder = BuiltInRegistries.ITEM.get(resourceLocation);

        if (itemHolder.isEmpty()) {
            return null;
        }

        Item item = itemHolder.get().value();
        if (item == null || item == Items.AIR) {
            return null;
        }

        return new ItemState(new ItemStack(item));
    }

    private static List<String> readLegacyFile(File file) throws IOException {
        // Try UTF-8 first
        try {
            return Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // ignore
        }
        // Try Shift_JIS for legacy files
        try {
            return Files.readAllLines(file.toPath(), Charset.forName("Shift_JIS"));
        } catch (IOException e) {
            // ignore
        }
        // Fall back to system default
        return Files.readAllLines(file.toPath(), Charset.defaultCharset());
    }
}
