package com.freshwater.haqiworld;

import com.freshwater.haqiworld.combat.HaqiCombatEvents;
import com.freshwater.haqiworld.command.HaqiCommand;
import com.freshwater.haqiworld.event.PlayerLifecycleHandler;
import com.freshwater.haqiworld.interaction.HaqiInteractionHandler;
import com.freshwater.haqiworld.mob.MobHaqiHandler;
import com.freshwater.haqiworld.network.HaqiNetwork;
import com.freshwater.haqiworld.registry.ModCreativeTabs;
import com.freshwater.haqiworld.registry.ModItems;
import com.freshwater.haqiworld.registry.ModSounds;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

/**
 * FreshwaterHaqiWorld - "Haqi World Survival".
 *
 * <p>Vanilla melee combat is removed; players fight by "haqi" (breathing into the
 * microphone). Voice volume is read through the Simple Voice Chat plugin API and fires a
 * Warden-style sonic boom whose range and damage scale with loudness and a permanently
 * unlocked haqi tier. Strong mobs can haqi back, and haqi kills feed a leaderboard.
 */
@Mod(FreshwaterHaqiWorld.MODID)
public final class FreshwaterHaqiWorld {
    public static final String MODID = "fhw";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FreshwaterHaqiWorld(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();

        ModItems.register(modBusGroup);
        ModSounds.register(modBusGroup);
        ModCreativeTabs.register(modBusGroup);

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        HaqiNetwork.register();

        HaqiCombatEvents.register();
        MobHaqiHandler.register();
        HaqiInteractionHandler.register();
        PlayerLifecycleHandler.register();
        HaqiCommand.register();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            com.freshwater.haqiworld.client.HaqiClient.init(modBusGroup);
        }

        LOGGER.info("FreshwaterHaqiWorld initialized.");
    }
}
