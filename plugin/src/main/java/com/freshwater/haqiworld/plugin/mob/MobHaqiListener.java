package com.freshwater.haqiworld.plugin.mob;

import com.freshwater.haqiworld.plugin.PluginConfig;
import com.freshwater.haqiworld.plugin.combat.SonicBoomService;
import org.bukkit.SoundCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.Warden;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MobHaqiListener implements Listener {
    private static final double MIN_RANGE = 4.0;
    private static final double MAX_RANGE = 22.0;
    private static final double BEAM_RADIUS = 2.0;

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final Map<UUID, Integer> cooldowns = new HashMap<>();
    private boolean taskStarted;

    public MobHaqiListener(JavaPlugin plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
        startTicker();
    }

    private void startTicker() {
        if (taskStarted) {
            return;
        }
        taskStarted = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!config.mobsCanHaqi) {
                    return;
                }
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    for (Mob mob : player.getWorld().getNearbyEntitiesByType(Mob.class, player.getLocation(), 32.0)) {
                        if (!isStrongMob(mob)) {
                            continue;
                        }
                        tickMob(mob);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 1L);
    }

    private static boolean isStrongMob(Mob mob) {
        return mob instanceof IronGolem
                || mob instanceof WitherSkeleton
                || mob instanceof Vindicator
                || mob instanceof Warden
                || mob.getType() == EntityType.IRON_GOLEM
                || mob.getType() == EntityType.WITHER_SKELETON
                || mob.getType() == EntityType.VINDICATOR
                || mob.getType() == EntityType.WARDEN;
    }

    private void tickMob(Mob mob) {
        UUID id = mob.getUniqueId();
        int cd = cooldowns.getOrDefault(id, 0);
        if (cd > 0) {
            cooldowns.put(id, cd - 1);
            return;
        }
        LivingEntity target = mob.getTarget();
        if (target == null || target.isDead()) {
            return;
        }
        double distance = mob.getLocation().distance(target.getLocation());
        if (distance < MIN_RANGE || distance > MAX_RANGE) {
            return;
        }
        if (!mob.hasLineOfSight(target)) {
            return;
        }
        Vector dir = target.getEyeLocation().toVector().subtract(mob.getEyeLocation().toVector());
        double range = Math.min(MAX_RANGE, dir.length() + 2.0);
        boolean isWarden = mob instanceof Warden;
        float damage = isWarden
                ? (float) config.wardenHaqiDamage
                : (float) config.mobHaqiDamage;
        SonicBoomService.Visual visual = isWarden
                ? SonicBoomService.Visual.VANILLA
                : SonicBoomService.Visual.RECOLORED;
        mob.getWorld().playSound(mob.getLocation(), "fhw:haqi_mob", SoundCategory.HOSTILE,
                1.5F, 0.8F + (float) (Math.random() * 0.3));
        SonicBoomService.fire(mob, mob.getEyeLocation(), dir, range, BEAM_RADIUS, damage, visual);
        cooldowns.put(id, config.mobHaqiCooldownTicks);
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent event) {
        // Ticker covers existing and new mobs near players; no per-entity goal needed.
    }
}
