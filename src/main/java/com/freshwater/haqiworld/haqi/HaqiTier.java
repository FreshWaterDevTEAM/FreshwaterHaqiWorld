package com.freshwater.haqiworld.haqi;

/**
 * The progression tiers for the haqi (breath) attack.
 *
 * <p>Damage roughly mirrors the vanilla sword ladder (stone / iron / diamond), with a
 * top "warden" tier crafted from Warden drops. Attack frequency is intentionally lower
 * than a sword (longer cooldowns).
 */
public enum HaqiTier {
    /** Equivalent to a stone sword. The starting haqi. */
    BASIC(0, "basic", 5.0F, 30, 7.0D, 12.0D, 1.6D),
    /** Equivalent to an iron sword. */
    UPGRADED(1, "upgraded", 6.0F, 26, 9.0D, 16.0D, 1.9D),
    /** Equivalent to a diamond sword. */
    ENHANCED(2, "enhanced", 7.0F, 22, 11.0D, 20.0D, 2.2D),
    /** Top tier, crafted from Warden material. */
    WARDEN(3, "warden", 10.0F, 18, 14.0D, 26.0D, 2.6D);

    private final int level;
    private final String id;
    private final float baseDamage;
    private final int cooldownTicks;
    private final double minRange;
    private final double maxRange;
    private final double beamRadius;

    HaqiTier(int level, String id, float baseDamage, int cooldownTicks,
             double minRange, double maxRange, double beamRadius) {
        this.level = level;
        this.id = id;
        this.baseDamage = baseDamage;
        this.cooldownTicks = cooldownTicks;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.beamRadius = beamRadius;
    }

    /** Numeric rank, used for permanent-unlock comparisons (higher = stronger). */
    public int level() {
        return level;
    }

    /** Lowercase id used in registry names, recipes and translation keys. */
    public String id() {
        return id;
    }

    public int cooldownTicks() {
        return cooldownTicks;
    }

    public double beamRadius() {
        return beamRadius;
    }

    /**
     * Damage dealt by the boom at a given loudness (0..1). Quiet breaths deal a fraction
     * of the tier damage; a full-volume haqi deals the full tier damage.
     */
    public float damageFor(float loudness) {
        float clamped = clamp01(loudness);
        // Even a soft haqi does something (40%), scaling up to full at max volume.
        return baseDamage * (0.4F + 0.6F * clamped);
    }

    /** Beam length (blocks) for a given loudness (0..1). */
    public double rangeFor(float loudness) {
        float clamped = clamp01(loudness);
        return minRange + (maxRange - minRange) * clamped;
    }

    public static HaqiTier byLevel(int level) {
        for (HaqiTier tier : values()) {
            if (tier.level == level) {
                return tier;
            }
        }
        return BASIC;
    }

    private static float clamp01(float v) {
        if (v < 0.0F) return 0.0F;
        if (v > 1.0F) return 1.0F;
        return v;
    }
}
