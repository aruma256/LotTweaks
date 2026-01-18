package com.github.aruma256.lottweaks.keybinding;

import java.util.Deque;
import java.util.LinkedList;

import com.github.aruma256.lottweaks.event.ScrollEvent;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ItemCycleKeyBase extends KeyBase {

	protected final Deque<ItemStack> candidates = new LinkedList<>();
	protected int lastRotateTime = -1;
	protected byte rotateDirection = 0;

	public ItemCycleKeyBase(String description, int keyCode, KeyMapping.Category category) {
		super(description, keyCode, category);
	}

	protected void addToCandidatesWithDedup(ItemStack itemStack) {
		for (ItemStack c: candidates) {
			if (ItemStack.matches(c, itemStack)) {
				return;
			}
		}
		candidates.add(itemStack);
	}

	protected void rotateCandidatesForward() {
		candidates.addFirst(candidates.pollLast());
		this.updateLastRotateTime();
		this.rotateDirection = 1;
	}

	protected void rotateCandidatesBackward() {
		candidates.addLast(candidates.pollFirst());
		this.updateLastRotateTime();
		this.rotateDirection = -1;
	}

	protected void updateCurrentItemStack(ItemStack itemStack) {
		Minecraft mc = Minecraft.getInstance();
		mc.player.getInventory().setItem(mc.player.getInventory().getSelectedSlot(), itemStack);
        mc.gameMode.handleCreativeModeItemAdd(mc.player.getItemInHand(InteractionHand.MAIN_HAND), 36 + mc.player.getInventory().getSelectedSlot());
	}

	@Override
	protected void onKeyPressStart() {
		this.resetLastRotateTime();
		this.rotateDirection = 0;
	}

	@Override
	protected void onKeyReleased() {
		candidates.clear();
	}

	private void resetLastRotateTime() {
		this.lastRotateTime = 0;
	}

	private void updateLastRotateTime() {
		this.lastRotateTime = this.pressTime;
	}

	protected boolean shouldRenderCandidates() {
		if (this.pressTime == 0) {
			return false;
		}
		if (!Minecraft.getInstance().player.isCreative()) {
			return false;
		}
		if (candidates.isEmpty()) {
			return false;
		}
		return true;
	}

	protected void handleScroll(ScrollEvent event) {
		if (this.pressTime == 0) {
			return;
		}
		if (!Minecraft.getInstance().player.isCreative()) {
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

}
