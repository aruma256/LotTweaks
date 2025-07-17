package com.github.lotqwerty.lottweaks;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AdjustRangeHelper {

	private static final ResourceLocation _RESOURCE_LOCATION = ResourceLocation.fromNamespaceAndPath(LotTweaks.MODID, "extension");

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event) {
		if (event.side.isServer() && !event.player.isCreative()) {
			clearRangeModifier(event.player);
		}
	}

	private static void clearRangeModifier(Player player) {
		player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).removeModifier(_RESOURCE_LOCATION);
	}

	public static void changeRangeModifier(Player player, double dist) {
		clearRangeModifier(player);
		AttributeInstance instance = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
		instance.addPermanentModifier(new AttributeModifier(_RESOURCE_LOCATION, dist - instance.getBaseValue() + 0.5,
				AttributeModifier.Operation.ADD_VALUE));
	}
}
