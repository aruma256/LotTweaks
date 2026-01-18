package com.github.aruma256.lottweaks.palette;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemValidationTest {

    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    public void testIsValidStack_withNull() {
        assertFalse(ItemValidation.isValidStack(null));
    }

    @Test
    public void testIsValidStack_withEmptyStack() {
        assertFalse(ItemValidation.isValidStack(ItemStack.EMPTY));
    }

    @Test
    public void testIsValidStack_withAirStack() {
        assertFalse(ItemValidation.isValidStack(new ItemStack(Items.AIR)));
    }

    @Test
    public void testIsValidStack_withValidStack() {
        assertTrue(ItemValidation.isValidStack(new ItemStack(Items.STONE)));
        assertTrue(ItemValidation.isValidStack(new ItemStack(Items.DIAMOND_SWORD)));
    }

    @Test
    public void testIsValidItem_withNull() {
        assertFalse(ItemValidation.isValidItem(null));
    }

    @Test
    public void testIsValidItem_withAir() {
        assertFalse(ItemValidation.isValidItem(Items.AIR));
    }

    @Test
    public void testIsValidItem_withValidItem() {
        assertTrue(ItemValidation.isValidItem(Items.STONE));
        assertTrue(ItemValidation.isValidItem(Items.DIAMOND_SWORD));
    }
}
