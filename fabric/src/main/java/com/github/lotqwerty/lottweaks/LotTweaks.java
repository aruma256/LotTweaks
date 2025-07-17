package com.github.lotqwerty.lottweaks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lotqwerty.lottweaks.network.LTPacketHandler;
import com.github.lotqwerty.lottweaks.network.ServerConnectionListener;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

//@Mod(LotTweaks.MODID)
public class LotTweaks implements ModInitializer {

	public static final String MODID = "lottweaks";
	public static final String NAME = "LotTweaks";
	public static final String VERSION = "2.3.6";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static class CONFIG {
		public static int MAX_RANGE = 128;
		public static int REPLACE_INTERVAL = 1;
		public static boolean REQUIRE_OP_TO_USE_REPLACE = false;
		public static boolean DISABLE_ANIMATIONS = false;
		public static boolean SNEAK_TO_SWITCH_GROUP = false;
		public static boolean INVERT_REPLACE_LOCK = false;
		public static boolean SHOW_BLOCKCONFIG_ERROR_LOG_TO_CHAT = true;
	}
	
	@Override
	public void onInitialize() {
		LTPacketHandler.init();
		ServerTickEvents.START_SERVER_TICK.register(new AdjustRangeHelper());
		ServerPlayConnectionEvents.JOIN.register(new ServerConnectionListener());
	}

}
