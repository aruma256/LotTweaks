package com.github.aruma256.lottweaks.palette;

/**
 * Represents the two palette groups available in LotTweaks.
 * PRIMARY corresponds to LotTweaks-BlockGroups.txt
 * SECONDARY corresponds to LotTweaks-BlockGroups2.txt
 */
public enum PaletteGroup {
    PRIMARY,
    SECONDARY;

    public static final String CONFIG_FILE_PRIMARY = "LotTweaks-BlockGroups.txt";
    public static final String CONFIG_FILE_SECONDARY = "LotTweaks-BlockGroups2.txt";

    public String getConfigFileName() {
        return (this == PRIMARY) ? CONFIG_FILE_PRIMARY : CONFIG_FILE_SECONDARY;
    }
}
