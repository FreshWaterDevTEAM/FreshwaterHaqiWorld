package com.freshwater.haqiworld.plugin;

import com.freshwater.haqiworld.plugin.combat.CombatListener;
import com.freshwater.haqiworld.plugin.combat.HaqiAttackTask;
import com.freshwater.haqiworld.plugin.command.HaqiCommand;
import com.freshwater.haqiworld.plugin.data.Leaderboard;
import com.freshwater.haqiworld.plugin.haqi.HaqiItems;
import com.freshwater.haqiworld.plugin.interaction.HaqiCraftListener;
import com.freshwater.haqiworld.plugin.interaction.InteractListener;
import com.freshwater.haqiworld.plugin.mob.MobHaqiListener;
import com.freshwater.haqiworld.plugin.pack.ResourcePackService;
import com.freshwater.haqiworld.plugin.voice.HaqiVoicechatPlugin;
import com.freshwater.haqiworld.plugin.voice.VoiceManager;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import org.bukkit.plugin.java.JavaPlugin;

public final class FreshwaterHaqiWorldPlugin extends JavaPlugin {
    private PluginConfig pluginConfig;
    private Leaderboard leaderboard;
    private ResourcePackService resourcePackService;
    private HaqiAttackTask attackTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ensureNewConfigKeys();
        this.pluginConfig = new PluginConfig(this);
        com.freshwater.haqiworld.plugin.data.PlayerData.init(this);
        this.leaderboard = new Leaderboard(this);
        this.leaderboard.load();

        HaqiItems.init(this);

        BukkitVoicechatService svc = getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (svc != null) {
            svc.registerPlugin(new HaqiVoicechatPlugin(pluginConfig));
            getLogger().info("Registered Simple Voice Chat plugin hook.");
        } else {
            getLogger().severe("Simple Voice Chat not found — haqi voice input will not work.");
        }

        getServer().getPluginManager().registerEvents(new CombatListener(this, pluginConfig, leaderboard), this);
        getServer().getPluginManager().registerEvents(new InteractListener(this, pluginConfig), this);
        getServer().getPluginManager().registerEvents(new HaqiCraftListener(), this);
        getServer().getPluginManager().registerEvents(new MobHaqiListener(this, pluginConfig), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Debug key uses /haqi debug — no plugin channel needed.
        HaqiCommand.register(this, pluginConfig, leaderboard);

        this.resourcePackService = new ResourcePackService(this, pluginConfig);
        this.resourcePackService.start();
        getServer().getPluginManager().registerEvents(resourcePackService, this);

        this.attackTask = new HaqiAttackTask(this, pluginConfig);
        this.attackTask.runTaskTimer(this, 1L, 1L);

        getLogger().info("FreshwaterHaqiWorld Paper plugin enabled.");
    }

    @Override
    public void onDisable() {
        if (attackTask != null) {
            attackTask.cancel();
        }
        if (resourcePackService != null) {
            resourcePackService.stop();
        }
        if (leaderboard != null) {
            leaderboard.save();
        }
        VoiceManager.get().clearAll();
    }

    public PluginConfig pluginConfig() {
        return pluginConfig;
    }

    public Leaderboard leaderboard() {
        return leaderboard;
    }

    /** Merge new defaults into an already-generated config.yml without wiping admin edits. */
    private void ensureNewConfigKeys() {
        var c = getConfig();
        boolean changed = false;
        if (c.getInt("fhw-config-revision", 0) < 8) {
            c.set("voice-activation-boost", false);
            c.set("haqi-volume-threshold", 0.08);
            c.set("haqi-reference-level", 0.10);
            c.set("loudness-gain", 2.5);
            c.set("fhw-config-revision", 8);
            changed = true;
            getLogger().info("Config revision 8: relative AGC loudness (fixes stuck ~0.001).");
        }
        if (!c.contains("loudness-gain")) {
            c.set("loudness-gain", 2.5);
            changed = true;
        }
        if (!c.contains("voice-activation-boost")) {
            c.set("voice-activation-boost", false);
            changed = true;
        }
        if (!c.contains("voice-activation-min-loudness")) {
            c.set("voice-activation-min-loudness", 0.55);
            changed = true;
        }
        if (changed) {
            saveConfig();
        }
    }
}
