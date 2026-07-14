package com.freshwater.haqiworld.plugin.data;

import com.freshwater.haqiworld.plugin.haqi.HaqiTier;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlayerData {
    private static NamespacedKey KEY_TIER;
    private static NamespacedKey KEY_STARTER;

    private PlayerData() {
    }

    public static void init(JavaPlugin plugin) {
        KEY_TIER = new NamespacedKey(plugin, "haqi_tier");
        KEY_STARTER = new NamespacedKey(plugin, "starter_given");
    }

    public static int getUnlockedLevel(Player player) {
        Integer v = player.getPersistentDataContainer().get(KEY_TIER, PersistentDataType.INTEGER);
        return v == null ? 0 : v;
    }

    public static HaqiTier getUnlockedTier(Player player) {
        return HaqiTier.byLevel(getUnlockedLevel(player));
    }

    public static boolean unlock(Player player, HaqiTier tier) {
        int current = getUnlockedLevel(player);
        if (tier.level() <= current) {
            return false;
        }
        setTier(player, tier);
        return true;
    }

    public static void setTier(Player player, HaqiTier tier) {
        player.getPersistentDataContainer().set(KEY_TIER, PersistentDataType.INTEGER, tier.level());
    }

    public static boolean isStarterGiven(Player player) {
        Byte v = player.getPersistentDataContainer().get(KEY_STARTER, PersistentDataType.BYTE);
        return v != null && v == 1;
    }

    public static void setStarterGiven(Player player) {
        player.getPersistentDataContainer().set(KEY_STARTER, PersistentDataType.BYTE, (byte) 1);
    }
}
