package com.github.aruma256.lottweaks.keybinding;

import com.github.aruma256.lottweaks.LotTweaks;
import com.github.aruma256.lottweaks.LTPacketHandlerClient;
import com.github.aruma256.lottweaks.LotTweaksClient;
import com.github.aruma256.lottweaks.event.RenderHotbarEvent;
import com.github.aruma256.lottweaks.event.RenderHotbarEvent.RenderHotbarListener;
import com.github.aruma256.lottweaks.render.HudTextRenderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.HitResult;

@Environment(EnvType.CLIENT)
public class ReachExtensionKey extends KeyBase implements RenderHotbarListener {

	private static final float DEFAULT_REACH_DISTANCE = 6;
	private static float reachDistance = DEFAULT_REACH_DISTANCE;

	public ReachExtensionKey(int keyCode, KeyMapping.Category category) {
		super("lottweaks-adjustrange", keyCode, category);
	}

	@Override
	public void onRenderHotbar(RenderHotbarEvent event) {
		GuiGraphics guiGraphics = event.getGuiGraphics();
		if (this.pressTime == 0) {
			return;
		}
		if (!Minecraft.getInstance().player.isCreative()) {
			return;
		}
		if (!LotTweaksClient.requireServerVersion("2.3.0")) {
			HudTextRenderer.showServerSideRequiredMessage(guiGraphics, Minecraft.getInstance().getWindow(), "2.3.0");
			return;
		}
		// Update dist
		Minecraft mc = Minecraft.getInstance();
		HitResult rayTraceResult = mc.getCameraEntity().pick(255.0, getPartialTick(), false);
		double dist;
		if (rayTraceResult == null || rayTraceResult.getType() == HitResult.Type.MISS) {
			dist = LotTweaks.CONFIG.MAX_RANGE;
		} else {
			dist = Math.min(LotTweaks.CONFIG.MAX_RANGE, mc.player.getEyePosition(event.getPartialTicks()).distanceTo(rayTraceResult.getLocation()));
		}
		LTPacketHandlerClient.sendReachRangeMessage(dist);
		reachDistance = (float) dist;
		// Render
		int distInt = (int)dist;
		String distStr = String.valueOf(distInt);
		HudTextRenderer.showMessage(guiGraphics, Minecraft.getInstance().getWindow(), distStr);
	}

	public static float getRange() {
		return reachDistance;
	}

	public static void resetRange() {
		reachDistance = DEFAULT_REACH_DISTANCE;
	}

}
