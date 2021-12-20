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
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;

@Environment(EnvType.CLIENT)
public class LotTweaksClient implements ClientModInitializer, ClientPlayConnectionEvents.Join, ClientPlayConnectionEvents.Disconnect
{
	private static String serverVersion = "0";

	@Override
	public void onInitializeClient() {
		RotationHelper.loadAllFromFile();
		RotationHelper.loadAllItemGroupFromStrArray();
		//
		KeyMapping key;
		key = new ExPickKey(GLFW.GLFW_KEY_V, LotTweaks.NAME);
		registerToMyEventBus(key);
		KeyBindingHelper.registerKeyBinding(key);
		key = new RotateKey(GLFW.GLFW_KEY_R, LotTweaks.NAME);
		registerToMyEventBus(key);
		KeyBindingHelper.registerKeyBinding(key);
		key = new ReplaceKey(GLFW.GLFW_KEY_G, LotTweaks.NAME);
		registerToMyEventBus(key);
		KeyBindingHelper.registerKeyBinding(key);
		key = new AdjustRangeKey(GLFW.GLFW_KEY_U, LotTweaks.NAME);
		registerToMyEventBus(key);
		KeyBindingHelper.registerKeyBinding(key);
		//
		ClientPlayConnectionEvents.JOIN.register(this);
		ClientPlayConnectionEvents.DISCONNECT.register(this);
		//
		registerToMyEventBus(new LotTweaksCommand());
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
				mc.gui.handleChat(ChatType.SYSTEM, new TextComponent(String.format("LotTweaks: %s%s", ChatFormatting.RED, line)), Util.NIL_UUID);
			}
		}
	}

	@Override
	public void onPlayReady(ClientPacketListener handler, PacketSender sender, Minecraft client) {
		showErrorLogToChat();
		AdjustRangeKey.resetRange();
	}

	@Override
	public void onPlayDisconnect(ClientPacketListener handler, Minecraft client) {
		clearServerVersion();
	}

}
