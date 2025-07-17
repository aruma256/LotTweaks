package com.github.lotqwerty.lottweaks.client.keys;

import com.github.lotqwerty.lottweaks.LotTweaks;
import com.github.lotqwerty.lottweaks.client.LotTweaksClient;
import com.github.lotqwerty.lottweaks.client.renderer.LTTextRenderer;
import com.github.lotqwerty.lottweaks.client.renderer.SelectionBoxRenderer;
import com.github.lotqwerty.lottweaks.network.LTPacketHandler;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ReplaceKey extends LTKeyBase {

	private BlockState lockedBlockState = null;

	public ReplaceKey(int keyCode, String category) {
		super("lottweaks-replace", keyCode, category);
	}

	@Override
	protected void onKeyPressStart() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player.isShiftKeyDown() ^ LotTweaks.CONFIG.INVERT_REPLACE_LOCK.get()) {
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

	@SubscribeEvent
	public void onDrawBlockHighlightEvent(final RenderHighlightEvent.Block event) {
		if (this.pressTime == 0) {
			return;
		}
		if (lockedBlockState == null) {
			return;
		}
		if (SelectionBoxRenderer.render(event.getCamera(), event.getPoseStack(), event.getMultiBufferSource().getBuffer(RenderType.lines()), event.getTarget().getBlockPos(), event.getPartialTick(), 1f, 0f, 0f)) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onRenderTick(final RenderTickEvent event) {
		if (event.getPhase() != EventPriority.NORMAL) {
			return;
		}
		if (this.pressTime == 0) {
			return;
		}
		if (!LotTweaksClient.requireServerVersion("2.2.1")) {
			LTTextRenderer.showServerSideRequiredMessage(new GuiGraphics(Minecraft.getInstance(), Minecraft.getInstance().renderBuffers().bufferSource()), "2.2.1");
			return;
		}
		if (this.pressTime==1 || this.pressTime > LotTweaks.CONFIG.REPLACE_INTERVAL.get()) {
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
		ItemStack itemStack = mc.player.getInventory().getSelected();
		Block block = Block.byItem(itemStack.getItem());
		if (itemStack.isEmpty() || block == Blocks.AIR) {
			return;
		}
		BlockState newBlockState = block.getStateForPlacement(new BlockPlaceContext(mc.player, InteractionHand.MAIN_HAND, itemStack, (BlockHitResult)target));
		LTPacketHandler.sendReplaceMessage(pos, newBlockState, state);
		// add to history
		ExPickKey.addToHistory(state.getBlock().getCloneItemStack(state, target, mc.level, pos, mc.player));
	}

}