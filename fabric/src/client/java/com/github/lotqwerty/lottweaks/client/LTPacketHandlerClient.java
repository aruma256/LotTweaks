package com.github.lotqwerty.lottweaks.client;

import com.github.lotqwerty.lottweaks.network.LTPacketHandler;
import com.github.lotqwerty.lottweaks.network.LTPacketHandler.HelloMessage;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;

public class LTPacketHandlerClient extends LTPacketHandler {

	protected static void initClient() {
		ClientPlayNetworking.registerGlobalReceiver(HelloMessage.TYPE, (payload, context) -> { HelloMessageHandler.handle(payload.version()); });
	}

	public static void sendReplaceMessage(BlockPos pos, BlockState state, BlockState checkState) {
		// FriendlyByteBuf buf = PacketByteBufs.create();
		// new ReplaceMessage(pos, state, checkState).toBytes(buf);
		ClientPlayNetworking.send(new ReplaceMessage(pos, state, checkState));
	}

	public static void sendReachRangeMessage(double dist) {
		// FriendlyByteBuf buf = PacketByteBufs.create();
		// new AdjustRangeMessage(dist).toBytes(buf);
		ClientPlayNetworking.send(new AdjustRangeMessage(dist));
	}

	public static class HelloMessageHandler {
		public static void handle(String version) {
			LotTweaksClient.setServerVersion(version);
		}
	}
}
