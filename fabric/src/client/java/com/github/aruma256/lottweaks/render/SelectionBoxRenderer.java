package com.github.aruma256.lottweaks.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SelectionBoxRenderer {

	// Outline expansion to prevent z-fighting with block faces
	private static final double OUTLINE_GROW_FACTOR = 0.0020000000949949026D / 2;

	// Selection box outline color (red)
	private static final float OUTLINE_RED = 1.0F;
	private static final float OUTLINE_GREEN = 0.0F;
	private static final float OUTLINE_BLUE = 0.0F;
	private static final float OUTLINE_ALPHA = 0.4F;

	private static final VoxelShape CUBE = Shapes.box(
		-OUTLINE_GROW_FACTOR, -OUTLINE_GROW_FACTOR, -OUTLINE_GROW_FACTOR,
		1 + OUTLINE_GROW_FACTOR, 1 + OUTLINE_GROW_FACTOR, 1 + OUTLINE_GROW_FACTOR
	);

	public static boolean render(LevelRenderContext context, BlockPos blockPos) {
		if (!Minecraft.getInstance().level.getWorldBorder().isWithinBounds(blockPos)) {
			return false;
		}

		Vec3 cameraPos = context.levelState().cameraRenderState.pos;
		PoseStack matrixStack = context.poseStack();
		float lineWidth = Minecraft.getInstance().getWindow().getAppropriateLineWidth();

		matrixStack.pushPose();
		matrixStack.translate(blockPos.getX() - cameraPos.x, blockPos.getY() - cameraPos.y, blockPos.getZ() - cameraPos.z);
		context.submitNodeCollector().submitShapeOutline(
			matrixStack,
			CUBE,
			RenderTypes.lines(),
			ARGB.colorFromFloat(OUTLINE_ALPHA, OUTLINE_RED, OUTLINE_GREEN, OUTLINE_BLUE),
			lineWidth,
			false
		);
		matrixStack.popPose();

		return true;
	}

}
