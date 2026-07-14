package com.freshwater.haqiworld.plugin.combat;

import com.freshwater.haqiworld.plugin.PluginConfig;
import com.freshwater.haqiworld.plugin.data.Leaderboard;
import com.freshwater.haqiworld.plugin.haqi.HaqiItems;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class CombatListener implements Listener {
    private final PluginConfig config;
    private final Leaderboard leaderboard;

    public CombatListener(JavaPlugin plugin, PluginConfig config, Leaderboard leaderboard) {
        this.config = config;
        this.leaderboard = leaderboard;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMelee(EntityDamageByEntityEvent event) {
        if (!config.removeMeleeCombat) {
            return;
        }
        if (HaqiDamageGuard.isActive(event.getEntity().getUniqueId())) {
            return;
        }
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                || cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null && config.removeMeleeCombat) {
            // Melee is cancelled, so player kills via damage(source) are haqi sonic booms.
            leaderboard.addKill(killer);
        }

        if (event.getEntity() instanceof Warden) {
            event.getDrops().add(HaqiItems.createWardenEcho());
        }
    }
}
