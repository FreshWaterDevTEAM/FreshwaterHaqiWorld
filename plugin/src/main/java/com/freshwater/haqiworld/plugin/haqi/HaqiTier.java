package com.freshwater.haqiworld.plugin.haqi;

public enum HaqiTier {
    BASIC(0, "basic", 5.0F, 30, 7.0D, 12.0D, 1.6D),
    UPGRADED(1, "upgraded", 6.0F, 26, 9.0D, 16.0D, 1.9D),
    ENHANCED(2, "enhanced", 7.0F, 22, 11.0D, 20.0D, 2.2D),
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

    public int level() {
        return level;
    }

    public String id() {
        return id;
    }

    public int cooldownTicks() {
        return cooldownTicks;
    }

    public double beamRadius() {
        return beamRadius;
    }

    public float damageFor(float loudness) {
        float l = Math.max(0.0F, Math.min(1.0F, loudness));
        return baseDamage * (0.4F + 0.6F * l);
    }

    public double rangeFor(float loudness) {
        float l = Math.max(0.0F, Math.min(1.0F, loudness));
        return minRange + (maxRange - minRange) * l;
    }

    public static HaqiTier byLevel(int level) {
        HaqiTier best = BASIC;
        for (HaqiTier t : values()) {
            if (t.level <= level && t.level >= best.level) {
                best = t;
            }
        }
        return best;
    }

    public static HaqiTier byId(String id) {
        for (HaqiTier t : values()) {
            if (t.id.equalsIgnoreCase(id)) {
                return t;
            }
        }
        return null;
    }

    public int customModelData() {
        return 1001 + level;
    }

    public static final int WARDEN_ECHO_CMD = 1005;
}
