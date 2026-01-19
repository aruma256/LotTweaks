package com.github.aruma256.lottweaks.network;

import java.nio.charset.StandardCharsets;

import com.github.aruma256.lottweaks.AdjustRangeHelper;
import com.github.aruma256.lottweaks.LotTweaks;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ModNetwork {

	public static void init() {
		PayloadTypeRegistry.playC2S().register(ReplaceBlockPacket.TYPE, ReplaceBlockPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ReplaceBlockPacket.TYPE, (payload, context) -> { payload.handle(context); });
		PayloadTypeRegistry.playC2S().register(ReachExtensionPacket.TYPE, ReachExtensionPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ReachExtensionPacket.TYPE, (payload, context) -> { payload.handle(context); });
		PayloadTypeRegistry.playS2C().register(HandshakePacket.TYPE, HandshakePacket.CODEC);
	}

	public static void sendHandshakePacket(ServerPlayer player) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		new HandshakePacket(LotTweaks.VERSION).toBytes(buf);
		ServerPlayNetworking.send(player, new HandshakePacket(LotTweaks.VERSION));
	}

	// ReplaceBlock

	public record ReplaceBlockPacket(BlockPos pos, BlockState state, BlockState checkState) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<ReplaceBlockPacket> TYPE = new CustomPacketPayload.Type<ReplaceBlockPacket>(Identifier.fromNamespaceAndPath("lottweaks", "replace"));
		public static final StreamCodec<FriendlyByteBuf, ReplaceBlockPacket> CODEC = StreamCodec.of(
			(buf, packet) -> packet.toBytes(buf),
			buf -> new ReplaceBlockPacket(buf)
		);

		@Override
		public Type<ReplaceBlockPacket> type() {
			return TYPE;
		}

		public ReplaceBlockPacket(FriendlyByteBuf buf) {
			this(buf.readBlockPos(), Block.stateById(buf.readInt()), Block.stateById(buf.readInt()));
		}

		public void toBytes(FriendlyByteBuf buf) {
			buf.writeBlockPos(this.pos);
			buf.writeInt(Block.getId(state));
			buf.writeInt(Block.getId(checkState));
		}

		@SuppressWarnings("resource")
		public void handle(Context context) {
			final ServerPlayer player = context.player();
			if (!player.isCreative()) {
				return;
			}
			if (LotTweaks.CONFIG.REQUIRE_OP_TO_USE_REPLACE && context.server().getPlayerList().getOps().get(player.nameAndId()) == null) {
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
			context.server().execute(() -> {
				player.level().setBlock(pos, state, 2);
			});
			return;
		}
	}


	// ReachExtension

	public record ReachExtensionPacket(double dist) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<ReachExtensionPacket> TYPE = new CustomPacketPayload.Type<ReachExtensionPacket>(Identifier.fromNamespaceAndPath("lottweaks", "adjust_range"));
		public static final StreamCodec<FriendlyByteBuf, ReachExtensionPacket> CODEC = StreamCodec.of(
			(buf, packet) -> packet.toBytes(buf),
			buf -> new ReachExtensionPacket(buf)
		);

		@Override
		public Type<ReachExtensionPacket> type() {
			return TYPE;
		}

		public ReachExtensionPacket(double dist) {
			this.dist = dist;
		}

		public ReachExtensionPacket(FriendlyByteBuf buf) {
			this(buf.readDouble());
		}

		public void toBytes(FriendlyByteBuf buf) {
			buf.writeDouble(this.dist);
		}

		public void handle(Context context) {
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

	// Handshake

	public record HandshakePacket(String version) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<HandshakePacket> TYPE = new CustomPacketPayload.Type<HandshakePacket>(Identifier.fromNamespaceAndPath("lottweaks", "hello"));
		public static final StreamCodec<FriendlyByteBuf, HandshakePacket> CODEC = StreamCodec.of(
			(buf, packet) -> packet.toBytes(buf),
			buf -> new HandshakePacket(buf)
		);

		@Override
		public Type<HandshakePacket> type() {
			return TYPE;
		}

		public HandshakePacket(String version) {
			this.version = version;
		}

		public HandshakePacket(FriendlyByteBuf buf) {
			this(buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8).toString());
		}

		public void toBytes(FriendlyByteBuf buf) {
			buf.writeInt(version.length());
			buf.writeCharSequence(version, StandardCharsets.UTF_8);
		}
	}

}
