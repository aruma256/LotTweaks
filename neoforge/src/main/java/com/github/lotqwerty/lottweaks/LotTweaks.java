package com.github.lotqwerty.lottweaks;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import com.github.lotqwerty.lottweaks.client.LotTweaksClient;
import com.github.lotqwerty.lottweaks.client.RotationHelper;
import com.github.lotqwerty.lottweaks.network.LTPacketHandler;
import com.github.lotqwerty.lottweaks.network.ServerConnectionListener;

@Mod(LotTweaks.MODID)
public class LotTweaks {

	public static final String MODID = "lottweaks";
	public static final String NAME = "LotTweaks";
	public static final String VERSION = "2.2.4";
	public static final Logger LOGGER = LogUtils.getLogger();

	public LotTweaks(IEventBus modEventBus) {
		// Register our mod's ModConfigSpec so that FML can create and load the config file for us
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC, NAME + ".toml");

		// Register the commonSetup method for modloading
		modEventBus.addListener(this::commonSetup);
		
		// Register client setup only on client side
		if (FMLEnvironment.dist == Dist.CLIENT) {
			modEventBus.addListener(this::clientSetup);
			modEventBus.addListener(LotTweaksClient::onRegisterKeyMappingsEvent);
			modEventBus.addListener(LotTweaksClient::onRegisterGuiOverlaysEvent);
		}

		// Register ourselves for server and other game events we are interested in
		NeoForge.EVENT_BUS.register(this);
	}

	private void clientSetup(FMLClientSetupEvent event) {
		LotTweaksClient.init();
		RotationHelper.loadAllFromFile();
		RotationHelper.loadAllItemGroupFromStrArray();
	}

	private void commonSetup(FMLCommonSetupEvent event) {
		LTPacketHandler.init();
		NeoForge.EVENT_BUS.register(new AdjustRangeHelper());
		NeoForge.EVENT_BUS.register(new ServerConnectionListener());
	}
}
