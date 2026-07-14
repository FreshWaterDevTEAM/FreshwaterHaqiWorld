package com.freshwater.haqiworld;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

/**
 * Client-only companion for the Paper plugin. Bundles item/sound assets and the debug
 * haqi keybind (Plugin Message to the Paper server). All gameplay runs on the Paper plugin.
 */
@Mod(FreshwaterHaqiWorld.MODID)
public final class FreshwaterHaqiWorld {
    public static final String MODID = "fhw";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FreshwaterHaqiWorld(FMLJavaModLoadingContext context) {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            LOGGER.info("FreshwaterHaqiWorld client mod ignored on dedicated server — use the Paper plugin.");
            return;
        }
        context.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        com.freshwater.haqiworld.client.HaqiClient.init(context.getModBusGroup());
        LOGGER.info("FreshwaterHaqiWorld client companion initialized.");
    }
}
