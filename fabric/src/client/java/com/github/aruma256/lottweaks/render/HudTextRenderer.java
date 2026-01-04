package com.github.aruma256.lottweaks.render;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.ARGB;

public class HudTextRenderer {

	// Vertical offset from bottom of screen
	private static final int MESSAGE_Y_OFFSET_FROM_BOTTOM = 70;

	// Color constants
	private static final int COLOR_DEFAULT = 0xFFFFFF;
	private static final int COLOR_ERROR = 0xFF9090;

	public static void showServerSideRequiredMessage(GuiGraphics guiGraphics, String requiredVersion) {
		showServerSideRequiredMessage(guiGraphics, Minecraft.getInstance().getWindow(), requiredVersion);
	}

	public static void showServerSideRequiredMessage(GuiGraphics guiGraphics, Window scaledResolution, String requiredVersion) {
		showMessage(guiGraphics, scaledResolution, String.format("[LotTweaks] Server-side installation (%s or later) is required.", requiredVersion), COLOR_ERROR);
	}

	public static void showMessage(GuiGraphics guiGraphics, Window scaledResolution, String msg) {
		showMessage(guiGraphics, scaledResolution, msg, COLOR_DEFAULT);
	}

	private static void showMessage(GuiGraphics guiGraphics, Window scaledResolution, String msg, int color) {
		Minecraft mc = Minecraft.getInstance();
		int x = (scaledResolution.getGuiScaledWidth() - mc.font.width(msg)) / 2;
		int y = scaledResolution.getGuiScaledHeight() - MESSAGE_Y_OFFSET_FROM_BOTTOM;
		guiGraphics.drawString(mc.font, msg, x, y, ARGB.opaque(color));
	}

}
