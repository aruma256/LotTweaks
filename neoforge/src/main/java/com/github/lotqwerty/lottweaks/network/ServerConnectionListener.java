package com.github.lotqwerty.lottweaks.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class ServerConnectionListener {

	@SubscribeEvent
	public void onPlayerLoggedIn(final PlayerLoggedInEvent event) {
		if (!event.getEntity().level().isClientSide) {
			LTPacketHandler.sendHelloMessage((ServerPlayer) event.getEntity());
		}
	}

}
