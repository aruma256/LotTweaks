package com.github.aruma256.lottweaks;

import java.util.List;

import com.github.aruma256.lottweaks.palette.ItemPalette;
import com.github.aruma256.lottweaks.palette.PaletteConfigManager;
import com.github.aruma256.lottweaks.palette.PaletteGroup;

import net.minecraft.world.item.ItemStack;

/**
 * Legacy facade for palette functionality.
 * Delegates to the new palette package classes.
 *
 * @deprecated Use classes in com.github.aruma256.lottweaks.palette package directly.
 */
@Deprecated
public class RotationHelper {

	public static final String ITEMGROUP_CONFFILE_PRIMARY = PaletteGroup.CONFIG_FILE_PRIMARY;

	public static final List<String> LOG_GROUP_CONFIG = PaletteConfigManager.LOG_CONFIG_WARNINGS;

	/**
	 * @deprecated Use {@link PaletteGroup} instead.
	 */
	@Deprecated
	public enum Group {
		PRIMARY,
		SECONDARY;

		public PaletteGroup toPaletteGroup() {
			return (this == PRIMARY) ? PaletteGroup.PRIMARY : PaletteGroup.SECONDARY;
		}

		public static Group fromPaletteGroup(PaletteGroup paletteGroup) {
			return (paletteGroup == PaletteGroup.PRIMARY) ? PRIMARY : SECONDARY;
		}
	}

	/**
	 * @deprecated Use {@link ItemPalette#canCycle(ItemStack, PaletteGroup)} instead.
	 */
	@Deprecated
	public static boolean canRotate(ItemStack itemStack, Group group) {
		return ItemPalette.canCycle(itemStack, group.toPaletteGroup());
	}

	/**
	 * @deprecated Use {@link ItemPalette#getAllCycleItems(ItemStack, PaletteGroup)} instead.
	 */
	@Deprecated
	public static List<ItemStack> getAllRotateResult(ItemStack itemStack, Group group) {
		return ItemPalette.getAllCycleItems(itemStack, group.toPaletteGroup());
	}

	/**
	 * @deprecated Use {@link PaletteConfigManager#loadAllFromFile()} instead.
	 */
	@Deprecated
	public static boolean loadAllFromFile() {
		return PaletteConfigManager.loadAllFromFile();
	}

	/**
	 * @deprecated No longer needed - parsing happens automatically in loadAllFromFile().
	 */
	@Deprecated
	public static boolean loadAllItemGroupFromStrArray() {
		// No-op: parsing now happens in loadAllFromFile()
		return true;
	}

	/**
	 * @deprecated Use {@link PaletteConfigManager#tryToAddItemGroup(String, PaletteGroup)} instead.
	 */
	@Deprecated
	public static boolean tryToAddItemGroupFromCommand(String newItemGroup, Group group) {
		return PaletteConfigManager.tryToAddItemGroup(newItemGroup, group.toPaletteGroup());
	}
}
