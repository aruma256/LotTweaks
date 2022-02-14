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
import net.minecraft.world.phys.HitResult;

@Environment(EnvType.CLIENT)
public class AdjustRangeKey extends LTKeyBase implements RenderHotbarListener {

	private static final float DEFAULT_REACH_DISTANCE = 6;
	private static float reachDistance = DEFAULT_REACH_DISTANCE;
	
	public AdjustRangeKey(int keyCode, String category) {
		super("AdjustRange", keyCode, category);
	}

	@Override
	public void onRenderHotbar(RenderHotbarEvent event) {
//		if (event.getType() != ElementType.HOTBAR) {
//			return;
//		}
		if (this.pressTime == 0) {
			return;
		}
		if (!Minecraft.getInstance().player.isCreative()) {
			return;
		}
		if (!LotTweaksClient.requireServerVersion("2.2.1")) {
			LTTextRenderer.showServerSideRequiredMessage(event.getMatrixStack(), event.getWindow(), "2.2.1");
			return;
		}
		// Update dist
		Minecraft mc = Minecraft.getInstance();
		HitResult rayTraceResult = mc.getCameraEntity().pick(255.0, mc.getFrameTime(), false);
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
		LTTextRenderer.showMessage(event.getMatrixStack(), event.getWindow(), distStr);
	}

	public static float getRange() {
		return reachDistance;
	}

	public static void resetRange() {
		reachDistance = DEFAULT_REACH_DISTANCE;
	}

}
