package com.github.aruma256.lottweaks.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.aruma256.lottweaks.event.DrawBlockOutlineEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;

@Mixin(LevelRenderer.class)
public abstract class DrawShapeOutlineInjector {

	@Inject(method="renderHitOutline(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;DDDLnet/minecraft/client/renderer/state/BlockOutlineRenderState;I)V", at=@At(value="HEAD"), cancellable=true)
	private void lottweaks_injected_renderHitOutline(PoseStack matrixStack, VertexConsumer vertexConsumer, double d, double e, double f, BlockOutlineRenderState blockOutlineRenderState, int i, CallbackInfo ci) {
		if (DrawBlockOutlineEvent.post(Minecraft.getInstance().gameRenderer.getMainCamera(), matrixStack, vertexConsumer, blockOutlineRenderState.pos())) {
			ci.cancel();
		}
	}

}
