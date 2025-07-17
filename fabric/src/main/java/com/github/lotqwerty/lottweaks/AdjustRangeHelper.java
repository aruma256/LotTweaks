package com.github.lotqwerty.lottweaks;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

public class AdjustRangeHelper implements ServerTickEvents.StartTick {

	private static final ResourceLocation _RESOURCE_LOCATION = ResourceLocation.fromNamespaceAndPath(LotTweaks.MODID, "extension");

	@Override
	public void onStartTick(MinecraftServer server) {
		for (Player player: server.getPlayerList().getPlayers()) {
			if (!player.isCreative()) {
				clearRangeModifier(player);
			}
		}
	}

	private static void clearRangeModifier(Player player) {
		player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).removeModifier(_RESOURCE_LOCATION);
	}

	public static void changeRangeModifier(Player player, double dist) {
		clearRangeModifier(player);
		AttributeInstance instance = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
		instance.addPermanentModifier(new AttributeModifier(_RESOURCE_LOCATION, dist - 5 + 0.5, AttributeModifier.Operation.ADD_VALUE));
	}

}
