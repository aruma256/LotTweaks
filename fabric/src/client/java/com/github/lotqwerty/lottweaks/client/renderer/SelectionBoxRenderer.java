package com.github.lotqwerty.lottweaks.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.Vec3;

public class SelectionBoxRenderer {

	private static final double growFactor = 0.0020000000949949026D / 2;
	private static final VoxelShape CUBE = Shapes.box(-growFactor, -growFactor, -growFactor, 1+growFactor, 1+growFactor, 1+growFactor);
	
	public static boolean render(Camera activeRenderInfo, PoseStack matrixStack, VertexConsumer buffer, BlockPos blockPos, float partialTicks, float r, float g, float b) {
		if (!Minecraft.getInstance().level.getWorldBorder().isWithinBounds(blockPos)) {
			return false;
		}

		Vec3 vector3d = activeRenderInfo.getPosition();
		double d0 = vector3d.x();
		double d1 = vector3d.y();
		double d2 = vector3d.z();

		renderHitOutline(matrixStack, buffer, activeRenderInfo.getEntity(), d0, d1, d2, blockPos);

		return true;
	}

	//from WorldRenderer.class
	private static void renderHitOutline(PoseStack matrixStackIn, VertexConsumer bufferIn, Entity entity, double xIn, double yIn, double zIn, BlockPos blockPosIn) {
		renderShape(matrixStackIn, bufferIn, CUBE, (double)blockPosIn.getX() - xIn, (double)blockPosIn.getY() - yIn, (double)blockPosIn.getZ() - zIn, 1.0F, 0.0F, 0.0F, 0.4F);
	}

	//from LevelRenderer.class
	private static void renderShape(PoseStack matrixStackIn, VertexConsumer bufferIn, VoxelShape shapeIn, double xIn, double yIn, double zIn, float red, float green, float blue, float alpha) {
		PoseStack.Pose pose = matrixStackIn.last();
		shapeIn.forAllEdges((ax, ay, az, bx, by, bz) -> {
			float x = (float)(bx - ax);
			float y = (float)(by - ay);
			float z = (float)(bz - az);
			float d = Mth.sqrt(x * x + y * y + z * z);
			x = x / d;
			y = y / d;
			z = z / d;
			bufferIn.addVertex(pose, (float)(ax + xIn), (float)(ay + yIn), (float)(az + zIn)).setColor(red, green, blue, alpha).setNormal(pose, x, y, z);
			bufferIn.addVertex(pose, (float)(bx + xIn), (float)(by + yIn), (float)(bz + zIn)).setColor(red, green, blue, alpha).setNormal(pose, x, y, z);
		});
	}

}
