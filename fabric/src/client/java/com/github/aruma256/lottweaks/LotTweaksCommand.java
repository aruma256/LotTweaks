package com.github.aruma256.lottweaks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.aruma256.lottweaks.palette.ItemPalette;
import com.github.aruma256.lottweaks.palette.ItemState;
import com.github.aruma256.lottweaks.palette.PaletteConfigManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Environment(EnvType.CLIENT)
public class LotTweaksCommand implements ClientCommandRegistrationCallback {

	private static void displayMessage(Component textComponent) {
		Minecraft.getInstance().getChatListener().handleSystemMessage(textComponent, false);
	}

	@Override
	public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
		LiteralArgumentBuilder<FabricClientCommandSource> builder = literal(LotTweaks.MODID)
			.then(literal("add")
				.then(literal("1")
					.executes(context -> {executeAdd(0); return Command.SINGLE_SUCCESS;}))
				.then(literal("2")
					.executes(context -> {executeAdd(1); return Command.SINGLE_SUCCESS;}))
				)
			.then(literal("reload")
				.executes(context -> {executeReload(); return Command.SINGLE_SUCCESS;})
			)
		;
		dispatcher.register(builder);
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> literal(String string) {
		return ClientCommandManager.literal(string);
	}

	private void executeAdd(int groupIndex) throws LotTweaksCommandRuntimeException {
		Minecraft mc = Minecraft.getInstance();
		List<ItemState> cycle = new ArrayList<>();
		int count = 0;
		for (int i = 0; i < Inventory.getSelectionSize(); i++) {
			ItemStack itemStack = mc.player.getInventory().getItem(i);
			if (itemStack.isEmpty()) {
				break;
			}
			Item item = itemStack.getItem();
			if (item == Items.AIR) {
				throw new LotTweaksCommandRuntimeException(String.format("Failed to get item instance. (%d)", i + 1));
			}

			Identifier id = BuiltInRegistries.ITEM.getKey(item);
			String name = id.toString();

			if (ItemPalette.canCycle(itemStack, groupIndex)) {
				throw new LotTweaksCommandRuntimeException(String.format("'%s' already exists (slot %d)", name, i + 1));
			}

			cycle.add(new ItemState(itemStack));
			count++;
		}

		if (cycle.isEmpty()) {
			throw new LotTweaksCommandRuntimeException("Hotbar is empty.");
		}

		if (cycle.size() < 2) {
			throw new LotTweaksCommandRuntimeException("Need at least 2 items to create a cycle.");
		}

		LotTweaks.LOGGER.debug("adding a new block/item-group from /lottweaks command");

		boolean succeeded = PaletteConfigManager.tryToAddItemGroup(cycle, groupIndex);
		if (succeeded) {
			displayMessage(Component.literal(String.format("LotTweaks: added %d blocks/items to group %d", count, groupIndex + 1)));
		} else {
			displayMessage(Component.literal(ChatFormatting.RED + "LotTweaks: failed to add blocks/items"));
		}
	}

	private void executeReload() throws LotTweaksCommandRuntimeException {
		try {
			boolean f = PaletteConfigManager.loadAllFromFile();
			if (!f) throw new LotTweaksCommandRuntimeException("LotTweaks: failed to reload config file");
			displayMessage(Component.literal("LotTweaks: reload succeeded!"));
		} catch (LotTweaksCommandRuntimeException e) {
			displayMessage(Component.literal(ChatFormatting.RED + e.getMessage()));
		}
		LotTweaksClient.showErrorLogToChat();
	}

	// TODO: 例外を使わずにエラーハンドリングを行う設計に変更する
	@SuppressWarnings("serial")
	private static final class LotTweaksCommandRuntimeException extends RuntimeException {
		public LotTweaksCommandRuntimeException(String message) {
			super(message);
		}
	}
}
