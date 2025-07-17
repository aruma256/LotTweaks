package com.github.lotqwerty.lottweaks.network;

import java.nio.charset.StandardCharsets;

import com.github.lotqwerty.lottweaks.AdjustRangeHelper;
import com.github.lotqwerty.lottweaks.LotTweaks;
import com.github.lotqwerty.lottweaks.client.LotTweaksClient;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.Channel.VersionTest;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

//https://mcforge.readthedocs.io/en/1.16.x/networking/simpleimpl/

public class LTPacketHandler {

	private static final int PROTOCOL_VERSION = 1;
	private static final SimpleChannel INSTANCE = ChannelBuilder.named(
		new ResourceLocation(LotTweaks.MODID))
		.networkProtocolVersion(PROTOCOL_VERSION)
		.clientAcceptedVersions((serverStatus, serverVersion) -> serverStatus == VersionTest.Status.VANILLA || serverStatus == VersionTest.Status.MISSING || serverVersion <= PROTOCOL_VERSION)
		.serverAcceptedVersions((clientStatus, clientVersion) -> true)
		.simpleChannel();

	public static void init() {
		int id = 0;
		INSTANCE.messageBuilder(ReplaceMessage.class, id++)
			.encoder(ReplaceMessage::toBytes)
			.decoder(ReplaceMessage::new)
			.consumerMainThread(ReplaceMessage::handle)
			.add();
		INSTANCE.messageBuilder(AdjustRangeMessage.class, id++)
			.encoder(AdjustRangeMessage::toBytes)
			.decoder(AdjustRangeMessage::new)
			.consumerMainThread(AdjustRangeMessage::handle)
			.add();
		INSTANCE.messageBuilder(HelloMessage.class, id++)
			.encoder(HelloMessage::toBytes)
			.decoder(HelloMessage::new)
			.consumerMainThread(HelloMessage::handle)
			.add();
	}

	public static void sendReplaceMessage(BlockPos pos, BlockState state, BlockState checkState) {
		INSTANCE.send(new ReplaceMessage(pos, state, checkState), PacketDistributor.SERVER.noArg());
	}

	public static void sendReachRangeMessage(double dist) {
		INSTANCE.send(new AdjustRangeMessage(dist), PacketDistributor.SERVER.noArg());
	}

	public static void sendHelloMessage(ServerPlayer player) {
		INSTANCE.send(new HelloMessage(LotTweaks.VERSION), PacketDistributor.PLAYER.with(player));
	}

	//Replace

	public static class ReplaceMessage {

		private final BlockPos pos;
		private final BlockState state;
		private final BlockState checkState;

		public ReplaceMessage(BlockPos pos, BlockState state, BlockState checkState) {
			this.pos = pos;
			this.state = state;
			this.checkState = checkState;
		}

		public ReplaceMessage(FriendlyByteBuf buf) {
			this(buf.readBlockPos(), Block.stateById(buf.readInt()), Block.stateById(buf.readInt()));
		}

		public void toBytes(FriendlyByteBuf buf) {
			buf.writeBlockPos(this.pos);
			buf.writeInt(Block.getId(state));
			buf.writeInt(Block.getId(checkState));
		}

		public void handle(CustomPayloadEvent.Context ctx) {
			ctx.setPacketHandled(true);
			final ServerPlayer player = ctx.getSender();
			if (!player.isCreative()) {
				return;
			}
			if (player.level().isClientSide) {
				// kore iru ??
				return;
			}
			if (LotTweaks.CONFIG.REQUIRE_OP_TO_USE_REPLACE.get() && player.getServer().getPlayerList().getOps().get(player.getGameProfile())==null) {
				return;
			}
			// validation
			if (state.getBlock() == Blocks.AIR) {
				return;
			}
			double dist = player.getEyePosition(1.0F).distanceTo(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
			if (dist > LotTweaks.CONFIG.MAX_RANGE.get()) {
				return;
			}
			if (player.level().getBlockState(pos) != checkState) {
				return;
			}
			//
			ctx.enqueueWork(() -> {
				player.level().setBlock(pos, state, 2);
			});
			return;
		}
	}

	// AdjustRange

	public static class AdjustRangeMessage {

		private double dist;

		public AdjustRangeMessage(double dist) {
			this.dist = dist;
		}

		public AdjustRangeMessage(FriendlyByteBuf buf) {
			this(buf.readDouble());
		}

		public void toBytes(FriendlyByteBuf buf) {
			buf.writeDouble(this.dist);
		}

		public void handle(CustomPayloadEvent.Context ctx) {
			ctx.setPacketHandled(true);
			final ServerPlayer player = ctx.getSender();
			if (!player.isCreative()) {
				return;
			}
			ctx.enqueueWork(() -> {
				if (dist < 0) {
					return;
				}
				dist = Math.min(LotTweaks.CONFIG.MAX_RANGE.get(), dist);
				AdjustRangeHelper.changeRangeModifier(player, dist);
			});
			return;
		}
	}

	// Hello

	public static class HelloMessage {

		private String version;

		public HelloMessage(String version) {
			this.version = version;
		}

		public HelloMessage(FriendlyByteBuf buf) {
			this.version = buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8).toString();
		}

		public void toBytes(FriendlyByteBuf buf) {
			buf.writeInt(version.length());
			buf.writeCharSequence(version, StandardCharsets.UTF_8);
		}

		public void handle(CustomPayloadEvent.Context ctx) {
			ctx.setPacketHandled(true);
			LotTweaksClient.setServerVersion(this.version);
		}

	}

}
