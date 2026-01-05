package com.github.aruma256.lottweaks.palette;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.github.aruma256.lottweaks.LotTweaks;

public class PaletteConfigManager {

    private static final String DEFAULT_RESOURCE_PRIMARY = "/assets/lottweaks/default-block-groups.txt";
    private static final String DEFAULT_RESOURCE_SECONDARY = "/assets/lottweaks/default-block-groups2.txt";

    private static final File CONFIG_DIR = new File("config");

    public static final List<String> LOG_CONFIG_WARNINGS = new ArrayList<>();

    private static List<String> linesFromFilePrimary = new ArrayList<>();
    private static List<String> linesFromFileSecondary = new ArrayList<>();

    private static String getDefaultResourcePath(PaletteGroup group) {
        return (group == PaletteGroup.PRIMARY) ? DEFAULT_RESOURCE_PRIMARY : DEFAULT_RESOURCE_SECONDARY;
    }

    private static File getConfigFile(PaletteGroup group) {
        return new File(CONFIG_DIR, group.getConfigFileName());
    }

    private static List<String> getLinesFromFile(PaletteGroup group) {
        return (group == PaletteGroup.PRIMARY) ? linesFromFilePrimary : linesFromFileSecondary;
    }

    public static boolean loadAllFromFile() {
        LOG_CONFIG_WARNINGS.clear();
        boolean success = true;
        for (PaletteGroup group : PaletteGroup.values()) {
            success &= loadFromFile(group);
        }
        return success;
    }

    private static boolean loadFromFile(PaletteGroup group) {
        File file = getConfigFile(group);
        try {
            if (!file.exists()) {
                LotTweaks.LOGGER.debug("Config file does not exist, copying default.");
                copyDefaultFromResources(group);
            }
            List<String> lines = readFile(file);
            List<String> linesHolder = getLinesFromFile(group);
            linesHolder.clear();
            linesHolder.addAll(lines);

            List<String> warnings = ItemPalette.loadFromLines(lines, group);
            LOG_CONFIG_WARNINGS.addAll(warnings.stream()
                .map(w -> String.format("%s (%s)", w, group.name()))
                .toList());

        } catch (IOException e) {
            LotTweaks.LOGGER.error("Failed to load config from file (Group: {})", group.name());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static List<String> readFile(File file) throws IOException {
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

    private static void copyDefaultFromResources(PaletteGroup group) throws IOException {
        CONFIG_DIR.mkdirs();
        File targetFile = getConfigFile(group);

        try (InputStream is = PaletteConfigManager.class.getResourceAsStream(getDefaultResourcePath(group))) {
            if (is == null) {
                throw new IOException("Default resource not found: " + getDefaultResourcePath(group));
            }
            Files.copy(is, targetFile.toPath());
        }
    }

    public static void writeAllToFile() {
        for (PaletteGroup group : PaletteGroup.values()) {
            writeToFile(group);
        }
    }

    private static void writeToFile(PaletteGroup group) {
        LotTweaks.LOGGER.debug("Write config to file.");
        File file = getConfigFile(group);
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (String line : getLinesFromFile(group)) {
                writer.append(line);
                writer.newLine();
            }
        } catch (IOException e) {
            LotTweaks.LOGGER.error("Failed to write config to file");
            e.printStackTrace();
            return;
        }
        LotTweaks.LOGGER.debug("Finished.");
    }

    public static boolean tryToAddItemGroup(String newItemGroup, PaletteGroup group) {
        List<String> lines = getLinesFromFile(group);
        lines.add(newItemGroup);

        List<String> warnings = ItemPalette.loadFromLines(lines, group);
        if (warnings.isEmpty()) {
            writeToFile(group);
            return true;
        } else {
            lines.remove(lines.size() - 1);
            // Reload to restore previous state
            ItemPalette.loadFromLines(lines, group);
            return false;
        }
    }
}
