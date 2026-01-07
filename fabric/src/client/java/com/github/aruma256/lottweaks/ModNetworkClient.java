package com.github.aruma256.lottweaks;

import com.github.aruma256.lottweaks.network.ModNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ModNetworkClient extends ModNetwork {

	protected static void initClient() {
		ClientPlayNetworking.registerGlobalReceiver(HandshakePacket.TYPE, (payload, context) -> { HandshakeHandler.handle(payload.version()); });
	}

	public static void sendReplaceBlockPacket(BlockPos pos, BlockState state, BlockState checkState) {
		ClientPlayNetworking.send(new ReplaceBlockPacket(pos, state, checkState));
	}

	public static void sendReachExtensionPacket(double dist) {
		ClientPlayNetworking.send(new ReachExtensionPacket(dist));
	}

	public static class HandshakeHandler {
		public static void handle(String version) {
			LotTweaksClient.setServerVersion(version);
		}
	}
}
