package com.github.lotqwerty.lottweaks.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public interface VanillaPickInvoker {
	
	@Invoker("pickBlock")
	public void lottweaks_invokeVanillaItemPick();

}
