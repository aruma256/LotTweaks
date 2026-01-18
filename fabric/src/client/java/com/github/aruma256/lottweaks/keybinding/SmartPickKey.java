package com.github.aruma256.lottweaks.keybinding;

import java.util.Deque;
import java.util.LinkedList;

import com.github.aruma256.lottweaks.event.RenderHotbarEvent;
import com.github.aruma256.lottweaks.event.ScrollEvent;
import com.github.aruma256.lottweaks.event.RenderHotbarEvent.RenderHotbarListener;
import com.github.aruma256.lottweaks.event.ScrollEvent.ScrollListener;
import com.github.aruma256.lottweaks.render.ItemStackRenderer;
import com.github.aruma256.lottweaks.mixin.client.VanillaPickInvoker;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

@Environment(EnvType.CLIENT)
public class SmartPickKey extends ItemCycleKeyBase implements ScrollListener, RenderHotbarListener, AttackBlockCallback {

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

	// TODO: ファクトリメソッドパターンに変更してthis-escapeを解消する
	@SuppressWarnings("this-escape")
	public SmartPickKey(int keyCode, KeyMapping.Category category) {
		super("Ex Pick", keyCode, category);
		AttackBlockCallback.EVENT.register(this);
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
				((VanillaPickInvoker)Minecraft.getInstance()).lottweaks_invokeVanillaItemPick();
			}
			return;
		}
		if (!mc.player.isShiftKeyDown()) {
			normalModePick();
		} else {
			historyModePick();
		}
	}

	private static boolean pickBlockAtTarget(HitResult rayTraceResult) {
		Minecraft mc = Minecraft.getInstance();
		final HitResult tmpHitResult = mc.hitResult;
		mc.hitResult = rayTraceResult;
		((VanillaPickInvoker)mc).lottweaks_invokeVanillaItemPick();
		mc.hitResult = tmpHitResult;

		if (rayTraceResult == null || rayTraceResult.getType() != HitResult.Type.BLOCK) {
			return false;
		}
		BlockPos blockPos = ((BlockHitResult)rayTraceResult).getBlockPos();
		BlockState blockState = mc.level.getBlockState(blockPos);
		if (blockState.isAir()) {
			return false;
		}
		return !blockState.getCloneItemStack(mc.level, blockPos, true).isEmpty();
	}

	private void normalModePick() {
		Minecraft mc = Minecraft.getInstance();
		HitResult rayTraceResult = mc.getCameraEntity().pick(255.0, getPartialTick(), false);
		if (rayTraceResult == null) {
			return;
		}
		boolean succeeded = pickBlockAtTarget(rayTraceResult);
		if (!succeeded) {
			return;
		}
		ItemStack itemStack = mc.player.getInventory().getSelectedItem();
		if (itemStack.isEmpty()) {
			return;
		}
		addToCandidatesWithDedup(itemStack);
		BlockPos pos = ((BlockHitResult)rayTraceResult).getBlockPos();
		for (BlockPos posDiff : SEARCH_POS) {
			try {
				BlockState state = mc.level.getBlockState(pos.offset(posDiff));
				itemStack = state.getCloneItemStack(mc.level, pos, true);
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
			candidates.addFirst(Minecraft.getInstance().player.getInventory().getSelectedItem());
			isHistoryMode = true;
		}
	}

	@Override
	protected void onKeyReleased() {
		super.onKeyReleased();
		isHistoryMode = false;
	}

	@Override
	public void onScroll(ScrollEvent event) {
		handleScroll(event);
	}

	@Override
	public void onRenderHotbar(RenderHotbarEvent event) {
		if (!shouldRenderCandidates()) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		float partialTicks = event.getPartialTicks();
		if (!isHistoryMode) {
			int x = mc.getWindow().getGuiScaledWidth() / 2 - 8;
			int y = mc.getWindow().getGuiScaledHeight() / 2 - 8;
			ItemStackRenderer.renderItemStacks(event.getGuiGraphics(), candidates, x, y, pressTime, partialTicks, lastRotateTime, rotateDirection);
		} else {
			int x = mc.getWindow().getGuiScaledWidth() / 2 - 90 + mc.player.getInventory().getSelectedSlot() * 20 + 2;
			int y = mc.getWindow().getGuiScaledHeight() - 16 - 3;
			ItemStackRenderer.renderItemStacks(event.getGuiGraphics(), candidates, x, y, pressTime, partialTicks, lastRotateTime, rotateDirection, ItemStackRenderer.RenderMode.LINE);
		}
	}

	@Override
	public InteractionResult interact(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
		onBlockAttacked(player, world, pos);
		return InteractionResult.PASS;
	}

	private void onBlockAttacked(Player player, Level world, BlockPos pos) {
		if (!world.isClientSide()) {
			return;
		}
		if (!player.isCreative()) {
			return;
		}
		BlockState blockState = world.getBlockState(pos);
		ItemStack itemStack = blockState.getCloneItemStack(world, pos, true);
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
