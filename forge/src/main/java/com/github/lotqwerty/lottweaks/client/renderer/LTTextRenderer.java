package com.github.lotqwerty.lottweaks.client.renderer;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class LTTextRenderer {

	public static void showServerSideRequiredMessage(GuiGraphics guiGraphics, String requiredVersion) {
		showServerSideRequiredMessage(guiGraphics, Minecraft.getInstance().getWindow(), requiredVersion);
	}

	public static void showServerSideRequiredMessage(GuiGraphics guiGraphics, Window scaledResolution, String requiredVersion) {
		showMessage(guiGraphics, scaledResolution, String.format("[LotTweaks] Server-side installation (%s or later) is required.", requiredVersion), 0xFF9090);
	}

	public static void showMessage(GuiGraphics guiGraphics, Window scaledResolution, String msg) {
		showMessage(guiGraphics, scaledResolution, msg, 0xFFFFFF);
	}

	private static void showMessage(GuiGraphics guiGraphics, Window scaledResolution, String msg, int color) {
		Minecraft mc = Minecraft.getInstance();
		int x = (scaledResolution.getGuiScaledWidth() - mc.font.width(msg)) / 2;
		int y = scaledResolution.getGuiScaledHeight() - 70;
		guiGraphics.drawString(mc.font, msg, x, y, color);
	}

}
