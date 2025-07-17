package com.github.lotqwerty.lottweaks;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class LotTweaksTest {

    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    public void testSimpleAssertion() {
        assertEquals(1, 1);
    }

    @Test
    public void testModConstants() {
        assertEquals("lottweaks", LotTweaks.MODID);
        assertEquals("LotTweaks", LotTweaks.NAME);
    }

    @Test
    public void testItemStackCreation() {
        ItemStack stack = new ItemStack(Items.STONE);
        assertNotNull(stack);
        assertEquals(Items.STONE, stack.getItem());
    }
}
