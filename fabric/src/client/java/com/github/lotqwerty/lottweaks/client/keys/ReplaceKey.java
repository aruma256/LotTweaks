package com.github.lotqwerty.lottweaks.client.keys;

import com.github.lotqwerty.lottweaks.LotTweaks;
import com.github.lotqwerty.lottweaks.client.LTPacketHandlerClient;
import com.github.lotqwerty.lottweaks.client.LotTweaksClient;
import com.github.lotqwerty.lottweaks.client.renderer.LTTextRenderer;
import com.github.lotqwerty.lottweaks.client.renderer.SelectionBoxRenderer;
import com.github.lotqwerty.lottweaks.fabric.DrawBlockOutlineEvent;
import com.github.lotqwerty.lottweaks.fabric.DrawBlockOutlineEvent.DrawBlockOutlineListener;
import com.github.lotqwerty.lottweaks.fabric.RenderHotbarEvent;
import com.github.lotqwerty.lottweaks.fabric.RenderHotbarEvent.RenderHotbarListener;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

@Environment(EnvType.CLIENT)
public class ReplaceKey extends LTKeyBase implements RenderHotbarListener, DrawBlockOutlineListener {

	private BlockState lockedBlockState = null;

	public ReplaceKey(int keyCode, String category) {
		super("lottweaks-replace", keyCode, category);
	}

	@Override
	protected void onKeyPressStart() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player.isShiftKeyDown() ^ LotTweaks.CONFIG.INVERT_REPLACE_LOCK) {
			HitResult target = mc.hitResult;
			if (target != null && target.getType() == HitResult.Type.BLOCK){
				lockedBlockState = mc.level.getBlockState(((BlockHitResult)target).getBlockPos());
			} else {
				lockedBlockState = Blocks.AIR.defaultBlockState();
			}
		} else {
			lockedBlockState = null;
		}
	}

	@Override
	protected void onKeyReleased() {
		lockedBlockState = null;
	}

	@Override
	public void onDrawBlockHighlightEvent(final DrawBlockOutlineEvent event) {
		if (this.pressTime == 0) {
			return;
		}
		if (lockedBlockState == null) {
			return;
		}
		if (SelectionBoxRenderer.render(event.getCamera(), event.getPoseStack(), event.getBuffers(), event.getPos(), 0f, 1f, 0f, 0f)) {
			event.setCanceled(true);
		}
	}

	@Override
	public void onRenderHotbar(RenderHotbarEvent event) {
		/*
		if (event.getPhase() != EventPriority.NORMAL) {
			return;
		}
		*/
		if (this.pressTime == 0) {
			return;
		}
		if (!LotTweaksClient.requireServerVersion("2.2.1")) {
			LTTextRenderer.showServerSideRequiredMessage(event.getGuiGraphics(), "2.2.1");
			return;
		}
		if (this.pressTime==1 || this.pressTime > LotTweaks.CONFIG.REPLACE_INTERVAL) {
			this.execReplace();
			if (this.pressTime==1) {
				this.pressTime++;
			}
		}
	}

	private void execReplace() {
		Minecraft mc = Minecraft.getInstance();
		if (!mc.player.isCreative()) {
			return;
		}
		HitResult target = mc.hitResult;
		if (target == null || target.getType() != HitResult.Type.BLOCK){
        	return;
        }
		BlockPos pos = ((BlockHitResult)target).getBlockPos();
		BlockState state = mc.level.getBlockState(pos);
		if (state.isAir())
		{
			return;
		}
		if (lockedBlockState != null && lockedBlockState != state) {
			return;
		}
		ItemStack itemStack = mc.player.getInventory().getSelectedItem();
		Block block = Block.byItem(itemStack.getItem());
		if (itemStack.isEmpty() || block == Blocks.AIR) {
			return;
		}
		BlockState newBlockState = block.getStateForPlacement(new BlockPlaceContext(mc.player, InteractionHand.MAIN_HAND, itemStack, (BlockHitResult)target));
		LTPacketHandlerClient.sendReplaceMessage(pos, newBlockState, state);
		// add to history
		ExPickKey.addToHistory(state.getCloneItemStack(mc.level, pos, true));
	}

}