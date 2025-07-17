package com.github.lotqwerty.lottweaks.fabric;

import java.util.ArrayList;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;

public class DrawBlockOutlineEvent {
	private static final ArrayList<DrawBlockOutlineListener> LISTENERS = new ArrayList<>();

	public static void registerListener(DrawBlockOutlineListener listener) {
		LISTENERS.add(listener);
	}

	public static boolean post(Camera camera, PoseStack poseStack, VertexConsumer vertexConsumer, BlockPos blockPos) {
		DrawBlockOutlineEvent event = new DrawBlockOutlineEvent(camera, poseStack, vertexConsumer, blockPos);
		for (DrawBlockOutlineListener iListener : LISTENERS) {
			iListener.onDrawBlockHighlightEvent(event);
			if (event.isCanceled()) {
				return true;
			}
		}
		return false;
	}

	private boolean canceled = false;
	private final Camera camera;
	private final PoseStack poseStack;
	private final VertexConsumer vertexConsumer;
	private final BlockPos blockPos;

	private DrawBlockOutlineEvent(Camera camera, PoseStack poseStack, VertexConsumer vertexConsumer, BlockPos blockPos) {
		this.camera = camera;
		this.poseStack = poseStack;
		this.vertexConsumer = vertexConsumer;
		this.blockPos = blockPos;
	}

	public Camera getCamera() {
		return this.camera;
	}

	public PoseStack getPoseStack() {
		return this.poseStack;
	}

	public VertexConsumer getBuffers() {
		return this.vertexConsumer;
	}

	public BlockPos getPos() {
		return this.blockPos;
	}

	public boolean isCanceled() {
		return this.canceled;
	}

	public void setCanceled(boolean value) {
		this.canceled = value;
	}

	public static interface DrawBlockOutlineListener {
		public void onDrawBlockHighlightEvent(DrawBlockOutlineEvent event);
	}

}
