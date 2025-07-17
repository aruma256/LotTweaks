package com.github.lotqwerty.lottweaks.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.lotqwerty.lottweaks.fabric.DrawBlockOutlineEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(LevelRenderer.class)
public abstract class DrawShapeOutlineInjector {

	@Inject(method="renderHitOutline(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)V", at=@At(value="HEAD"), cancellable=true)
	private void lottweaks_injected_renderHitOutline(PoseStack matrixStack, VertexConsumer vertexConsumer, Entity entity, double d, double e, double f, BlockPos blockPos, BlockState blockState, int i, CallbackInfo ci) {
		if (DrawBlockOutlineEvent.post(Minecraft.getInstance().gameRenderer.getMainCamera(), matrixStack, vertexConsumer, blockPos)) {
			ci.cancel();
		}
	}

}
