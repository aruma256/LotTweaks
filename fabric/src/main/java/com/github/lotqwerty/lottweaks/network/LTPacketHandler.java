package com.github.lotqwerty.lottweaks.network;

import java.nio.charset.StandardCharsets;

import com.github.lotqwerty.lottweaks.AdjustRangeHelper;
import com.github.lotqwerty.lottweaks.LotTweaks;
import com.github.lotqwerty.lottweaks.network.LTPacketHandler.AdjustRangeMessage;
import com.github.lotqwerty.lottweaks.network.LTPacketHandler.HelloMessage;
import com.github.lotqwerty.lottweaks.network.LTPacketHandler.ReplaceMessage;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.fabricmc.fabric.mixin.networking.PacketCodecDispatcherMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

//https://mcforge.readthedocs.io/en/1.16.x/networking/simpleimpl/

public class LTPacketHandler {

	public static void init() {
		PayloadTypeRegistry.playC2S().register(ReplaceMessage.TYPE, ReplaceMessage.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ReplaceMessage.TYPE, (payload, context) -> { payload.handle(context); });
		PayloadTypeRegistry.playC2S().register(AdjustRangeMessage.TYPE, AdjustRangeMessage.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(AdjustRangeMessage.TYPE, (payload, context) -> { payload.handle(context); });
		PayloadTypeRegistry.playS2C().register(HelloMessage.TYPE, HelloMessage.CODEC);
	}

	public static void sendHelloMessage(ServerPlayer player) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		new HelloMessage(LotTweaks.VERSION).toBytes(buf);
		ServerPlayNetworking.send(player, new HelloMessage(LotTweaks.VERSION));
	}

	//Replace

	public record ReplaceMessage(BlockPos pos, BlockState state, BlockState checkState) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<ReplaceMessage> TYPE = new CustomPacketPayload.Type<ReplaceMessage>(ResourceLocation.fromNamespaceAndPath("lottweaks", "replace"));
		public static final StreamCodec<FriendlyByteBuf, ReplaceMessage> CODEC = StreamCodec.of(
			(buf, packet) -> packet.toBytes(buf),
			buf -> new ReplaceMessage(buf)
		);

		@Override
		public Type<ReplaceMessage> type() {
			return TYPE;
		}

		public ReplaceMessage(FriendlyByteBuf buf) {
			this(buf.readBlockPos(), Block.stateById(buf.readInt()), Block.stateById(buf.readInt()));
		}

		public void toBytes(FriendlyByteBuf buf) {
			buf.writeBlockPos(this.pos);
			buf.writeInt(Block.getId(state));
			buf.writeInt(Block.getId(checkState));
		}

		@SuppressWarnings("resource")
		public void handle(Context context) {
			/*
			ctx.get().setPacketHandled(true);
			final ServerPlayer player = ctx.get().getSender();
			*/
			final ServerPlayer player = context.player();
			if (!player.isCreative()) {
				return;
			}
			if (player.level().isClientSide) {
				// kore iru ??
				return;
			}
			if (LotTweaks.CONFIG.REQUIRE_OP_TO_USE_REPLACE && player.getServer().getPlayerList().getOps().get(player.getGameProfile())==null) {
				return;
			}
			// validation
			if (state.getBlock() == Blocks.AIR) {
				return;
			}
			double dist = player.getEyePosition(1.0F).distanceTo(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
			if (dist > LotTweaks.CONFIG.MAX_RANGE) {
				return;
			}
			if (player.level().getBlockState(pos) != checkState) {
				return;
			}
			//
			context.server().execute(() -> {
				player.level().setBlock(pos, state, 2);
			});
			return;
		}
	}


	// AdjustRange

	public record AdjustRangeMessage(double dist) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<AdjustRangeMessage> TYPE = new CustomPacketPayload.Type<AdjustRangeMessage>(ResourceLocation.fromNamespaceAndPath("lottweaks", "adjust_range"));
		public static final StreamCodec<FriendlyByteBuf, AdjustRangeMessage> CODEC = StreamCodec.of(
			(buf, packet) -> packet.toBytes(buf),
			buf -> new AdjustRangeMessage(buf)
		);

		@Override
		public Type<AdjustRangeMessage> type() {
			return TYPE;
		}

		public AdjustRangeMessage(double dist) {
			this.dist = dist;
		}

		public AdjustRangeMessage(FriendlyByteBuf buf) {
			this(buf.readDouble());
		}

		public void toBytes(FriendlyByteBuf buf) {
			buf.writeDouble(this.dist);
		}

		public void handle(Context context) {
			/*
			ctx.get().setPacketHandled(true);
			final ServerPlayer player = ctx.get().getSender();
			*/
			final ServerPlayer player = context.player();
			if (!player.isCreative()) {
				return;
			}
			context.server().execute(() -> {
				if (dist < 0) {
					return;
				}
				double clipped_dist = Math.min(LotTweaks.CONFIG.MAX_RANGE, dist);
				AdjustRangeHelper.changeRangeModifier(player, clipped_dist);
			});
			return;
		}
	}

	// Hello

	public record HelloMessage(String version) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<HelloMessage> TYPE = new CustomPacketPayload.Type<HelloMessage>(ResourceLocation.fromNamespaceAndPath("lottweaks", "hello"));
		public static final StreamCodec<FriendlyByteBuf, HelloMessage> CODEC = StreamCodec.of(
			(buf, packet) -> packet.toBytes(buf),
			buf -> new HelloMessage(buf)
		);

		@Override
		public Type<HelloMessage> type() {
			return TYPE;
		}

		public HelloMessage(String version) {
			this.version = version;
		}

		public HelloMessage(FriendlyByteBuf buf) {
			this(buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8).toString());
		}

		public void toBytes(FriendlyByteBuf buf) {
			buf.writeInt(version.length());
			buf.writeCharSequence(version, StandardCharsets.UTF_8);
		}
	}

}
