package com.github.aruma256.lottweaks;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.Bootstrap;

public class MinecraftBootstrapExtension implements BeforeAllCallback {

    private static boolean initialized = false;

    @Override
    public void beforeAll(ExtensionContext context) {
        if (!initialized) {
            SharedConstants.tryDetectVersion();
            Bootstrap.bootStrap();
            // 26.1: コンポーネントのバインドが必要（タグを含むルックアップが必要）
            HolderLookup.Provider lookup = VanillaRegistries.createLookup();
            BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(lookup).forEach(pending -> pending.apply());
            initialized = true;
        }
    }
}
