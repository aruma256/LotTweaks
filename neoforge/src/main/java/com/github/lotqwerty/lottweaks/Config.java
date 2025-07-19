package com.github.lotqwerty.lottweaks;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MAX_RANGE = BUILDER
            .comment("Maximum range for area operations")
            .defineInRange("common.MAX_RANGE", 128, 0, 256);

    public static final ModConfigSpec.IntValue REPLACE_INTERVAL = BUILDER
            .comment("Interval for replace operations")
            .defineInRange("client.REPLACE_INTERVAL", 1, 1, 256);

    public static final ModConfigSpec.BooleanValue REQUIRE_OP_TO_USE_REPLACE = BUILDER
            .comment("Default: false")
            .comment("Require OP permission to use replace functionality")
            .define("server.REQUIRE_OP_TO_USE_REPLACE", false);

    public static final ModConfigSpec.BooleanValue DISABLE_ANIMATIONS = BUILDER
            .comment("Default: false")
            .comment("Disable animations in the mod")
            .define("client.DISABLE_ANIMATIONS", false);

    public static final ModConfigSpec.BooleanValue SNEAK_TO_SWITCH_GROUP = BUILDER
            .comment("Default: false -> Double-tap to switch to the secondary group")
            .comment("Use sneak key to switch item groups")
            .define("client.SNEAK_TO_SWITCH_GROUP", false);

    public static final ModConfigSpec.BooleanValue INVERT_REPLACE_LOCK = BUILDER
            .comment("Default: false")
            .comment("Invert the replace lock behavior")
            .define("client.INVERT_REPLACE_LOCK", false);

    public static final ModConfigSpec.BooleanValue SHOW_BLOCKCONFIG_ERROR_LOG_TO_CHAT = BUILDER
            .comment("Default: true")
            .comment("'true' is highly recommended")
            .comment("Show block configuration errors in chat")
            .define("client.SHOW_BLOCKCONFIG_ERROR_LOG_TO_CHAT", true);

    static final ModConfigSpec SPEC = BUILDER.build();
}
