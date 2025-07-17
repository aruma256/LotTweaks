package com.github.lotqwerty.lottweaks.client.keys;

import java.util.Deque;
import java.util.LinkedList;

import com.github.lotqwerty.lottweaks.client.renderer.LTRenderer;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ExPickKey extends ItemSelectKeyBase implements IGuiOverlay {

	private static final int HISTORY_SIZE = 10;

	private static final BlockPos[] SEARCH_POS = {
			new BlockPos(1, 0, 0),
			new BlockPos(-1, 0, 0),
			new BlockPos(0, 1, 0),
			new BlockPos(0, -1, 0),
			new BlockPos(0, 0, 1),
			new BlockPos(0, 0, -1),
			//
			new BlockPos(1, 1, 0),
			new BlockPos(1, -1, 0),
			new BlockPos(-1, 1, 0),
			new BlockPos(-1, -1, 0),
			new BlockPos(1, 0, 1),
			new BlockPos(1, 0, -1),
			new BlockPos(-1, 0, 1),
			new BlockPos(-1, 0, -1),
			new BlockPos(0, 1, 1),
			new BlockPos(0, 1, -1),
			new BlockPos(0, -1, 1),
			new BlockPos(0, -1, -1),
			};

	private static final Deque<ItemStack> breakHistory = new LinkedList<>();

	private boolean isHistoryMode = false;

	public ExPickKey(int keyCode, String category) {
		super("lottweaks-expick", keyCode, category);
	}

	@Override
	protected void onKeyPressStart() {
		super.onKeyPressStart();
		candidates.clear();
		Minecraft mc = Minecraft.getInstance();
		HitResult rayTraceResult;

		if (!mc.player.isCreative()) {
			rayTraceResult = mc.hitResult;
			if (rayTraceResult != null) {
				Forge_onPickBlock(rayTraceResult);
			}
			return;
		}
		if (!mc.player.isShiftKeyDown()) {
			normalModePick();
		} else {
			historyModePick();
		}
	}

	private void normalModePick() {
		Minecraft mc = Minecraft.getInstance();
		HitResult rayTraceResult = mc.getCameraEntity().pick(255.0, mc.getFrameTime(), false);
		if (rayTraceResult == null) {
			return;
		}
		boolean succeeded = Forge_onPickBlock(rayTraceResult);
		if (!succeeded) {
			return;
		}
		ItemStack itemStack = mc.player.getInventory().getSelected();
		if (itemStack.isEmpty()) {
			return;
		}
		addToCandidatesWithDedup(itemStack);
		BlockPos pos = ((BlockHitResult)rayTraceResult).getBlockPos();
		for (BlockPos posDiff : SEARCH_POS) {
			try {
				BlockState state = mc.level.getBlockState(pos.offset(posDiff));
				itemStack = state.getBlock().getCloneItemStack(state, rayTraceResult, mc.level, pos, mc.player);
				if (!itemStack.isEmpty()) {
					addToCandidatesWithDedup(itemStack);
				}
			} catch (Exception e) {
			}
		}
	}

	private void historyModePick() {
		if (!breakHistory.isEmpty()) {
			candidates.addAll(breakHistory);
			candidates.addFirst(Minecraft.getInstance().player.getInventory().getSelected());
			isHistoryMode = true;
		}
	}

	private static boolean Forge_onPickBlock(HitResult rayTraceResult) {
		Minecraft mc = Minecraft.getInstance();
		final HitResult tmpHitResult = mc.hitResult;
		mc.hitResult = rayTraceResult;
		final int tmpSlot = mc.player.getInventory().selected;
		final ItemStack tmpStack = mc.player.getInventory().getSelected();
		//
		mc.pickBlock();
		//
		mc.hitResult = tmpHitResult;
		//
		if (tmpSlot != mc.player.getInventory().selected) return true;
		if (tmpStack != mc.player.getInventory().getSelected()) return true;
		if (rayTraceResult.getType() != HitResult.Type.BLOCK) return false;
		BlockPos blockpos = ((BlockHitResult)rayTraceResult).getBlockPos();
		BlockState blockstate = mc.level.getBlockState(blockpos);
		if (blockstate.isAir()) return false;
		ItemStack itemstack = blockstate.getCloneItemStack(rayTraceResult, mc.level, blockpos, mc.player);
		return (itemstack != null) && !itemstack.isEmpty();
	}

	@Override
	protected void onKeyReleased() {
		super.onKeyReleased();
		isHistoryMode = false;
	}

	@SubscribeEvent
	public void onMouseWheelEvent(final InputEvent.MouseScrollingEvent event) {
		if (this.pressTime == 0) {
			return;
		}
		if (!Minecraft.getInstance().player.isCreative()) {
			return;
		}
		if (event.isCanceled()) {
			return;
		}
		double wheel = event.getDeltaY();
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
	public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTicks, int screenWidth, int screenHeight) {
		if (this.pressTime == 0) {
			return;
		}
		if (!Minecraft.getInstance().player.isCreative()) {
			return;
		}
		if (candidates.isEmpty()) {
			return;
		}
		if (!isHistoryMode) {
			int x = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - 8;
			int y = Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2 - 8;
			LTRenderer.renderItemStacks(guiGraphics, candidates, x, y, pressTime, partialTicks, lastRotateTime, rotateDirection);
		} else {
			int x = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - 90 + Minecraft.getInstance().player.getInventory().selected * 20 + 2;
			int y = Minecraft.getInstance().getWindow().getGuiScaledHeight() - 16 - 3;
			LTRenderer.renderItemStacks(guiGraphics, candidates, x, y, pressTime, partialTicks, lastRotateTime, rotateDirection, LTRenderer.RenderMode.LINE);
		}
	}

	@SubscribeEvent
	public void onBreakBlock(final PlayerInteractEvent.LeftClickBlock event) {
		if (!event.getLevel().isClientSide) {
			return;
		}
		if (!event.getEntity().isCreative()) {
			return;
		}
		//
		Minecraft mc = Minecraft.getInstance();
		BlockState blockState = event.getLevel().getBlockState(event.getPos());
		ItemStack itemStack = blockState.getBlock().getCloneItemStack(blockState, mc.hitResult, event.getLevel(), event.getPos(), event.getEntity());
		addToHistory(itemStack);
	}

	protected static void addToHistory(ItemStack itemStack) {
		if (itemStack == null || itemStack.isEmpty()) {
			return;
		}
		Deque<ItemStack> tmpHistory = new LinkedList<>();
		tmpHistory.addAll(breakHistory);
		breakHistory.clear();
		while(!tmpHistory.isEmpty()) {
			if (!ItemStack.matches(tmpHistory.peekFirst(), itemStack)) {
				breakHistory.add(tmpHistory.pollFirst());
			} else {
				tmpHistory.removeFirst();
			}
		}
		while (breakHistory.size() >= HISTORY_SIZE) {
			breakHistory.pollLast();
		}
		breakHistory.addFirst(itemStack);
	}

}