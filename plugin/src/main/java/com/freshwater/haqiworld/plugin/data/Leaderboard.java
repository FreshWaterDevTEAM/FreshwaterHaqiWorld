package com.freshwater.haqiworld.plugin.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Leaderboard {
    public record Entry(UUID uuid, String name, int kills) {
    }

    private final JavaPlugin plugin;
    private final File file;
    private final Map<UUID, Entry> entries = new HashMap<>();

    public Leaderboard(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "leaderboard.yml");
    }

    public void load() {
        entries.clear();
        if (!file.exists()) {
            return;
        }
        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String key : yaml.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String name = yaml.getString(key + ".name", "???");
                int kills = yaml.getInt(key + ".kills", 0);
                entries.put(uuid, new Entry(uuid, name, kills));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void save() {
        FileConfiguration yaml = new YamlConfiguration();
        for (Entry e : entries.values()) {
            yaml.set(e.uuid().toString() + ".name", e.name());
            yaml.set(e.uuid().toString() + ".kills", e.kills());
        }
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            yaml.save(file);
        } catch (IOException ex) {
            plugin.getLogger().warning("Failed to save leaderboard: " + ex.getMessage());
        }
    }

    public void addKill(Player player) {
        Entry prev = entries.getOrDefault(player.getUniqueId(),
                new Entry(player.getUniqueId(), player.getName(), 0));
        entries.put(player.getUniqueId(), new Entry(player.getUniqueId(), player.getName(), prev.kills() + 1));
        save();
    }

    public List<Entry> top(int limit) {
        List<Entry> list = new ArrayList<>(entries.values());
        list.sort(Comparator.comparingInt(Entry::kills).reversed());
        if (list.size() > limit) {
            return list.subList(0, limit);
        }
        return list;
    }
}
