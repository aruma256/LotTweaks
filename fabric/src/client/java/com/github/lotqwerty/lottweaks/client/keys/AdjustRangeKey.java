package com.github.lotqwerty.lottweaks.client.keys;

import com.github.lotqwerty.lottweaks.LotTweaks;
import com.github.lotqwerty.lottweaks.client.LTPacketHandlerClient;
import com.github.lotqwerty.lottweaks.client.LotTweaksClient;
import com.github.lotqwerty.lottweaks.client.renderer.LTTextRenderer;
import com.github.lotqwerty.lottweaks.fabric.RenderHotbarEvent;
import com.github.lotqwerty.lottweaks.fabric.RenderHotbarEvent.RenderHotbarListener;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.HitResult;

@Environment(EnvType.CLIENT)
public class AdjustRangeKey extends LTKeyBase implements RenderHotbarListener {

	private static final float DEFAULT_REACH_DISTANCE = 6;
	private static float reachDistance = DEFAULT_REACH_DISTANCE;
	
	public AdjustRangeKey(int keyCode, String category) {
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
			LTTextRenderer.showServerSideRequiredMessage(guiGraphics, Minecraft.getInstance().getWindow(), "2.3.0");
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
		LTTextRenderer.showMessage(guiGraphics, Minecraft.getInstance().getWindow(), distStr);
	}

	public static float getRange() {
		return reachDistance;
	}

	public static void resetRange() {
		reachDistance = DEFAULT_REACH_DISTANCE;
	}

}
