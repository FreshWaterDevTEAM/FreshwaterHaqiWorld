package com.freshwater.haqiworld;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = FreshwaterHaqiWorld.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue ENABLE_DEBUG_KEYBIND = BUILDER
            .comment("If true, hold H to simulate haqi loudness via Plugin Message to the Paper plugin.")
            .define("enableDebugKeybind", true);

    private static final ForgeConfigSpec.DoubleValue DEBUG_KEYBIND_LOUDNESS = BUILDER
            .comment("Simulated loudness (0..1) while the debug keybind is held.")
            .defineInRange("debugKeybindLoudness", 1.0D, 0.0D, 1.0D);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean enableDebugKeybind = true;
    public static double debugKeybindLoudness = 1.0D;

    private Config() {
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        enableDebugKeybind = ENABLE_DEBUG_KEYBIND.get();
        debugKeybindLoudness = DEBUG_KEYBIND_LOUDNESS.get();
    }
}
