package com.github.aruma256.lottweaks;

import org.lwjgl.glfw.GLFW;

import com.github.aruma256.lottweaks.event.RenderHotbarEvent;
import com.github.aruma256.lottweaks.event.RenderHotbarEvent.RenderHotbarListener;
import com.github.aruma256.lottweaks.event.ScrollEvent;
import com.github.aruma256.lottweaks.event.ScrollEvent.ScrollListener;
import com.github.aruma256.lottweaks.keybinding.PaletteKey;
import com.github.aruma256.lottweaks.keybinding.PickHistory;
import com.github.aruma256.lottweaks.keybinding.ReachExtensionKey;
import com.github.aruma256.lottweaks.keybinding.ReplaceBlockKey;
import com.github.aruma256.lottweaks.keybinding.SmartPickKey;
import com.github.aruma256.lottweaks.palette.PaletteConfigManager;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class LotTweaksClient implements ClientModInitializer, ClientPlayConnectionEvents.Init, ClientPlayConnectionEvents.Join
{
	private static String serverVersion = "0";
	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("lottweaks", "keys"));
	private static final PickHistory PICK_HISTORY = new PickHistory();
	private static final ReplaceBlockKey REPLACE_BLOCK_KEY = new ReplaceBlockKey(GLFW.GLFW_KEY_G, CATEGORY, PICK_HISTORY);
	private static KeyMapping[] keyMappings = {
			SmartPickKey.create(GLFW.GLFW_KEY_V, CATEGORY, PICK_HISTORY),
			new PaletteKey(GLFW.GLFW_KEY_R, CATEGORY),
			REPLACE_BLOCK_KEY,
			new ReachExtensionKey(GLFW.GLFW_KEY_U, CATEGORY)
	};

	public LotTweaksClient() {
	}

	@Override
	public void onInitializeClient() {
		PaletteConfigManager.loadAllFromFile();
		//
		for (KeyMapping key : keyMappings) {
			registerKey(key);
		}
		//
		WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, outlineRenderState) -> {
			return REPLACE_BLOCK_KEY.renderBlockOutline(context, outlineRenderState);
		});
		//
		ClientPlayConnectionEvents.INIT.register(this);
		ClientPlayConnectionEvents.JOIN.register(this);
		//
		ClientCommandRegistrationCallback.EVENT.register(new LotTweaksCommand());
		//
		ModNetworkClient.initClient();
	}

	private static void registerToMyEventBus(Object obj) {
		if (obj instanceof ClientTickEvents.EndTick) {
			ClientTickEvents.END_CLIENT_TICK.register((ClientTickEvents.EndTick)obj);
		}
		if (obj instanceof RenderHotbarListener) {
			RenderHotbarEvent.registerListener((RenderHotbarListener)obj);
		}
		if (obj instanceof ScrollListener) {
			ScrollEvent.registerListener((ScrollListener)obj);
		}
	}

	private static void registerKey(KeyMapping key) {
		registerToMyEventBus(key);
		KeyBindingHelper.registerKeyBinding(key);
	}

	public static boolean requireServerVersion(String requiredVersion) {
		return (serverVersion.compareTo(requiredVersion) >= 0);
	}

	public static void clearServerVersion() {
		setServerVersion("0");
	}

	public static void setServerVersion(String version) {
		serverVersion = version;
	}

	public static String getServerVersion() {
		return serverVersion;
	}

	public static void showErrorLogToChat() {
		if (LotTweaks.CONFIG.SHOW_BLOCKCONFIG_ERROR_LOG_TO_CHAT) {
			Minecraft mc = Minecraft.getInstance();
			for (String line : PaletteConfigManager.LOG_CONFIG_WARNINGS) {
				mc.getChatListener().handleSystemMessage(Component.literal(String.format("LotTweaks: %s%s", ChatFormatting.RED, line)), false);
			}
		}
	}

	@Override
	public void onPlayInit(ClientPacketListener handler, Minecraft client) {
		clearServerVersion();
	}

	@Override
	public void onPlayReady(ClientPacketListener handler, PacketSender sender, Minecraft client) {
		// Reload config now that dynamic registries (enchantments, etc.) are available
		PaletteConfigManager.loadAllFromFile();
		showErrorLogToChat();
		ReachExtensionKey.resetRange();
	}

}
