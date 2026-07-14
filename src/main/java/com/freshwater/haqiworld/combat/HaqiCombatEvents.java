package com.freshwater.haqiworld.combat;

import com.freshwater.haqiworld.Config;
import com.freshwater.haqiworld.data.HaqiLeaderboard;
import com.freshwater.haqiworld.registry.ModItems;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

/**
 * Wires up the combat rules:
 * <ul>
 *     <li>cancels vanilla melee weapon damage from players (only haqi hurts mobs);</li>
 *     <li>runs the per-tick haqi attack manager;</li>
 *     <li>credits haqi kills to the leaderboard;</li>
 *     <li>drops the Warden material used for the top-tier haqi.</li>
 * </ul>
 */
public final class HaqiCombatEvents {

    private HaqiCombatEvents() {
    }

    public static void register() {
        TickEvent.ServerTickEvent.Post.BUS.addListener(HaqiCombatEvents::onServerTick);
        LivingDamageEvent.BUS.addListener(HaqiCombatEvents::onLivingDamage);
        LivingDeathEvent.BUS.addListener(HaqiCombatEvents::onLivingDeath);
        LivingDropsEvent.BUS.addListener(HaqiCombatEvents::onLivingDrops);
    }

    private static void onServerTick(TickEvent.ServerTickEvent.Post event) {
        HaqiAttackManager.tick(event.server());
    }

    /**
     * Cancels direct player melee attacks so combat must go through haqi. The sonic boom
     * uses the {@code sonic_boom} damage type, so it is never cancelled here.
     *
     * @return true to cancel the damage
     */
    private static boolean onLivingDamage(LivingDamageEvent event) {
        if (!Config.removeMeleeCombat) {
            return false;
        }
        DamageSource source = event.getSource();
        return source.is(DamageTypes.PLAYER_ATTACK);
    }

    private static void onLivingDeath(LivingDeathEvent event) {
        DamageSource source = event.getSource();
        if (!source.is(DamageTypes.SONIC_BOOM)) {
            return;
        }
        Entity attacker = source.getEntity();
        if (!(attacker instanceof ServerPlayer player)) {
            return;
        }
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return;
        }
        HaqiLeaderboard.get(server).addKill(player);
    }

    private static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Warden)) {
            return;
        }
        ItemStack drop = new ItemStack(ModItems.WARDEN_ECHO.get());
        event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), drop));
    }
}
