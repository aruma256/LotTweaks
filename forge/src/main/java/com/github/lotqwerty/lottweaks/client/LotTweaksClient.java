package com.github.lotqwerty.lottweaks.client;

import org.lwjgl.glfw.GLFW;

import com.github.lotqwerty.lottweaks.LotTweaks;
import com.github.lotqwerty.lottweaks.client.keys.ExPickKey;
import com.github.lotqwerty.lottweaks.client.keys.AdjustRangeKey;
import com.github.lotqwerty.lottweaks.client.keys.ReplaceKey;
import com.github.lotqwerty.lottweaks.client.keys.RotateKey;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class LotTweaksClient
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

	public static void init() {
		for (KeyMapping key : keyMappings) {
			MinecraftForge.EVENT_BUS.register(key);
		}
		//
		MinecraftForge.EVENT_BUS.register(new LotTweaksClient());
		//
		MinecraftForge.EVENT_BUS.register(new LotTweaksCommand());
	}

	public static void onRegisterKeyMappingsEvent(RegisterKeyMappingsEvent event) {
		for (KeyMapping key : keyMappings) {
			event.register(key);
		}
	}

	public static void onRegisterGuiOverlaysEvent(RegisterGuiOverlaysEvent event) {
		for (KeyMapping key : keyMappings) {
			if (key instanceof IGuiOverlay) {
				event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), key.getName(), (IGuiOverlay)key);
			}
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
		if (LotTweaks.CONFIG.SHOW_BLOCKCONFIG_ERROR_LOG_TO_CHAT.get()) {
			Minecraft mc = Minecraft.getInstance();
			for (String line : RotationHelper.LOG_GROUP_CONFIG) {
				mc.getChatListener().handleSystemMessage(Component.literal(String.format("LotTweaks: %s%s", ChatFormatting.RED, line)), false);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerLoggedIn(final ClientPlayerNetworkEvent.LoggingIn event) {
		showErrorLogToChat();
	}

	@SubscribeEvent
	public void onClientDisconnectionFromServer(final ClientPlayerNetworkEvent.LoggingOut event) {
		clearServerVersion();
	}

}
