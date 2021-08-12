package com.github.lotqwerty.lottweaks.client.keys;

import com.github.lotqwerty.lottweaks.LotTweaks;
import com.github.lotqwerty.lottweaks.client.LotTweaksClient;
import com.github.lotqwerty.lottweaks.client.renderer.LTTextRenderer;
import com.github.lotqwerty.lottweaks.client.renderer.SelectionBoxRenderer;
import com.github.lotqwerty.lottweaks.network.LTPacketHandler;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ReplaceKey extends LTKeyBase {

	private BlockState lockedBlockState = null;

	public ReplaceKey(int keyCode, String category) {
		super("Replace", keyCode, category);
	}

	@Override
	protected void onKeyPressStart() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player.isSneaking() ^ LotTweaks.CONFIG.INVERT_REPLACE_LOCK.get()) {
			RayTraceResult target = mc.objectMouseOver;
			if (target != null && target.getType() == RayTraceResult.Type.BLOCK){
				lockedBlockState = mc.world.getBlockState(((BlockRayTraceResult)target).getPos());
			} else {
				lockedBlockState = Blocks.AIR.getDefaultState();
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
	public void onDrawBlockHighlightEvent(final DrawHighlightEvent.HighlightBlock event) {
		if (this.pressTime == 0) {
			return;
		}
		if (lockedBlockState == null) {
			return;
		}
		if (SelectionBoxRenderer.render(event.getInfo(), event.getMatrix(), event.getBuffers().getBuffer(RenderType.getLines()), event.getTarget().getPos(), event.getPartialTicks(), 1f, 0f, 0f)) {
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
			LTTextRenderer.showServerSideRequiredMessage(new MatrixStack(), "2.2.1");
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
		RayTraceResult target = mc.objectMouseOver;
		if (target == null || target.getType() != RayTraceResult.Type.BLOCK){
        	return;
        }
		BlockPos pos = ((BlockRayTraceResult)target).getPos();
        BlockState state = mc.world.getBlockState(pos);
        if (state.getBlock().isAir(state, mc.world, pos))
        {
            return;
        }
        if (lockedBlockState != null && lockedBlockState != state) {
            return;
        }
		ItemStack itemStack = mc.player.inventory.getCurrentItem();
		Block block = Block.getBlockFromItem(itemStack.getItem());
		if (itemStack.isEmpty() || block == Blocks.AIR) {
			return;
		}
		BlockState newBlockState = block.getStateForPlacement(new BlockItemUseContext(mc.player, Hand.MAIN_HAND, itemStack, (BlockRayTraceResult)target));
		LTPacketHandler.sendReplaceMessage(pos, newBlockState, state);
		// add to history
		ExPickKey.addToHistory(state.getBlock().getPickBlock(state, target, mc.world, pos, mc.player));
	}

}