package com.github.aruma256.lottweaks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
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

	public static boolean render(WorldRenderContext context, BlockPos blockPos) {
		if (!Minecraft.getInstance().level.getWorldBorder().isWithinBounds(blockPos)) {
			return false;
		}

		Camera camera = context.gameRenderer().getMainCamera();
		PoseStack matrixStack = context.matrices();
		VertexConsumer buffer = context.consumers().getBuffer(RenderTypes.lines());

		Vec3 vector3d = camera.position();
		double d0 = vector3d.x();
		double d1 = vector3d.y();
		double d2 = vector3d.z();

		renderHitOutline(matrixStack, buffer, camera.entity(), d0, d1, d2, blockPos);

		return true;
	}

	// from WorldRenderer.class
	private static void renderHitOutline(PoseStack matrixStackIn, VertexConsumer bufferIn, Entity entity, double xIn, double yIn, double zIn, BlockPos blockPosIn) {
		float lineWidth = Minecraft.getInstance().getWindow().getAppropriateLineWidth();
		renderShape(matrixStackIn, bufferIn, CUBE, (double)blockPosIn.getX() - xIn, (double)blockPosIn.getY() - yIn, (double)blockPosIn.getZ() - zIn, OUTLINE_RED, OUTLINE_GREEN, OUTLINE_BLUE, OUTLINE_ALPHA, lineWidth);
	}

	// from LevelRenderer.class
	private static void renderShape(PoseStack matrixStackIn, VertexConsumer bufferIn, VoxelShape shapeIn, double xIn, double yIn, double zIn, float red, float green, float blue, float alpha, float lineWidth) {
		PoseStack.Pose pose = matrixStackIn.last();
		shapeIn.forAllEdges((ax, ay, az, bx, by, bz) -> {
			float x = (float)(bx - ax);
			float y = (float)(by - ay);
			float z = (float)(bz - az);
			float d = Mth.sqrt(x * x + y * y + z * z);
			x = x / d;
			y = y / d;
			z = z / d;
			bufferIn.addVertex(pose, (float)(ax + xIn), (float)(ay + yIn), (float)(az + zIn)).setColor(red, green, blue, alpha).setNormal(pose, x, y, z).setLineWidth(lineWidth);
			bufferIn.addVertex(pose, (float)(bx + xIn), (float)(by + yIn), (float)(bz + zIn)).setColor(red, green, blue, alpha).setNormal(pose, x, y, z).setLineWidth(lineWidth);
		});
	}

}
