package com.freshwater.haqiworld.plugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginConfig {
    public final double haqiVolumeThreshold;
    public final double haqiReferenceLevel;
    public final double loudnessGain;
    public final boolean voiceActivationBoost;
    public final double voiceActivationMinLoudness;
    public final boolean requireHaqiItem;
    public final boolean removeMeleeCombat;
    public final boolean enableDebugKeybind;
    public final float debugKeybindLoudness;
    public final boolean mobsCanHaqi;
    public final int mobHaqiCooldownTicks;
    public final double mobHaqiDamage;
    public final int leaderboardSize;
    public final boolean resourcePackEnabled;
    public final String resourcePackUrl;
    public final String resourcePackHost;
    public final int resourcePackPort;
    public final boolean resourcePackForce;

    public PluginConfig(JavaPlugin plugin) {
        FileConfiguration c = plugin.getConfig();
        haqiVolumeThreshold = c.getDouble("haqi-volume-threshold", 0.04);
        haqiReferenceLevel = c.getDouble("haqi-reference-level", 0.10);
        loudnessGain = c.getDouble("loudness-gain", 2.5);
        voiceActivationBoost = c.getBoolean("voice-activation-boost", false);
        voiceActivationMinLoudness = c.getDouble("voice-activation-min-loudness", 0.55);
        requireHaqiItem = c.getBoolean("require-haqi-item", true);
        removeMeleeCombat = c.getBoolean("remove-melee-combat", true);
        enableDebugKeybind = c.getBoolean("enable-debug-keybind", true);
        debugKeybindLoudness = (float) c.getDouble("debug-keybind-loudness", 1.0);
        mobsCanHaqi = c.getBoolean("mobs-can-haqi", true);
        mobHaqiCooldownTicks = c.getInt("mob-haqi-cooldown-ticks", 60);
        mobHaqiDamage = c.getDouble("mob-haqi-damage", 6.0);
        leaderboardSize = c.getInt("leaderboard-size", 10);
        resourcePackEnabled = c.getBoolean("resource-pack.enabled", false);
        resourcePackUrl = c.getString("resource-pack.url", "");
        resourcePackHost = c.getString("resource-pack.host", "");
        resourcePackPort = c.getInt("resource-pack.port", 8163);
        resourcePackForce = c.getBoolean("resource-pack.force", false);
    }
}
