package com.freshwater.haqiworld.data;

import com.freshwater.haqiworld.haqi.HaqiTier;
import net.minecraft.world.entity.player.Player;

/**
 * Stores the highest haqi tier a player has permanently unlocked.
 *
 * <p>The value lives in the player's Forge persistent data (NBT). Once unlocked, a tier
 * stays unlocked even if the haqi item is lost, and it is carried across death/respawn by
 * the clone handler.
 */
public final class HaqiPlayerData {
    private static final String TAG_TIER = "fhw_haqi_tier";

    private HaqiPlayerData() {
    }

    public static int getUnlockedLevel(Player player) {
        return player.getPersistentData().getIntOr(TAG_TIER, 0);
    }

    public static HaqiTier getUnlockedTier(Player player) {
        return HaqiTier.byLevel(getUnlockedLevel(player));
    }

    /**
     * Permanently unlocks the given tier if it is stronger than the current one.
     *
     * @return true if this raised the player's unlocked tier
     */
    public static boolean unlock(Player player, HaqiTier tier) {
        if (tier.level() > getUnlockedLevel(player)) {
            player.getPersistentData().putInt(TAG_TIER, tier.level());
            return true;
        }
        return false;
    }

    /** Copies the unlocked tier from a previous player instance (used on respawn). */
    public static void copy(Player from, Player to) {
        to.getPersistentData().putInt(TAG_TIER, getUnlockedLevel(from));
    }
}
