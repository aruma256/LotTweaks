package com.github.lotqwerty.lottweaks.client.keys;

import com.github.lotqwerty.lottweaks.LotTweaks;
import com.github.lotqwerty.lottweaks.client.LotTweaksClient;
import com.github.lotqwerty.lottweaks.client.renderer.LTTextRenderer;
import com.github.lotqwerty.lottweaks.client.renderer.SelectionBoxRenderer;
import com.github.lotqwerty.lottweaks.network.LTPacketHandler;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ReplaceKey extends LTKeyBase {

	private IBlockState lockedBlockState = null;

	public ReplaceKey(int keyCode, String category) {
		super("Replace", keyCode, category);
	}

	@Override
	protected void onKeyPressStart() {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.player.isSneaking() ^ LotTweaks.CONFIG.INVERT_REPLACE_LOCK) {
			RayTraceResult target = mc.objectMouseOver;
			if (target != null && target.typeOfHit == RayTraceResult.Type.BLOCK){
				lockedBlockState = mc.world.getBlockState(target.getBlockPos());
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
	public void onDrawBlockHighlightEvent(final DrawBlockHighlightEvent event) {
		if (this.pressTime == 0) {
			return;
		}
		if (lockedBlockState == null) {
			return;
		}
		RayTraceResult target = Minecraft.getMinecraft().objectMouseOver;
		if (target != null && target.typeOfHit == RayTraceResult.Type.BLOCK){
			if (SelectionBoxRenderer.render(target.getBlockPos(), event.getPartialTicks(), 1f, 0f, 0f)) {
				event.setCanceled(true);
			}
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
			LTTextRenderer.showServerSideRequiredMessage("2.2.1");
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
		Minecraft mc = Minecraft.getMinecraft();
		if (!mc.player.isCreative()) {
			return;
		}
		RayTraceResult target = mc.objectMouseOver;
		if (target == null || target.typeOfHit != RayTraceResult.Type.BLOCK){
        	return;
        }
        IBlockState state = mc.world.getBlockState(target.getBlockPos());
        if (state.getBlock().isAir(state, mc.world, target.getBlockPos()))
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
		float hitX = (float) (target.hitVec.x - target.getBlockPos().getX());
		float hitY = (float) (target.hitVec.y - target.getBlockPos().getY());
		float hitZ = (float) (target.hitVec.z - target.getBlockPos().getZ());
		IBlockState newBlockState = block.getStateForPlacement(mc.world, target.getBlockPos(), target.sideHit, hitX, hitY, hitZ, itemStack.getItemDamage(), mc.player, EnumHand.MAIN_HAND);
		LTPacketHandler.sendReplaceMessage(target.getBlockPos(), block, block.getMetaFromState(newBlockState), mc.world.getBlockState(target.getBlockPos()).getBlock());
		// add to history
		ExPickKey.addToHistory(state.getBlock().getPickBlock(state, target, mc.world, target.getBlockPos(), mc.player));
	}

}