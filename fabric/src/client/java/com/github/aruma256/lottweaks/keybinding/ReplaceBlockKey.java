package com.github.aruma256.lottweaks.keybinding;

import com.github.aruma256.lottweaks.LotTweaks;
import com.github.aruma256.lottweaks.ModNetworkClient;
import com.github.aruma256.lottweaks.LotTweaksClient;
import com.github.aruma256.lottweaks.event.DrawBlockOutlineEvent;
import com.github.aruma256.lottweaks.event.RenderHotbarEvent;
import com.github.aruma256.lottweaks.event.DrawBlockOutlineEvent.DrawBlockOutlineListener;
import com.github.aruma256.lottweaks.event.RenderHotbarEvent.RenderHotbarListener;
import com.github.aruma256.lottweaks.render.HudTextRenderer;
import com.github.aruma256.lottweaks.render.SelectionBoxRenderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
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
public class ReplaceBlockKey extends KeyBase implements RenderHotbarListener, DrawBlockOutlineListener {

	private final PickHistory pickHistory;
	private BlockState lockedBlockState = null;

	public ReplaceBlockKey(int keyCode, KeyMapping.Category category, PickHistory pickHistory) {
		super("lottweaks-replace", keyCode, category);
		this.pickHistory = pickHistory;
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
		if (this.pressTime == 0) {
			return;
		}
		if (!LotTweaksClient.requireServerVersion("2.2.1")) {
			HudTextRenderer.showServerSideRequiredMessage(event.getGuiGraphics(), "2.2.1");
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
		ModNetworkClient.sendReplaceBlockPacket(pos, newBlockState, state);
		pickHistory.add(state.getCloneItemStack(mc.level, pos, true));
	}

}
