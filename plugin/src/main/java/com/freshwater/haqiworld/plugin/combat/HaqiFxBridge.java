package com.freshwater.haqiworld.plugin.combat;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;

/**
 * Sends recolored sonic-boom FX to Forge clients via plugin channel {@code fhw:fx}.
 */
public final class HaqiFxBridge {
    public static final String CHANNEL = "fhw:fx";
    private static JavaPlugin plugin;

    private HaqiFxBridge() {
    }

    public static void init(JavaPlugin plugin) {
        HaqiFxBridge.plugin = plugin;
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL);
    }

    public static void sendRecoloredBoom(Location origin, Vector direction, double range) {
        if (plugin == null || origin.getWorld() == null) {
            return;
        }
        Vector dir = direction.clone();
        if (dir.lengthSquared() < 1.0E-6) {
            return;
        }
        dir.normalize();
        byte[] payload = encode(origin, dir, range);
        if (payload == null) {
            return;
        }
        Collection<? extends Player> viewers = origin.getWorld()
                .getNearbyEntitiesByType(Player.class, origin, 64.0);
        for (Player player : viewers) {
            player.sendPluginMessage(plugin, CHANNEL, payload);
        }
    }

    private static byte[] encode(Location origin, Vector dir, double range) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bout);
            out.writeByte(1);
            out.writeDouble(origin.getX());
            out.writeDouble(origin.getY());
            out.writeDouble(origin.getZ());
            out.writeFloat((float) dir.getX());
            out.writeFloat((float) dir.getY());
            out.writeFloat((float) dir.getZ());
            out.writeFloat((float) range);
            out.flush();
            return bout.toByteArray();
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to encode fhw:fx payload: " + e.getMessage());
            return null;
        }
    }
}
