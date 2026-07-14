package com.freshwater.haqiworld.combat;

import com.freshwater.haqiworld.Config;
import com.freshwater.haqiworld.haqi.HaqiItem;
import com.freshwater.haqiworld.haqi.HaqiTier;
import com.freshwater.haqiworld.data.HaqiPlayerData;
import com.freshwater.haqiworld.registry.ModSounds;
import com.freshwater.haqiworld.voice.VoiceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Runs once per server tick. Reads each player's microphone loudness and, when they are
 * haqi-ing loudly enough (and the cooldown has elapsed), fires a sonic boom scaled by the
 * loudness and the player's permanently-unlocked tier.
 *
 * <p>Also sprinkles in ambient "haqi" sounds for nearby mobs for atmosphere.
 */
public final class HaqiAttackManager {
    private static final Map<UUID, Integer> cooldowns = new HashMap<>();
    private static int ambientTimer = 0;

    private HaqiAttackManager() {
    }

    private static final Map<Integer, Integer> dragonCooldowns = new HashMap<>();

    public static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tickPlayer(player);
        }
        tickAmbientMobSounds(server);
        tickEnderDragons(server);
    }

    /**
     * The ender dragon does not run a standard goal selector, so it gets its boom here:
     * if a dragon has players within range it fires at the nearest one on a cooldown.
     */
    private static void tickEnderDragons(MinecraftServer server) {
        if (!Config.mobsCanHaqi) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerLevel level = player.level();
            AABB area = player.getBoundingBox().inflate(48.0D);
            for (EnderDragon dragon : level.getEntitiesOfClass(EnderDragon.class, area)) {
                tickEnderDragon(level, dragon);
            }
        }
    }

    private static void tickEnderDragon(ServerLevel level, EnderDragon dragon) {
        int cd = dragonCooldowns.getOrDefault(dragon.getId(), 0);
        if (cd > 0) {
            dragonCooldowns.put(dragon.getId(), cd - 1);
            return;
        }
        ServerPlayer nearest = level.getNearestPlayer(dragon, 40.0D) instanceof ServerPlayer sp ? sp : null;
        if (nearest == null) {
            return;
        }
        Vec3 origin = dragon.getEyePosition();
        Vec3 direction = nearest.getEyePosition().subtract(origin);
        double range = Math.min(40.0D, direction.length() + 2.0D);
        level.playSound(null, origin.x, origin.y, origin.z,
                ModSounds.HAQI_MOB.get(), SoundSource.HOSTILE, 2.0F, 0.7F);
        SonicBoomService.fire(level, dragon, origin, direction, range, 3.0D,
                (float) Config.mobHaqiDamage * 1.5F, level.damageSources().sonicBoom(dragon));
        dragonCooldowns.put(dragon.getId(), Config.mobHaqiCooldownTicks);
    }

    private static void tickPlayer(ServerPlayer player) {
        UUID id = player.getUUID();
        int cooldown = cooldowns.getOrDefault(id, 0);
        if (cooldown > 0) {
            cooldowns.put(id, cooldown - 1);
            return;
        }

        if (!VoiceManager.get().isHaqiing(id)) {
            return;
        }

        HaqiTier tier = resolveTier(player);
        if (tier == null) {
            return;
        }

        float loudness = VoiceManager.get().getLoudness(id);
        fireForPlayer(player, tier, loudness);
        cooldowns.put(id, tier.cooldownTicks());
    }

    /**
     * Determines the tier a player attacks at. With {@code requireHaqiItem} they must hold
     * a haqi item; the effective tier is the weaker of the held item's tier and the
     * player's unlocked tier (you cannot punch above your unlocked weight).
     */
    private static HaqiTier resolveTier(ServerPlayer player) {
        HaqiTier unlocked = HaqiPlayerData.getUnlockedTier(player);
        if (!Config.requireHaqiItem) {
            return unlocked;
        }
        HaqiTier held = heldHaqiTier(player);
        if (held == null) {
            return null;
        }
        return held.level() <= unlocked.level() ? held : unlocked;
    }

    private static HaqiTier heldHaqiTier(ServerPlayer player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof HaqiItem haqi) {
            return haqi.getTier();
        }
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof HaqiItem haqi) {
            return haqi.getTier();
        }
        return null;
    }

    private static void fireForPlayer(ServerPlayer player, HaqiTier tier, float loudness) {
        ServerLevel level = player.level();
        Vec3 origin = player.getEyePosition();
        Vec3 direction = player.getViewVector(1.0F);
        double range = tier.rangeFor(loudness);
        float damage = tier.damageFor(loudness);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.HAQI_PLAYER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        SonicBoomService.fire(level, player, origin, direction, range, tier.beamRadius(),
                damage, level.damageSources().sonicBoom(player));
    }

    /**
     * Occasionally plays a cosmetic haqi sound from mobs near players. Hostile mobs get the
     * gruff "耄耋哈气" clips; peaceful mobs get the gentle "温柔老吴" clip.
     */
    private static void tickAmbientMobSounds(MinecraftServer server) {
        if (++ambientTimer < 60) {
            return;
        }
        ambientTimer = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerLevel level = player.level();
            AABB area = player.getBoundingBox().inflate(24.0D);
            for (Mob mob : level.getEntitiesOfClass(Mob.class, area)) {
                if (level.random.nextFloat() < 0.02F) {
                    boolean hostile = mob instanceof Enemy;
                    level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                            hostile ? ModSounds.HAQI_MOB.get() : ModSounds.HAQI_MOB_PEACEFUL.get(),
                            hostile ? SoundSource.HOSTILE : SoundSource.NEUTRAL,
                            0.7F, 0.85F + level.random.nextFloat() * 0.3F);
                }
            }
        }
    }

    public static void clearPlayer(UUID id) {
        cooldowns.remove(id);
    }
}
