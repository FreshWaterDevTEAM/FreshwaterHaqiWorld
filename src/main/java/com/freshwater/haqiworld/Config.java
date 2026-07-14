package com.freshwater.haqiworld;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * Common config for FreshwaterHaqiWorld.
 */
@Mod.EventBusSubscriber(modid = FreshwaterHaqiWorld.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.DoubleValue HAQI_VOLUME_THRESHOLD = BUILDER
            .comment("Minimum normalized microphone loudness (0..1) required to fire a haqi.")
            .defineInRange("haqiVolumeThreshold", 0.12D, 0.0D, 1.0D);

    private static final ForgeConfigSpec.DoubleValue HAQI_REFERENCE_LEVEL = BUILDER
            .comment("RMS reference level used to normalize loudness. Lower = more sensitive microphone.")
            .defineInRange("haqiReferenceLevel", 0.18D, 0.001D, 1.0D);

    private static final ForgeConfigSpec.BooleanValue REQUIRE_HAQI_ITEM = BUILDER
            .comment("If true, the player must hold a haqi item to fire a sonic boom.")
            .define("requireHaqiItem", true);

    private static final ForgeConfigSpec.BooleanValue REMOVE_MELEE_COMBAT = BUILDER
            .comment("If true, vanilla melee weapon damage dealt by players is cancelled (only haqi hurts mobs).")
            .define("removeMeleeCombat", true);

    private static final ForgeConfigSpec.BooleanValue ENABLE_DEBUG_KEYBIND = BUILDER
            .comment("If true, players can simulate haqi with a keybind (for testing without a microphone).")
            .define("enableDebugKeybind", true);

    private static final ForgeConfigSpec.DoubleValue DEBUG_KEYBIND_LOUDNESS = BUILDER
            .comment("Simulated loudness (0..1) applied while the debug haqi keybind is held.")
            .defineInRange("debugKeybindLoudness", 1.0D, 0.0D, 1.0D);

    private static final ForgeConfigSpec.BooleanValue MOBS_CAN_HAQI = BUILDER
            .comment("If true, strong mobs (iron golem, wither skeleton, vindicator, ender dragon) can fire sonic booms.")
            .define("mobsCanHaqi", true);

    private static final ForgeConfigSpec.IntValue MOB_HAQI_COOLDOWN_TICKS = BUILDER
            .comment("Cooldown (ticks) between mob sonic booms.")
            .defineInRange("mobHaqiCooldownTicks", 60, 10, 600);

    private static final ForgeConfigSpec.DoubleValue MOB_HAQI_DAMAGE = BUILDER
            .comment("Damage dealt by a mob sonic boom.")
            .defineInRange("mobHaqiDamage", 6.0D, 0.0D, 100.0D);

    private static final ForgeConfigSpec.IntValue LEADERBOARD_SIZE = BUILDER
            .comment("Number of entries shown by the haqi kill leaderboard.")
            .defineInRange("leaderboardSize", 10, 1, 100);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static double haqiVolumeThreshold;
    public static double haqiReferenceLevel;
    public static boolean requireHaqiItem;
    public static boolean removeMeleeCombat;
    public static boolean enableDebugKeybind;
    public static double debugKeybindLoudness;
    public static boolean mobsCanHaqi;
    public static int mobHaqiCooldownTicks;
    public static double mobHaqiDamage;
    public static int leaderboardSize;

    private Config() {
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        haqiVolumeThreshold = HAQI_VOLUME_THRESHOLD.get();
        haqiReferenceLevel = HAQI_REFERENCE_LEVEL.get();
        requireHaqiItem = REQUIRE_HAQI_ITEM.get();
        removeMeleeCombat = REMOVE_MELEE_COMBAT.get();
        enableDebugKeybind = ENABLE_DEBUG_KEYBIND.get();
        debugKeybindLoudness = DEBUG_KEYBIND_LOUDNESS.get();
        mobsCanHaqi = MOBS_CAN_HAQI.get();
        mobHaqiCooldownTicks = MOB_HAQI_COOLDOWN_TICKS.get();
        mobHaqiDamage = MOB_HAQI_DAMAGE.get();
        leaderboardSize = LEADERBOARD_SIZE.get();
    }
}
