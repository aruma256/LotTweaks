package com.github.lotqwerty.lottweaks.client;

import java.util.StringJoiner;

import com.github.lotqwerty.lottweaks.LotTweaks;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.IClientCommand;

public class LotTweaksCommand extends CommandBase implements IClientCommand {

	private static final String COMMAND_NAME = LotTweaks.MODID;
	private static final String COMMAND_USAGE = String.format("/%s add", COMMAND_NAME);

	@Override
	public String getName() {
		return COMMAND_NAME;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return COMMAND_USAGE;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length != 1) {
			throw new WrongUsageException(getUsage(sender), new Object[0]);
		}
		if (args[0].equals("add")) {
			executeAdd();
		} else if (args[0].equals("reload")) {
			executeReload();
		}
	}

	private void executeAdd() throws CommandException {
		Minecraft mc = Minecraft.getMinecraft();
		StringJoiner stringJoiner = new StringJoiner(",");
		int count = 0;
		for (int i = 0; i < InventoryPlayer.getHotbarSize(); i++) {
			ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
			if (RotationHelper.canRotate(itemStack)) {
				throw new CommandException(String.format("Already exists (%d)", i + 1), new Object[0]);
			}
			System.out.println("Hello");
			if (itemStack.isEmpty()) {
				break;
			}
			Block block = Block.getBlockFromItem(itemStack.getItem());
			int meta = itemStack.getItemDamage();
			if (block == Blocks.AIR) {
				throw new CommandException(String.format("Failed to get block instance. (%d)", i + 1), new Object[0]);
			}
			String name = Block.REGISTRY.getNameForObject(block).toString();
			if (meta == 0) {
				stringJoiner.add(name);
			} else {
				stringJoiner.add(String.format("%s/%d", name, meta));
			}
			count++;
		}
		String line = stringJoiner.toString();
		if (line.isEmpty()) {
			throw new CommandException(String.format("Hotbar is empty."), new Object[0]);
		}
		LotTweaks.LOGGER.debug("adding a new block-group from /lottweaks command");
		LotTweaks.LOGGER.debug(line);
		String[] oldBlockGroups = RotationHelper.BLOCK_GROUPS;
		String[] newBlockGroups = new String[oldBlockGroups.length + 1];
		System.arraycopy(oldBlockGroups, 0, newBlockGroups, 0, oldBlockGroups.length);
		newBlockGroups[newBlockGroups.length - 1] = line;
		boolean succeeded = RotationHelper.tryToUpdateBlockGroupsFromCommand(newBlockGroups);
		if (succeeded) {
			RotationHelper.writeToFile();
			mc.ingameGUI.addChatMessage(ChatType.SYSTEM,
					new TextComponentString(String.format("LotTweaks: added %d blocks", count)));
		} else {
			mc.ingameGUI.addChatMessage(ChatType.SYSTEM,
					new TextComponentString(TextFormatting.RED + "LotTweaks: failed to add blocks"));
		}
	}

	private void executeReload() throws CommandException {
		RotationHelper.loadFromFile();
		RotationHelper.loadBlockGroups();
		Minecraft mc = Minecraft.getMinecraft();
		int groupCount = RotationHelper.BLOCK_CHAIN.size();
		if (groupCount > 0) {
			mc.ingameGUI.addChatMessage(ChatType.SYSTEM,
					new TextComponentString("LotTweaks: reload succeeded!"));
		} else {
			mc.ingameGUI.addChatMessage(ChatType.SYSTEM,
					new TextComponentString(TextFormatting.RED + "LotTweaks: failed to reload config file (0 blocks loaded)"));
		}
	}

	@Override
	public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
		return false;
	}

}
