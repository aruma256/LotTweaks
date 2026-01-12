package com.github.aruma256.lottweaks.palette;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.github.aruma256.lottweaks.LotTweaks;

public class PaletteConfigManager {

    private static final String DEFAULT_JSON_RESOURCE = "/assets/lottweaks/default-item-groups.json";

    private static final File CONFIG_DIR = new File("config");

    public static final List<String> LOG_CONFIG_WARNINGS = new ArrayList<>();

    public static boolean loadAllFromFile() {
        LOG_CONFIG_WARNINGS.clear();

        // Check for migration
        if (ConfigMigrator.hasLegacyConfig(CONFIG_DIR) && !ItemGroupsConfigLoader.configExists(CONFIG_DIR)) {
            LotTweaks.LOGGER.info("Migrating legacy config files to new JSON format...");
            performMigration();
        }

        // If new config doesn't exist, copy default
        if (!ItemGroupsConfigLoader.configExists(CONFIG_DIR)) {
            LotTweaks.LOGGER.debug("Config file does not exist, copying default.");
            copyDefaultJsonFromResources();
        }

        // Load from JSON
        ItemGroupsConfigLoader.LoadResult result = ItemGroupsConfigLoader.load(CONFIG_DIR);
        LOG_CONFIG_WARNINGS.addAll(result.getWarnings());
        ItemPalette.loadGroups(result.getGroups());

        return result.getWarnings().isEmpty();
    }

    private static void performMigration() {
        ConfigMigrator.MigrationResult migrationResult = ConfigMigrator.migrate(CONFIG_DIR);
        LOG_CONFIG_WARNINGS.addAll(migrationResult.getWarnings());

        // Save migrated config to new JSON file
        ItemGroupsConfigLoader.save(CONFIG_DIR, migrationResult.getGroups());
    }

    private static void copyDefaultJsonFromResources() {
        CONFIG_DIR.mkdirs();
        File targetFile = new File(CONFIG_DIR, ItemGroupsConfigLoader.CONFIG_FILE_NAME);

        try (InputStream is = PaletteConfigManager.class.getResourceAsStream(DEFAULT_JSON_RESOURCE)) {
            if (is == null) {
                LotTweaks.LOGGER.warn("Default JSON resource not found: {}. Creating empty config.", DEFAULT_JSON_RESOURCE);
                // Create default empty groups
                List<List<List<ItemState>>> defaultGroups = ItemGroupsConfigLoader.createDefaultGroups();
                ItemGroupsConfigLoader.save(CONFIG_DIR, defaultGroups);
                return;
            }
            Files.copy(is, targetFile.toPath());
        } catch (IOException e) {
            LotTweaks.LOGGER.error("Failed to copy default config from resources", e);
            // Create default empty groups as fallback
            List<List<List<ItemState>>> defaultGroups = ItemGroupsConfigLoader.createDefaultGroups();
            ItemGroupsConfigLoader.save(CONFIG_DIR, defaultGroups);
        }
    }

    public static void saveAllToFile() {
        List<List<List<ItemState>>> groupData = ItemPalette.getGroupData();
        ItemGroupsConfigLoader.save(CONFIG_DIR, groupData);
    }

    public static boolean tryToAddItemGroup(List<ItemState> newCycle, int groupIndex) {
        List<List<List<ItemState>>> groupData = ItemPalette.getGroupData();

        // Ensure we have enough groups
        while (groupData.size() <= groupIndex) {
            groupData.add(new ArrayList<>());
        }

        // Add the new cycle
        groupData.get(groupIndex).add(newCycle);

        // Reload to validate
        ItemPalette.loadGroups(groupData);

        // Save to file
        saveAllToFile();
        return true;
    }

    // --- Deprecated methods for backward compatibility ---

    @Deprecated
    public static boolean tryToAddItemGroup(String newItemGroup, PaletteGroup group) {
        // Parse the line into ItemStates
        List<ItemState> cycle = new ArrayList<>();
        for (String itemStr : newItemGroup.split(",")) {
            itemStr = itemStr.trim();
            if (itemStr.isEmpty()) continue;

            net.minecraft.resources.Identifier resourceLocation = net.minecraft.resources.Identifier.parse(itemStr);
            java.util.Optional<net.minecraft.core.Holder.Reference<net.minecraft.world.item.Item>> itemHolder =
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.get(resourceLocation);

            if (itemHolder.isEmpty()) {
                return false;
            }

            net.minecraft.world.item.Item item = itemHolder.get().value();
            if (item == null || item == net.minecraft.world.item.Items.AIR) {
                return false;
            }

            ItemState itemState = new ItemState(new net.minecraft.world.item.ItemStack(item));

            // Check for duplicates
            if (ItemPalette.canCycle(itemState.toItemStack(), group.ordinal())) {
                return false;
            }

            cycle.add(itemState);
        }

        if (cycle.size() < 2) {
            return false;
        }

        return tryToAddItemGroup(cycle, group.ordinal());
    }
}
