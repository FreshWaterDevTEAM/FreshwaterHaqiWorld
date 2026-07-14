package com.freshwater.haqiworld.plugin.combat;

import com.freshwater.haqiworld.plugin.PluginConfig;
import com.freshwater.haqiworld.plugin.haqi.HaqiItems;
import com.freshwater.haqiworld.plugin.haqi.HaqiTier;
import com.freshwater.haqiworld.plugin.voice.VoiceManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class HaqiAttackTask extends BukkitRunnable {
    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final Map<UUID, Integer> cooldowns = new HashMap<>();
    private final Map<Integer, Integer> dragonCooldowns = new HashMap<>();
    private final Map<UUID, Integer> noItemHintCooldown = new HashMap<>();
    private int ambientTimer;

    public HaqiAttackTask(JavaPlugin plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            tickPlayer(player);
        }
        tickAmbientMobSounds();
        tickEnderDragons();
    }

    private void tickPlayer(Player player) {
        UUID id = player.getUniqueId();
        int hintCd = noItemHintCooldown.getOrDefault(id, 0);
        if (hintCd > 0) {
            noItemHintCooldown.put(id, hintCd - 1);
        }

        int cd = cooldowns.getOrDefault(id, 0);
        if (cd > 0) {
            cooldowns.put(id, cd - 1);
            return;
        }
        if (!VoiceManager.get().isHaqiing(id, config)) {
            return;
        }
        HaqiTier tier = HaqiItems.resolveCombatTier(player, config.requireHaqiItem);
        if (tier == null) {
            if (hintCd <= 0) {
                player.sendActionBar(Component.text("请手持哈气物品再哈气", NamedTextColor.RED));
                noItemHintCooldown.put(id, 40);
            }
            return;
        }
        float loudness = VoiceManager.get().getLoudness(id);
        fireForPlayer(player, tier, loudness);
        cooldowns.put(id, tier.cooldownTicks());
    }

    private void fireForPlayer(Player player, HaqiTier tier, float loudness) {
        Vector dir = player.getEyeLocation().getDirection();
        double range = tier.rangeFor(loudness);
        float damage = tier.damageFor(loudness);
        double radius = tier.beamRadiusFor(loudness);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_CHARGE,
                SoundCategory.PLAYERS, 0.6F + loudness * 0.6F, 1.1F - loudness * 0.2F);
        player.getWorld().playSound(player.getLocation(), "fhw:haqi_player", SoundCategory.PLAYERS,
                0.5F + loudness * 0.7F, 1.0F);

        SonicBoomService.fire(player, player.getEyeLocation(), dir, range, radius, damage);
        player.sendActionBar(Component.text(
                String.format("哈气 %.0f%%  伤害%.1f  射程%.0f", loudness * 100.0F, damage, range),
                NamedTextColor.AQUA));
    }

    private void tickEnderDragons() {
        if (!config.mobsCanHaqi) {
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (EnderDragon dragon : player.getWorld().getEntitiesByClass(EnderDragon.class)) {
                if (dragon.getLocation().distanceSquared(player.getLocation()) > 48 * 48) {
                    continue;
                }
                tickDragon(dragon);
            }
        }
    }

    private void tickDragon(EnderDragon dragon) {
        int cd = dragonCooldowns.getOrDefault(dragon.getEntityId(), 0);
        if (cd > 0) {
            dragonCooldowns.put(dragon.getEntityId(), cd - 1);
            return;
        }
        Player nearest = null;
        double best = 40.0;
        for (Player p : dragon.getWorld().getPlayers()) {
            double d = p.getLocation().distance(dragon.getLocation());
            if (d < best) {
                best = d;
                nearest = p;
            }
        }
        if (nearest == null) {
            return;
        }
        Vector dir = nearest.getEyeLocation().toVector().subtract(dragon.getEyeLocation().toVector());
        double range = Math.min(40.0, dir.length() + 2.0);
        dragon.getWorld().playSound(dragon.getLocation(), "fhw:haqi_mob", SoundCategory.HOSTILE, 2.0F, 0.7F);
        dragon.getWorld().playSound(dragon.getLocation(), Sound.ENTITY_WARDEN_ATTACK_IMPACT,
                SoundCategory.HOSTILE, 1.0F, 0.7F);
        SonicBoomService.fire(dragon, dragon.getEyeLocation(), dir, range, 3.0,
                (float) (config.mobHaqiDamage * 1.5));
        dragonCooldowns.put(dragon.getEntityId(), config.mobHaqiCooldownTicks);
    }

    private void tickAmbientMobSounds() {
        // Paper 1.21 has no EntityAmbientEvent — overlay haqi clips frequently near players.
        if (++ambientTimer < 25) {
            return;
        }
        ambientTimer = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (Mob mob : player.getWorld().getNearbyEntitiesByType(Mob.class, player.getLocation(), 20.0)) {
                if (Math.random() >= 0.25) {
                    continue;
                }
                boolean hostile = mob instanceof Enemy;
                String sound = hostile ? "fhw:haqi_mob" : "fhw:haqi_mob_peaceful";
                SoundCategory cat = hostile ? SoundCategory.HOSTILE : SoundCategory.NEUTRAL;
                float pitch = 0.85F + (float) (Math.random() * 0.3);
                mob.getWorld().playSound(mob.getLocation(), sound, cat, 1.0F, pitch);
                mob.getWorld().playSound(mob.getLocation(),
                        hostile ? Sound.ENTITY_WARDEN_TENDRIL_CLICKS : Sound.ENTITY_CAT_PURR,
                        cat, 0.25F, pitch);
            }
        }
    }

    public void clearPlayer(UUID id) {
        cooldowns.remove(id);
        noItemHintCooldown.remove(id);
    }
}
