package com.github.aruma256.lottweaks.palette;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PaletteGroupTest {

    @Test
    public void testEnumValuesExist() {
        assertEquals(2, PaletteGroup.values().length);
        assertNotNull(PaletteGroup.PRIMARY);
        assertNotNull(PaletteGroup.SECONDARY);
    }

    @Test
    public void testGetConfigFileName() {
        assertEquals("LotTweaks-BlockGroups.txt", PaletteGroup.PRIMARY.getConfigFileName());
        assertEquals("LotTweaks-BlockGroups2.txt", PaletteGroup.SECONDARY.getConfigFileName());
    }
}
