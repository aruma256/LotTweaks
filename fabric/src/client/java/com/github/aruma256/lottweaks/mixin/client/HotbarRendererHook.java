package com.github.aruma256.lottweaks.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.aruma256.lottweaks.event.RenderHotbarEvent;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;

@Mixin(Gui.class)
public abstract class HotbarRendererHook {

	@Inject(at = @At("TAIL"), method = "extractItemHotbar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V")
	private void lottweaks_renderHotbar(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo info) {
		RenderHotbarEvent.post(guiGraphics, deltaTracker.getGameTimeDeltaPartialTick(true));
	}

}
