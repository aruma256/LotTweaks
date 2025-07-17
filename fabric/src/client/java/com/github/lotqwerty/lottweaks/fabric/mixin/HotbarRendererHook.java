package com.github.lotqwerty.lottweaks.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.lotqwerty.lottweaks.fabric.RenderHotbarEvent;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;

@Mixin(Gui.class)
public abstract class HotbarRendererHook {

	@Inject(at = @At("TAIL"), method = "renderItemHotbar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V")
	private void lottweaks_renderHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo info) {
		RenderHotbarEvent.post(guiGraphics, deltaTracker.getGameTimeDeltaPartialTick(true));
	}

}
