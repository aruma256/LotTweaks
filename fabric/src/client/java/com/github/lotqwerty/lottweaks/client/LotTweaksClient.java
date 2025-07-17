package com.github.lotqwerty.lottweaks.client;

import org.lwjgl.glfw.GLFW;

import com.github.lotqwerty.lottweaks.LotTweaks;
import com.github.lotqwerty.lottweaks.client.keys.AdjustRangeKey;
import com.github.lotqwerty.lottweaks.client.keys.ExPickKey;
import com.github.lotqwerty.lottweaks.client.keys.ReplaceKey;
import com.github.lotqwerty.lottweaks.client.keys.RotateKey;
import com.github.lotqwerty.lottweaks.fabric.ClientChatEvent;
import com.github.lotqwerty.lottweaks.fabric.ClientChatEvent.ClientChatEventListener;
import com.github.lotqwerty.lottweaks.fabric.DrawBlockOutlineEvent;
import com.github.lotqwerty.lottweaks.fabric.DrawBlockOutlineEvent.DrawBlockOutlineListener;
import com.github.lotqwerty.lottweaks.fabric.RenderHotbarEvent;
import com.github.lotqwerty.lottweaks.fabric.ScrollEvent;
import com.github.lotqwerty.lottweaks.fabric.RenderHotbarEvent.RenderHotbarListener;
import com.github.lotqwerty.lottweaks.fabric.ScrollEvent.ScrollListener;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class LotTweaksClient implements ClientModInitializer, ClientPlayConnectionEvents.Init, ClientPlayConnectionEvents.Join
{
	private static String serverVersion = "0";
	private static KeyMapping[] keyMappings = {
			new ExPickKey(GLFW.GLFW_KEY_V, LotTweaks.NAME),
			new RotateKey(GLFW.GLFW_KEY_R, LotTweaks.NAME),
			new ReplaceKey(GLFW.GLFW_KEY_G, LotTweaks.NAME),
			new AdjustRangeKey(GLFW.GLFW_KEY_U, LotTweaks.NAME)
	};

	public LotTweaksClient() {
	}

	@Override
	public void onInitializeClient() {
		RotationHelper.loadAllFromFile();
		RotationHelper.loadAllItemGroupFromStrArray();
		//
		for (KeyMapping key : keyMappings) {
			registerKey(key);
		}
		//
		ClientPlayConnectionEvents.INIT.register(this);
		ClientPlayConnectionEvents.JOIN.register(this);
		//
		ClientCommandRegistrationCallback.EVENT.register(new LotTweaksCommand());
		//
		LTPacketHandlerClient.initClient();
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
		if (obj instanceof ClientChatEventListener) {
			ClientChatEvent.registerListener((ClientChatEventListener)obj);
		}
		if (obj instanceof DrawBlockOutlineListener) {
			DrawBlockOutlineEvent.registerListener((DrawBlockOutlineListener)obj);
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
			for (String line : RotationHelper.LOG_GROUP_CONFIG) {
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
		showErrorLogToChat();
		AdjustRangeKey.resetRange();
	}

}
