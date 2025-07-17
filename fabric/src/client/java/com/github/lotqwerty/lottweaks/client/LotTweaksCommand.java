package com.github.lotqwerty.lottweaks.client;

import java.util.StringJoiner;

import com.github.lotqwerty.lottweaks.LotTweaks;
import com.github.lotqwerty.lottweaks.client.RotationHelper.Group;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
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
					.executes(context -> {executeAdd(Group.PRIMARY); return Command.SINGLE_SUCCESS;}))
				.then(literal("2")
					.executes(context -> {executeAdd(Group.SECONDARY); return Command.SINGLE_SUCCESS;}))
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

	private void executeAdd(Group group) throws LotTweaksCommandRuntimeException {
		Minecraft mc = Minecraft.getInstance();
		StringJoiner stringJoiner = new StringJoiner(",");
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
			String name = BuiltInRegistries.ITEM.getKey(item).toString();
			if (RotationHelper.canRotate(itemStack, group)) {
				throw new LotTweaksCommandRuntimeException(String.format("'%s' already exists (slot %d)", name, i + 1));
			}
			stringJoiner.add(name);
			count++;
		}
		String line = stringJoiner.toString();
		if (line.isEmpty()) {
			throw new LotTweaksCommandRuntimeException(String.format("Hotbar is empty."));
		}
		LotTweaks.LOGGER.debug("adding a new block/item-group from /lottweaks command");
		LotTweaks.LOGGER.debug(line);
		boolean succeeded = RotationHelper.tryToAddItemGroupFromCommand(line, group);
		if (succeeded) {
			displayMessage(Component.literal(String.format("LotTweaks: added %d blocks/items", count)));
		} else {
			displayMessage(Component.literal(ChatFormatting.RED + "LotTweaks: failed to add blocks/items"));
		}
	}

	private void executeReload() throws LotTweaksCommandRuntimeException {
		try {
			boolean f;
			f = RotationHelper.loadAllFromFile();
			if (!f) throw new LotTweaksCommandRuntimeException("LotTweaks: failed to reload config file");
			f = RotationHelper.loadAllItemGroupFromStrArray();
			if (!f) throw new LotTweaksCommandRuntimeException("LotTweaks: failed to reload blocks");
			displayMessage(Component.literal("LotTweaks: reload succeeded!"));
		} catch (LotTweaksCommandRuntimeException e) {
			displayMessage(Component.literal(ChatFormatting.RED + e.getMessage()));
		}
		LotTweaksClient.showErrorLogToChat();
	}

	private static final class LotTweaksCommandRuntimeException extends RuntimeException {
		public LotTweaksCommandRuntimeException(String message) {
	        super(message);
	    }
	}
}
