package com.github.aruma256.lottweaks.keybinding;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class BlockSearcher {

	private static final BlockPos[] SEARCH_OFFSETS = {
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

	public List<ItemStack> searchSurroundingBlocks(BlockPos centerPos) {
		List<ItemStack> results = new ArrayList<>();
		Minecraft mc = Minecraft.getInstance();
		for (BlockPos offset : SEARCH_OFFSETS) {
			try {
				BlockState state = mc.level.getBlockState(centerPos.offset(offset));
				ItemStack itemStack = state.getCloneItemStack(mc.level, centerPos, true);
				if (!itemStack.isEmpty()) {
					results.add(itemStack);
				}
			} catch (Exception e) {
			}
		}
		return results;
	}

}
