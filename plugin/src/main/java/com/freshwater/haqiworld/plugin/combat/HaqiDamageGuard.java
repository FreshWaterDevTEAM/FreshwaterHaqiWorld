package com.freshwater.haqiworld.plugin.combat;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Marks entities currently receiving haqi sonic-boom damage so the melee-cancel
 * listener does not wipe {@code LivingEntity#damage(amount, player)} (which Paper
 * classifies as {@code ENTITY_ATTACK}).
 */
public final class HaqiDamageGuard {
    private static final Set<UUID> ACTIVE = ConcurrentHashMap.newKeySet();

    private HaqiDamageGuard() {
    }

    public static void enter(UUID entityId) {
        ACTIVE.add(entityId);
    }

    public static void leave(UUID entityId) {
        ACTIVE.remove(entityId);
    }

    public static boolean isActive(UUID entityId) {
        return ACTIVE.contains(entityId);
    }
}
