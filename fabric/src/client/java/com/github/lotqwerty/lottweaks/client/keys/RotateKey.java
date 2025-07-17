package com.github.lotqwerty.lottweaks.client.keys;

import java.util.List;

import com.github.lotqwerty.lottweaks.LotTweaks;
import com.github.lotqwerty.lottweaks.client.RotationHelper;
import com.github.lotqwerty.lottweaks.client.RotationHelper.Group;
import com.github.lotqwerty.lottweaks.client.renderer.LTRenderer;
import com.github.lotqwerty.lottweaks.fabric.RenderHotbarEvent;
import com.github.lotqwerty.lottweaks.fabric.RenderHotbarEvent.RenderHotbarListener;
import com.github.lotqwerty.lottweaks.fabric.ScrollEvent;
import com.github.lotqwerty.lottweaks.fabric.ScrollEvent.ScrollListener;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class RotateKey extends ItemSelectKeyBase implements ScrollListener, RenderHotbarListener {

	private int phase = 0;

	public RotateKey(int keyCode, String category) {
		super("lottweaks-rotate", keyCode, category);
	}

	private void updatePhase() {
		if (this.doubleTapTick == 0) {
			phase = 0;
		} else {
			phase ^= 1;
		}
	}

	private Group getGroup() {
		if (LotTweaks.CONFIG.SNEAK_TO_SWITCH_GROUP) {
			return (!Minecraft.getInstance().player.isShiftKeyDown()) ? Group.PRIMARY : Group.SECONDARY;
		} else {
			return this.phase==0 ? Group.PRIMARY : Group.SECONDARY;
		}
	}

	@Override
	protected void onKeyPressStart() {
		super.onKeyPressStart();
		this.updatePhase();
		candidates.clear();
		Minecraft mc = Minecraft.getInstance();
		if (!mc.player.isCreative()) {
			return;
		}
		ItemStack itemStack = mc.player.getInventory().getSelectedItem();
		if (itemStack.isEmpty()) {
			return;
		}
		List<ItemStack> results = RotationHelper.getAllRotateResult(itemStack, getGroup());
		if (results == null || results.size() <= 1) {
			return;
		}
		candidates.addAll(results);
	}

	protected void onKeyReleased() {
		super.onKeyReleased();
	}

	@Override
	public void onScroll(ScrollEvent event) {
		if (this.pressTime == 0) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (!mc.player.isCreative()) {
			return;
		}
		if (event.isCanceled()) {
			return;
		}
		double wheel = event.getScrollDelta();
		if (wheel == 0) {
			return;
		}
		event.setCanceled(true);
		if (candidates.isEmpty()) {
			return;
		}
		if (wheel > 0) {
			this.rotateCandidatesForward();
		}else {
			this.rotateCandidatesBackward();
		}
		this.updateCurrentItemStack(candidates.getFirst());
	}

	@Override
	public void onRenderHotbar(RenderHotbarEvent event) {
		float partialTicks = event.getPartialTicks();
		if (this.pressTime == 0) {
			candidates.clear();
			return;
		}
		if (!Minecraft.getInstance().player.isCreative()) {
			return;
		}
		if (candidates.isEmpty()) {
			return;
		}
		int x = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - 90 + Minecraft.getInstance().player.getInventory().getSelectedSlot() * 20 + 2;
		int y = Minecraft.getInstance().getWindow().getGuiScaledHeight() - 16 - 3;
		y -= 50 + (20 + candidates.size());
		LTRenderer.renderItemStacks(event.getGuiGraphics(), candidates, x, y, pressTime, partialTicks, lastRotateTime, rotateDirection);
	}

}