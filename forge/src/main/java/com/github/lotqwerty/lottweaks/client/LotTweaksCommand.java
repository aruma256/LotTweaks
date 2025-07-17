package com.github.lotqwerty.lottweaks.client;

import java.util.StringJoiner;

import com.github.lotqwerty.lottweaks.LotTweaks;
import com.github.lotqwerty.lottweaks.client.RotationHelper.Group;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.GiveCommand;
import net.minecraft.ChatFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

@OnlyIn(Dist.CLIENT)
public class LotTweaksCommand extends LiteralArgumentBuilder<CommandSourceStack> {

	private static final DynamicCommandExceptionType ERROR_NO_ACTION_PERFORMED = new DynamicCommandExceptionType((obj) -> { return (Component)obj; });

	protected LotTweaksCommand() {
		super(LotTweaks.MODID);
	}

	private static void displayMessage(Component textComponent) {
		Minecraft.getInstance().getChatListener().handleSystemMessage(textComponent, false);
	}

	@SubscribeEvent
	public void onRegisterCommand(final RegisterClientCommandsEvent event) {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(LotTweaks.MODID)
			.then(Commands.literal("add")
				.then(Commands.literal("1")
					.executes(context -> {executeAdd(Group.PRIMARY); return Command.SINGLE_SUCCESS;}))
				.then(Commands.literal("2")
					.executes(context -> {executeAdd(Group.SECONDARY); return Command.SINGLE_SUCCESS;}))
				)
			.then(Commands.literal("reload")
				.executes(context -> {executeReload(); return Command.SINGLE_SUCCESS;})
			)
		;
		event.getDispatcher().register(builder);
	}

	private void executeAdd(Group group) throws CommandSyntaxException {
		GiveCommand a;
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
				throw ERROR_NO_ACTION_PERFORMED.create(Component.literal(String.format("Failed to get item instance. (%d)", i + 1)));
			}
			String name = ForgeRegistries.ITEMS.getKey(item).toString();
			if (RotationHelper.canRotate(itemStack, group)) {
				throw ERROR_NO_ACTION_PERFORMED.create(Component.literal(String.format("'%s' already exists (slot %d)", name, i + 1)));
			}
			stringJoiner.add(name);
			count++;
		}
		String line = stringJoiner.toString();
		if (line.isEmpty()) {
			throw ERROR_NO_ACTION_PERFORMED.create(Component.literal(String.format("Hotbar is empty.")));
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

	private void executeReload() {
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
