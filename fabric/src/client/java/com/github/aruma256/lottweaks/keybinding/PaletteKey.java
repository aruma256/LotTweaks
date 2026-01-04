package com.github.aruma256.lottweaks.keybinding;

import java.util.List;

import com.github.aruma256.lottweaks.LotTweaks;
import com.github.aruma256.lottweaks.palette.ItemPalette;
import com.github.aruma256.lottweaks.palette.PaletteGroup;
import com.github.aruma256.lottweaks.event.RenderHotbarEvent;
import com.github.aruma256.lottweaks.event.ScrollEvent;
import com.github.aruma256.lottweaks.event.RenderHotbarEvent.RenderHotbarListener;
import com.github.aruma256.lottweaks.event.ScrollEvent.ScrollListener;
import com.github.aruma256.lottweaks.renderer.LTRenderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class PaletteKey extends ItemCycleKeyBase implements ScrollListener, RenderHotbarListener {

	private int phase = 0;

	public PaletteKey(int keyCode, KeyMapping.Category category) {
		super("lottweaks-rotate", keyCode, category);
	}

	private void updatePhase() {
		if (this.doubleTapTick == 0) {
			phase = 0;
		} else {
			phase ^= 1;
		}
	}

	private PaletteGroup getGroup() {
		if (LotTweaks.CONFIG.SNEAK_TO_SWITCH_GROUP) {
			return (!Minecraft.getInstance().player.isShiftKeyDown()) ? PaletteGroup.PRIMARY : PaletteGroup.SECONDARY;
		} else {
			return this.phase == 0 ? PaletteGroup.PRIMARY : PaletteGroup.SECONDARY;
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
		List<ItemStack> results = ItemPalette.getAllCycleItems(itemStack, getGroup());
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
