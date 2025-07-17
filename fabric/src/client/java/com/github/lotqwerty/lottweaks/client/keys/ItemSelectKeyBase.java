package com.github.lotqwerty.lottweaks.client.keys;

import java.util.Deque;
import java.util.LinkedList;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ItemSelectKeyBase extends LTKeyBase {

	protected final Deque<ItemStack> candidates = new LinkedList<>();
	protected int lastRotateTime = -1;
	protected byte rotateDirection = 0;

	public ItemSelectKeyBase(String description, int keyCode, String category) {
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
	
}
