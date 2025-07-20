package com.github.lotqwerty.lottweaks.network;

import java.nio.charset.StandardCharsets;

import com.github.lotqwerty.lottweaks.AdjustRangeHelper;
import com.github.lotqwerty.lottweaks.LotTweaks;
import com.github.lotqwerty.lottweaks.Config;
import com.github.lotqwerty.lottweaks.client.LotTweaksClient;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.bus.api.SubscribeEvent;

public class LTPacketHandler {

	@SubscribeEvent
	public static void register(RegisterPayloadHandlerEvent event) {
		final IPayloadRegistrar registrar = event.registrar(LotTweaks.MODID)
				.versioned("1.0.0")
				.optional();

		// Register payloads
		registrar.play(ReplacePayload.ID, ReplacePayload::new, LTPacketHandler::handleReplaceMessage);
		registrar.play(AdjustRangePayload.ID, AdjustRangePayload::new, LTPacketHandler::handleAdjustRangeMessage);
		registrar.play(HelloPayload.ID, HelloPayload::new, LTPacketHandler::handleHelloMessage);
	}

	public static void init() {
		// Registration now happens via event
	}

	// Handler methods
	private static void handleReplaceMessage(ReplacePayload payload, PlayPayloadContext context) {
		context.workHandler().submitAsync(() -> {
			if (context.player().isPresent() && context.player().get() instanceof ServerPlayer player) {
				if (!player.isCreative()) {
					return;
				}
				if (player.level().isClientSide) {
					return;
				}
				if (Config.REQUIRE_OP_TO_USE_REPLACE.get() && player.getServer().getPlayerList().getOps().get(player.getGameProfile()) == null) {
					return;
				}
				// validation
				if (payload.state().getBlock() == Blocks.AIR) {
					return;
				}
				double dist = player.getEyePosition(1.0F).distanceTo(new Vec3(payload.pos().getX(), payload.pos().getY(), payload.pos().getZ()));
				if (dist > Config.MAX_RANGE.get()) {
					return;
				}
				if (player.level().getBlockState(payload.pos()) != payload.checkState()) {
					return;
				}
				// Execute the block replacement
				player.level().setBlock(payload.pos(), payload.state(), 2);
			}
		});
	}

	private static void handleAdjustRangeMessage(AdjustRangePayload payload, PlayPayloadContext context) {
		context.workHandler().submitAsync(() -> {
			if (context.player().isPresent() && context.player().get() instanceof ServerPlayer player) {
				if (!player.isCreative()) {
					return;
				}
				if (payload.dist() < 0) {
					return;
				}
				double dist = Math.min(Config.MAX_RANGE.get(), payload.dist());
				AdjustRangeHelper.changeRangeModifier(player, dist);
			}
		});
	}

	private static void handleHelloMessage(HelloPayload payload, PlayPayloadContext context) {
		context.workHandler().submitAsync(() -> {
			LotTweaksClient.setServerVersion(payload.version());
		});
	}

	// Public API methods
	public static void sendReplaceMessage(BlockPos pos, BlockState state, BlockState checkState) {
		PacketDistributor.SERVER.noArg().send(new ReplacePayload(pos, state, checkState));
	}

	public static void sendReachRangeMessage(double dist) {
		PacketDistributor.SERVER.noArg().send(new AdjustRangePayload(dist));
	}

	public static void sendHelloMessage(ServerPlayer player) {
		PacketDistributor.PLAYER.with(player).send(new HelloPayload(LotTweaks.VERSION));
	}

	// Payload Records
	public record ReplacePayload(BlockPos pos, BlockState state, BlockState checkState) implements CustomPacketPayload {
		public static final ResourceLocation ID = new ResourceLocation(LotTweaks.MODID, "replace");

		public ReplacePayload(FriendlyByteBuf buf) {
			this(buf.readBlockPos(), Block.stateById(buf.readInt()), Block.stateById(buf.readInt()));
		}

		@Override
		public void write(FriendlyByteBuf buf) {
			buf.writeBlockPos(this.pos);
			buf.writeInt(Block.getId(state));
			buf.writeInt(Block.getId(checkState));
		}

		@Override
		public ResourceLocation id() {
			return ID;
		}
	}

	public record AdjustRangePayload(double dist) implements CustomPacketPayload {
		public static final ResourceLocation ID = new ResourceLocation(LotTweaks.MODID, "adjust_range");

		public AdjustRangePayload(FriendlyByteBuf buf) {
			this(buf.readDouble());
		}

		@Override
		public void write(FriendlyByteBuf buf) {
			buf.writeDouble(this.dist);
		}

		@Override
		public ResourceLocation id() {
			return ID;
		}
	}

	public record HelloPayload(String version) implements CustomPacketPayload {
		public static final ResourceLocation ID = new ResourceLocation(LotTweaks.MODID, "hello");

		public HelloPayload(FriendlyByteBuf buf) {
			this(buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8).toString());
		}

		@Override
		public void write(FriendlyByteBuf buf) {
			buf.writeInt(version.length());
			buf.writeCharSequence(version, StandardCharsets.UTF_8);
		}

		@Override
		public ResourceLocation id() {
			return ID;
		}
	}
}