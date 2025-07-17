package com.github.lotqwerty.lottweaks.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;
import java.util.Optional;

import com.github.lotqwerty.lottweaks.LotTweaks;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Holder;

public class RotationHelper {

	public static final String ITEMGROUP_CONFFILE_PRIMARY = "LotTweaks-BlockGroups.txt";
	private static final String ITEMGROUP_CONFFILE_SECONDARY = "LotTweaks-BlockGroups2.txt";

	private static final HashMap<Item, Item> ITEM_CHAIN_PRIMARY = new HashMap<>();
	private static final HashMap<Item, Item> ITEM_CHAIN_SECONDARY = new HashMap<>();
	private static final String[] DEFAULT_ITEM_GROUP_STRLIST_PRIMARY = {
		"//■ LotTweaks BlockGroups (PRIMARY)",
		"//VANILLA BLOCKS",
		"//STONE",
		"minecraft:stone,minecraft:granite,minecraft:polished_granite,minecraft:diorite,minecraft:polished_diorite,minecraft:andesite,minecraft:polished_andesite",
		"//DIRT",
		"minecraft:dirt,minecraft:coarse_dirt,minecraft:podzol,minecraft:crimson_nylium,minecraft:warped_nylium",
		"//PLANKS",
		"minecraft:oak_planks,minecraft:spruce_planks,minecraft:birch_planks,minecraft:jungle_planks,minecraft:acacia_planks,minecraft:dark_oak_planks,minecraft:mangrove_planks,minecraft:crimson_planks,minecraft:warped_planks",
		"//SAPLINGS",
		"minecraft:oak_sapling,minecraft:spruce_sapling,minecraft:birch_sapling,minecraft:jungle_sapling,minecraft:acacia_sapling,minecraft:dark_oak_sapling,minecraft:mangrove_propagule",
		"//ORE",
		"minecraft:coal_ore,minecraft:iron_ore,minecraft:copper_ore,minecraft:gold_ore,minecraft:redstone_ore,minecraft:emerald_ore,minecraft:lapis_ore,minecraft:diamond_ore,minecraft:deepslate_coal_ore,minecraft:deepslate_iron_ore,minecraft:deepslate_copper_ore,minecraft:deepslate_gold_ore,minecraft:deepslate_redstone_ore,minecraft:deepslate_emerald_ore,minecraft:deepslate_lapis_ore,minecraft:deepslate_diamond_ore",
		"//RAW BLOCKS",
		"minecraft:raw_iron_block,minecraft:raw_copper_block,minecraft:raw_gold_block",
		"//COPPER",
		"minecraft:copper_block,minecraft:exposed_copper,minecraft:weathered_copper,minecraft:oxidized_copper",
		"//CUT COPPER",
		"minecraft:cut_copper,minecraft:exposed_cut_copper,minecraft:weathered_cut_copper,minecraft:oxidized_cut_copper",
		"//CUT COPPER STAIRS",
		"minecraft:cut_copper_stairs,minecraft:exposed_cut_copper_stairs,minecraft:weathered_cut_copper_stairs,minecraft:oxidized_cut_copper_stairs",
		"//CUT COPPER SLAB",
		"minecraft:cut_copper_slab,minecraft:exposed_cut_copper_slab,minecraft:weathered_cut_copper_slab,minecraft:oxidized_cut_copper_slab",
		"//WAXED COPPER",
		"minecraft:waxed_copper_block,minecraft:waxed_exposed_copper,minecraft:waxed_weathered_copper,minecraft:waxed_oxidized_copper",
		"//WAXED CUT COPPER",
		"minecraft:waxed_cut_copper,minecraft:waxed_exposed_cut_copper,minecraft:waxed_weathered_cut_copper,minecraft:waxed_oxidized_cut_copper",
		"//WAXED CUT COPPER STAIRS",
		"minecraft:waxed_cut_copper_stairs,minecraft:waxed_exposed_cut_copper_stairs,minecraft:waxed_weathered_cut_copper_stairs,minecraft:waxed_oxidized_cut_copper_stairs",
		"//WAXED CUT COPPER SLAB",
		"minecraft:waxed_cut_copper_slab,minecraft:waxed_exposed_cut_copper_slab,minecraft:waxed_weathered_cut_copper_slab,minecraft:waxed_oxidized_cut_copper_slab",
		"//LOG",
		"minecraft:oak_log,minecraft:spruce_log,minecraft:birch_log,minecraft:jungle_log,minecraft:acacia_log,minecraft:dark_oak_log,minecraft:mangrove_log,minecraft:crimson_stem,minecraft:warped_stem",
		"//STRIPPED LOG",
		"minecraft:stripped_oak_log,minecraft:stripped_spruce_log,minecraft:stripped_birch_log,minecraft:stripped_jungle_log,minecraft:stripped_acacia_log,minecraft:stripped_dark_oak_log,minecraft:stripped_mangrove_log,minecraft:stripped_crimson_stem,minecraft:stripped_warped_stem",
		"//STRIPPED WOOD",
		"minecraft:stripped_oak_wood,minecraft:stripped_spruce_wood,minecraft:stripped_birch_wood,minecraft:stripped_jungle_wood,minecraft:stripped_acacia_wood,minecraft:stripped_dark_oak_wood,minecraft:stripped_mangrove_wood,minecraft:stripped_crimson_hyphae,minecraft:stripped_warped_hyphae",
		"//WOOL",
		toColorVariationsStr("minecraft:COLOR_wool"),
		"//WOOD",
		"minecraft:oak_wood,minecraft:spruce_wood,minecraft:birch_wood,minecraft:jungle_wood,minecraft:acacia_wood,minecraft:dark_oak_wood,minecraft:mangrove_wood,minecraft:crimson_hyphae,minecraft:warped_hyphae",
		"//SPONGE",
		"minecraft:sponge,minecraft:wet_sponge",
		"//SANDSTONE",
		"minecraft:sandstone,minecraft:chiseled_sandstone,minecraft:cut_sandstone",
		"//FLOWER",
		"minecraft:dandelion,minecraft:poppy,minecraft:blue_orchid,minecraft:allium,minecraft:azure_bluet,minecraft:red_tulip,minecraft:orange_tulip,minecraft:white_tulip,minecraft:pink_tulip,minecraft:oxeye_daisy,minecraft:cornflower,minecraft:lily_of_the_valley,minecraft:wither_rose",
		"//Mineral Blocks",
		"minecraft:gold_block,minecraft:iron_block,minecraft:coal_block,minecraft:lapis_block,minecraft:diamond_block,minecraft:redstone_block,minecraft:emerald_block",
		"//WOODEN SLAB",
		"minecraft:oak_slab,minecraft:spruce_slab,minecraft:birch_slab,minecraft:jungle_slab,minecraft:acacia_slab,minecraft:dark_oak_slab,minecraft:mangrove_slab,minecraft:crimson_slab,minecraft:warped_slab",
		"//STONE SLAB",
		"minecraft:stone_slab,minecraft:smooth_stone_slab,minecraft:granite_slab,minecraft:polished_granite_slab,minecraft:diorite_slab,minecraft:polished_diorite_slab,minecraft:andesite_slab,minecraft:polished_andesite_slab",
		"//SANDSTONE SLAB",
		"minecraft:sandstone_slab,minecraft:cut_sandstone_slab,minecraft:smooth_sandstone_slab",
		"//RED SANDSTONE SLAB",
		"minecraft:red_sandstone_slab,minecraft:cut_red_sandstone_slab,minecraft:smooth_red_sandstone_slab",
		"//PRISMARINE SLAB",
		"minecraft:prismarine_slab,minecraft:prismarine_brick_slab,minecraft:dark_prismarine_slab",
		"//BLACKSTONE SLAB",
		"minecraft:blackstone_slab,minecraft:polished_blackstone_slab,minecraft:polished_blackstone_brick_slab",
		"//OTHER SLAB",
		"minecraft:petrified_oak_slab,minecraft:cobblestone_slab,minecraft:brick_slab,minecraft:stone_brick_slab,minecraft:mud_brick_slab,minecraft:nether_brick_slab,minecraft:quartz_slab,minecraft:purpur_slab,minecraft:mossy_stone_brick_slab,minecraft:mossy_cobblestone_slab,minecraft:end_stone_brick_slab,minecraft:smooth_quartz_slab,minecraft:red_nether_brick_slab",
		"//WOODEN STAIRS",
		"minecraft:oak_stairs,minecraft:spruce_stairs,minecraft:birch_stairs,minecraft:jungle_stairs,minecraft:mangrove_stairs,minecraft:crimson_stairs,minecraft:warped_stairs",
		"//WOODEN DOOR",
		"minecraft:oak_door,minecraft:spruce_door,minecraft:birch_door,minecraft:jungle_door,minecraft:acacia_door,minecraft:dark_oak_door,minecraft:crimson_door,minecraft:warped_door",
		"//WOODEN FENCE",
		"minecraft:oak_fence,minecraft:spruce_fence,minecraft:birch_fence,minecraft:jungle_fence,minecraft:acacia_fence,minecraft:dark_oak_fence,minecraft:mangrove_fence,minecraft:crimson_fence,minecraft:warped_fence",
		"//STAINED_GLASS",
		toColorVariationsStr("minecraft:COLOR_stained_glass"),
		"//INFESTED STONE BRICKS",
		"minecraft:infested_stone_bricks,minecraft:infested_mossy_stone_bricks,minecraft:infested_cracked_stone_bricks,minecraft:infested_chiseled_stone_bricks",
		"//FENCE GATE",
		"minecraft:oak_fence_gate,minecraft:spruce_fence_gate,minecraft:birch_fence_gate,minecraft:jungle_fence_gate,minecraft:acacia_fence_gate,minecraft:dark_oak_fence_gate,minecraft:crimson_fence_gate,minecraft:warped_fence_gate",
		"//STONEBRICKS",
		"minecraft:stone_bricks,minecraft:mossy_stone_bricks,minecraft:cracked_stone_bricks,minecraft:chiseled_stone_bricks",
		"//WALL",
		"minecraft:cobblestone_wall,minecraft:mossy_cobblestone_wall,minecraft:brick_wall,minecraft:prismarine_wall,minecraft:red_sandstone_wall,minecraft:mossy_stone_brick_wall,minecraft:granite_wall,minecraft:stone_brick_wall,minecraft:mud_brick_wall,minecraft:nether_brick_wall,minecraft:andesite_wall,minecraft:red_nether_brick_wall,minecraft:sandstone_wall,minecraft:end_stone_brick_wall,minecraft:diorite_wall,minecraft:blackstone_wall,minecraft:polished_blackstone_wall,minecraft:polished_blackstone_brick_wall",
		"//CARPET",
		toColorVariationsStr("minecraft:COLOR_carpet"),
		"//QUARTZ BLOCK",
		"minecraft:chiseled_quartz_block,minecraft:quartz_block,minecraft:quartz_bricks,minecraft:quartz_pillar",
		"//TERRACOTTA",
		"minecraft:terracotta," + toColorVariationsStr("minecraft:COLOR_terracotta"),
		"//STAINED GLASS PANE",
		toColorVariationsStr("minecraft:COLOR_stained_glass_pane"),
		"//PRISMARINE",
		"minecraft:prismarine,minecraft:prismarine_bricks,minecraft:dark_prismarine",
		"//SHULKER BOX",
		"minecraft:shulker_box," + toColorVariationsStr("minecraft:COLOR_shulker_box"),
		"//GLAZED TERRACOTTA",
		toColorVariationsStr("minecraft:COLOR_glazed_terracotta"),
		"//CONCRETE",
		toColorVariationsStr("minecraft:COLOR_concrete"),
		"//CONCRETE POWDER",
		toColorVariationsStr("minecraft:COLOR_concrete_powder"),
		"//CORAL BLOCK",
		"minecraft:dead_tube_coral_block,minecraft:dead_brain_coral_block,minecraft:dead_bubble_coral_block,minecraft:dead_fire_coral_block,minecraft:dead_horn_coral_block,minecraft:tube_coral_block,minecraft:brain_coral_block,minecraft:bubble_coral_block,minecraft:fire_coral_block,minecraft:horn_coral_block",
		"//BLACKSTONE",
		"minecraft:blackstone,minecraft:polished_blackstone,minecraft:chiseled_polished_blackstone,minecraft:polished_blackstone_bricks,minecraft:cracked_polished_blackstone_bricks",
		"//CORAL",
		"minecraft:tube_coral,minecraft:brain_coral,minecraft:bubble_coral,minecraft:fire_coral,minecraft:horn_coral,minecraft:dead_brain_coral,minecraft:dead_bubble_coral,minecraft:dead_fire_coral,minecraft:dead_horn_coral,minecraft:dead_tube_coral",
		"//CORAL FAN",
		"minecraft:tube_coral_fan,minecraft:brain_coral_fan,minecraft:bubble_coral_fan,minecraft:fire_coral_fan,minecraft:horn_coral_fan,minecraft:dead_tube_coral_fan,minecraft:dead_brain_coral_fan,minecraft:dead_bubble_coral_fan,minecraft:dead_fire_coral_fan,minecraft:dead_horn_coral_fan",
		"//BUTTON",
		"minecraft:stone_button,minecraft:oak_button,minecraft:spruce_button,minecraft:birch_button,minecraft:jungle_button,minecraft:acacia_button,minecraft:dark_oak_button,minecraft:crimson_button,minecraft:warped_button,minecraft:polished_blackstone_button",
		"//SIGN",
		"minecraft:oak_sign,minecraft:spruce_sign,minecraft:birch_sign,minecraft:jungle_sign,minecraft:acacia_sign,minecraft:dark_oak_sign,minecraft:mangrove_sign,minecraft:crimson_sign,minecraft:warped_sign",
		"//LANTERN",
		"minecraft:lantern,minecraft:soul_lantern",
		"//CAMPFIRE",
		"minecraft:campfire,minecraft:soul_campfire",
		"//VANILLA ITEMS",
		"//TOOLS",
		"minecraft:wooden_axe,minecraft:compass,minecraft:clock",
		"//CANDLES",
		"minecraft:candle," + toColorVariationsStr("minecraft:COLOR_candle"),
		"//FROGLIGHT",
		"minecraft:ochre_froglight,minecraft:verdant_froglight,minecraft:pearlescent_froglight",
	};

	private static final String[] DEFAULT_ITEM_GROUP_STRLIST_SECONDARY = {
		"//■ LotTweaks BlockGroups (SECONDARY)",
		"//WHITE",
		toSameColorsStr("white"),
		"//ORANGE",
		toSameColorsStr("orange"),
		"//MAGENTA",
		toSameColorsStr("magenta"),
		"//LIGHT BLUE",
		toSameColorsStr("light_blue"),
		"//YELLOW",
		toSameColorsStr("yellow"),
		"//LIME",
		toSameColorsStr("lime"),
		"//PINK",
		toSameColorsStr("pink"),
		"//GRAY",
		toSameColorsStr("gray"),
		"//LIGHT GRAY",
		toSameColorsStr("light_gray"),
		"//CYAN",
		toSameColorsStr("cyan"),
		"//PURPLE",
		toSameColorsStr("purple"),
		"//BLUE",
		toSameColorsStr("blue"),
		"//BROWN",
		toSameColorsStr("brown"),
		"//GREEN",
		toSameColorsStr("green"),
		"//RED",
		toSameColorsStr("red"),
		"//BLACK",
		toSameColorsStr("black"),
		"//COPPER",
		"minecraft:copper_block,minecraft:cut_copper,minecraft:cut_copper_stairs,minecraft:cut_copper_slab",
		"//WAXED COPPER",
		"minecraft:waxed_copper_block,minecraft:waxed_cut_copper,minecraft:waxed_cut_copper_stairs,minecraft:waxed_cut_copper_slab",
		"//EXPOSED COPPER",
		"minecraft:exposed_copper,minecraft:exposed_cut_copper,minecraft:exposed_cut_copper_stairs,minecraft:exposed_cut_copper_slab",
		"//WAXED EXPOSED COPPER",
		"minecraft:waxed_exposed_copper,minecraft:waxed_exposed_cut_copper,minecraft:waxed_exposed_cut_copper_stairs,minecraft:waxed_exposed_cut_copper_slab",
		"//WEATHERED COPPER",
		"minecraft:weathered_copper,minecraft:weathered_cut_copper,minecraft:weathered_cut_copper_stairs,minecraft:weathered_cut_copper_slab",
		"//WAXED WEATHERED COPPER",
		"minecraft:waxed_weathered_copper,minecraft:waxed_weathered_cut_copper,minecraft:waxed_weathered_cut_copper_stairs,minecraft:waxed_weathered_cut_copper_slab",
		"//OXIDIZED COPPER",
		"minecraft:oxidized_copper,minecraft:oxidized_cut_copper,minecraft:oxidized_cut_copper_stairs,minecraft:oxidized_cut_copper_slab",
		"//WAXED OXIDIZED COPPER",
		"minecraft:waxed_oxidized_copper,minecraft:waxed_oxidized_cut_copper,minecraft:waxed_oxidized_cut_copper_stairs,minecraft:waxed_oxidized_cut_copper_slab",
	};

	public static List<String> ITEM_GROUPS_STRLIST_PRIMARY = new ArrayList<>(Arrays.asList(DEFAULT_ITEM_GROUP_STRLIST_PRIMARY));
	private static final List<String> ITEM_GROUPS_STRLIST_SECONDARY = new ArrayList<>(Arrays.asList(DEFAULT_ITEM_GROUP_STRLIST_SECONDARY));

	public static final List<String> LOG_GROUP_CONFIG = new ArrayList<>();

	public enum Group {
		PRIMARY,
		SECONDARY,
	}

	private static void warnGroupConfigErrors(String msg, int lineCount, Group group) {
		String fullMsg =  String.format("%s (Line %d of %s group)", msg, lineCount, group.name());
		LOG_GROUP_CONFIG.add(fullMsg);
		LotTweaks.LOGGER.warn(fullMsg);
	}

	private static String toColorVariationsStr(String name) {
		StringJoiner joiner = new StringJoiner(",");
		String[] colors = new String[] {"red", "orange", "yellow", "lime", "green", "cyan", "light_blue", "blue", "purple", "magenta", "pink", "brown", "black", "gray", "light_gray", "white"};
		for (String color : colors) {
			joiner.add(name.replace("COLOR", color));
		}
		return joiner.toString();
	}

	private static String toSameColorsStr(String color) {
		String format = "minecraft:COLOR_wool,minecraft:COLOR_stained_glass,minecraft:COLOR_terracotta,minecraft:COLOR_stained_glass_pane,minecraft:COLOR_carpet,minecraft:COLOR_concrete,minecraft:COLOR_concrete_powder,minecraft:COLOR_shulker_box";
		return format.replace("COLOR", color);
	}

	private static HashMap<Item, Item> getItemChain(Group group) {
		return (group == Group.PRIMARY) ? ITEM_CHAIN_PRIMARY : ITEM_CHAIN_SECONDARY;
	}

	private static List<String> getItemGroupStrList(Group group) {
		return (group == Group.PRIMARY) ? ITEM_GROUPS_STRLIST_PRIMARY : ITEM_GROUPS_STRLIST_SECONDARY;
	}

	private static String getFileName(Group group) {
		return (group == Group.PRIMARY) ? ITEMGROUP_CONFFILE_PRIMARY : ITEMGROUP_CONFFILE_SECONDARY;
	}

	public static boolean canRotate(ItemStack itemStack, Group group) {
		if (itemStack == null || itemStack.isEmpty()) {
			return false;
		}
		Item item = itemStack.getItem();
		if (item == null || item == Items.AIR) {
			return false;
		}
		return getItemChain(group).containsKey(item);
	}
	
	private static ItemStack toItemStack(Item item) {
		return new ItemStack(item);
	}

	public static List<ItemStack> getAllRotateResult(ItemStack itemStack, Group group){
		List<ItemStack> stacks = new ArrayList<>();
		if (itemStack == null || itemStack.isEmpty()) {
			return null;
		}
		Item srcItem = itemStack.getItem();
		if (srcItem == null || srcItem == Items.AIR) {
			return null;
		}
		if (!getItemChain(group).containsKey(srcItem)) {
			return null;
		}
		stacks.add(itemStack);
		Item item = getItemChain(group).get(srcItem);
		int counter = 0;
		while (item != srcItem) {
			stacks.add(toItemStack(item));
			item = getItemChain(group).get(item);
			counter++;
			if (counter >= 50000) {
				LotTweaks.LOGGER.error("infinite loop!");
				return null;
			}
		}
		return stacks;
	}

	public static boolean loadAllItemGroupFromStrArray() {
		LOG_GROUP_CONFIG.clear();
		boolean flag = true;
		for(Group group : Group.values()) {
			flag &= loadItemGroupFromStrArray(group);
		}
		return flag;
	}

	private static boolean loadItemGroupFromStrArray(Group group) {
		HashMap<Item, Item> newItemChain = new HashMap<>();
		try {
			int lineCount = 0;
			for (String line: getItemGroupStrList(group)) {
				lineCount++;
				if (line.isEmpty() || line.startsWith("//")) {
					continue;
				}
				List<Item> items = new ArrayList<>();
				for (String itemStr: line.split(",")) {
					ResourceLocation resourceLocation = ResourceLocation.parse(itemStr);
					Optional<Holder.Reference<Item>> itemHolder = BuiltInRegistries.ITEM.get(resourceLocation);
					if (itemHolder.isEmpty()) {
						warnGroupConfigErrors(String.format("'%s' is not supported.", itemStr), lineCount, group);
						continue;
					}
					Item item = itemHolder.get().value();
					if (item == null || item == Items.AIR) {
						warnGroupConfigErrors(String.format("'%s' is not supported.", itemStr), lineCount, group);
						continue;
					}
					if (items.contains(item) || newItemChain.containsKey(item)) {
						warnGroupConfigErrors(String.format("'%s' is duplicated.", itemStr), lineCount, group);
						continue;
					}
					items.add(item);
				}
				if (items.size() <= 1) {
					warnGroupConfigErrors(String.format("The group size is %d.", items.size()), lineCount, group);
					continue;
				}
				for (int i=0;i<items.size();i++) {
					newItemChain.put(items.get(i), items.get((i+1)%items.size()));
				}
			}
		} catch (Exception e) {
			LotTweaks.LOGGER.error("Error occurred while loading item group from string array", e);
			return false;
		}
		getItemChain(group).clear();
		getItemChain(group).putAll(newItemChain);
		return true;
	}

	public static boolean tryToAddItemGroupFromCommand(String newItemGroup, Group group) {
		List<String> strList = getItemGroupStrList(group);
		strList.add(newItemGroup);
		boolean succeeded = loadItemGroupFromStrArray(group);
		if (succeeded) {
			writeToFile(group);
			return true;
		} else {
			strList.remove(strList.size()-1);
			return false;
		}
	}

	public static boolean loadAllFromFile() {
		boolean flag = true;
		for(Group group : Group.values()) {
			flag &= loadFromFile(group);
		}
		return flag;
	}

	private static boolean loadFromFile(Group group) {
		File file = new File(new File("config"), getFileName(group));
		try {
			if (!file.exists()) {
				LotTweaks.LOGGER.debug("Config file does not exist.");
				writeToFile(group);
			} else {
				List<String> listFromFile = loadFile(file);
				List<String> listOnMemory = getItemGroupStrList(group);
				listOnMemory.clear();
				listOnMemory.addAll(listFromFile);
			}
		} catch (IOException e) {
			LotTweaks.LOGGER.error(String.format("Failed to load config from file (Group: %s)", group.name()));
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static List<String> loadFile(File file) throws IOException{
		try {
			return Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
		} catch (IOException e) {
		}
		try {
			return Files.readAllLines(file.toPath(), Charset.forName("Shift_JIS"));
		} catch (IOException e) {
		}
		return Files.readAllLines(file.toPath(), Charset.defaultCharset());
	}

	public static void writeAllToFile() {
		for(Group group : Group.values()) {
			writeToFile(group);
		}
	}

	private static void writeToFile(Group group) {
		LotTweaks.LOGGER.debug("Write config to file.");
		String filename = (group == Group.PRIMARY ? ITEMGROUP_CONFFILE_PRIMARY : ITEMGROUP_CONFFILE_SECONDARY);
		File file = new File(new File("config"), filename);
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
			for (String line: getItemGroupStrList(group)) {
				writer.append(line);
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			LotTweaks.LOGGER.error("Failed to write config to file");
			e.printStackTrace();
			return;
		}
		LotTweaks.LOGGER.debug("Finished.");
	}
	
}
