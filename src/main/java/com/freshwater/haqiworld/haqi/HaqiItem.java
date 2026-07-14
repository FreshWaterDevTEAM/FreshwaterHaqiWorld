package com.freshwater.haqiworld.haqi;

import net.minecraft.world.item.Item;

/**
 * A "haqi" item. Holding one lets the player fire a sonic boom by breathing into the mic.
 *
 * <p>Absorbing a haqi (sneak + right click) permanently unlocks its {@link HaqiTier} for
 * the player, after which they fire at their highest unlocked tier regardless of which
 * haqi item is held. Interaction logic lives in the event handlers so it can stay aligned
 * with the version-specific interaction API.
 */
public class HaqiItem extends Item {
    private final HaqiTier tier;

    public HaqiItem(Properties properties, HaqiTier tier) {
        super(properties);
        this.tier = tier;
    }

    public HaqiTier getTier() {
        return tier;
    }
}
