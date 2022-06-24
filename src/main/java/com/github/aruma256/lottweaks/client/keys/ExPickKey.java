package com.github.aruma256.lottweaks.client.keys;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import com.github.aruma256.lottweaks.client.selector.CircleItemSelector;
import com.github.aruma256.lottweaks.client.selector.HorizontalItemSelector;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ExPickKey extends LTKeyBase {

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

	private CircleItemSelector nearbyBlockSelector;
	private HorizontalItemSelector historyBlockSelector;

	public ExPickKey(int keyCode, String category) {
		super("Ex Pick", keyCode, category);
	}

	@Override
	protected void onKeyPressStart() {
		super.onKeyPressStart();
		nearbyBlockSelector = null;
		historyBlockSelector = null;
		Minecraft mc = Minecraft.getMinecraft();
		RayTraceResult rayTraceResult;

		if (!mc.player.isCreative()) {
			rayTraceResult = mc.objectMouseOver;
			if (rayTraceResult != null) {
				ForgeHooks.onPickBlock(rayTraceResult, mc.player, mc.world);
			}
			return;
		}
		normalModePick();
		historyModePick();
	}

	private void normalModePick() {
		Minecraft mc = Minecraft.getMinecraft();
		RayTraceResult rayTraceResult = mc.getRenderViewEntity().rayTrace(255.0, mc.getRenderPartialTicks());
		if (rayTraceResult == null) {
			return;
		}
		boolean succeeded = ForgeHooks.onPickBlock(rayTraceResult, mc.player, mc.world);
		if (!succeeded) {
			return;
		}
		List<ItemStack> results = new ArrayList<>();
		results.add(mc.player.inventory.getCurrentItem());
		BlockPos pos = rayTraceResult.getBlockPos();
		for (BlockPos posDiff : SEARCH_POS) {
			try {
				BlockPos targetPos = pos.add(posDiff);
				IBlockState state = mc.world.getBlockState(targetPos);
				ItemStack pickedItemStack = state.getBlock().getPickBlock(state, new RayTraceResult(new Vec3d(targetPos), rayTraceResult.sideHit, targetPos), mc.world, targetPos, mc.player);
				if (!pickedItemStack.isEmpty()) {
					boolean isUnique = true;
					for (ItemStack result : results) {
						if (ItemStack.areItemStacksEqual(result, pickedItemStack)) {
							isUnique = false;
							break;
						}
					}
					if (isUnique) {
						results.add(pickedItemStack);
					}
				}
			} catch (Exception e) {
			}
		}
		if (results.size() > 1) {
			nearbyBlockSelector = new CircleItemSelector(results, mc.player.inventory.currentItem);
		}
	}

	private void historyModePick() {
		if (!breakHistory.isEmpty()) {
			List<ItemStack> results = new LinkedList<>();
			results.add(Minecraft.getMinecraft().player.inventory.getCurrentItem());
			results.addAll(breakHistory);
			historyBlockSelector = new HorizontalItemSelector(results, Minecraft.getMinecraft().player.inventory.currentItem);
		}
	}

	@Override
	protected void onKeyReleased() {
		super.onKeyReleased();
		nearbyBlockSelector = null;
		historyBlockSelector = null;
	}

	@SubscribeEvent
	public void onRenderTick(final TickEvent.RenderTickEvent event) {
		if (event.phase != TickEvent.Phase.START) {
			return;
		}
		if (this.pressTime == 0) {
			return;
		}
		if (nearbyBlockSelector == null) {
			return;
		}
		boolean flag = Display.isActive(); // EntityRenderer#updateCameraAndRender
		if (flag && Minecraft.getMinecraft().inGameHasFocus) {
			nearbyBlockSelector.notifyMouseMovement(Mouse.getDX(), Mouse.getDY());
		}
	}

	@SubscribeEvent
	public void onMouseEvent(final MouseEvent event) {
		if (this.pressTime == 0) {
			return;
		}
		if (!Minecraft.getMinecraft().player.isCreative()) {
			return;
		}
		if (event.isCanceled()) {
			return;
		}
		int wheel = event.getDwheel();
		if (wheel == 0) {
			return;
		}
		event.setCanceled(true);
		if (historyBlockSelector == null) {
			return;
		}
		historyBlockSelector.rotate(wheel);
	}

	@SubscribeEvent
	public void onRenderOverlay(final RenderGameOverlayEvent.Post event) {
		if (event.getType() != ElementType.HOTBAR) {
			return;
		}
		if (this.pressTime == 0) {
			return;
		}
		if (!Minecraft.getMinecraft().player.isCreative()) {
			return;
		}
		ScaledResolution sr = event.getResolution();
		if (nearbyBlockSelector != null) {
			nearbyBlockSelector.render(sr);
		}
		if (historyBlockSelector != null) {
			historyBlockSelector.render(sr);
		}
	}

	@SubscribeEvent
	public void onBreakBlock(final PlayerInteractEvent.LeftClickBlock event) {
		if (!event.getWorld().isRemote) {
			return;
		}
		if (!event.getEntityPlayer().isCreative()) {
			return;
		}
		//
		Minecraft mc = Minecraft.getMinecraft();
		IBlockState blockState = event.getWorld().getBlockState(event.getPos());
		ItemStack itemStack = blockState.getBlock().getPickBlock(blockState, mc.objectMouseOver, event.getWorld(), event.getPos(), event.getEntityPlayer());
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
			if (!ItemStack.areItemStacksEqual(tmpHistory.peekFirst(), itemStack)) {
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