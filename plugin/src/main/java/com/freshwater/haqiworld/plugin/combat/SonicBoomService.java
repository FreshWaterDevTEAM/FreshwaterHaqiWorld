package com.freshwater.haqiworld.plugin.combat;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public final class SonicBoomService {
    public enum Visual {
        /** Vanilla cyan sonic boom (Warden mob / Warden haqi). */
        VANILLA,
        /** Recolored frames via Forge client channel (iron/gold/diamond haqi & other mobs). */
        RECOLORED
    }

    private SonicBoomService() {
    }

    public static void fire(LivingEntity source, Location origin, Vector direction,
                            double range, double beamRadius, float damage) {
        fire(source, origin, direction, range, beamRadius, damage, Visual.RECOLORED);
    }

    public static void fire(LivingEntity source, Location origin, Vector direction,
                            double range, double beamRadius, float damage, Visual visual) {
        Vector dir = direction.clone();
        if (dir.lengthSquared() < 1.0E-6) {
            return;
        }
        dir.normalize();

        if (visual == Visual.VANILLA) {
            for (int i = 1; i <= (int) Math.floor(range); i++) {
                Location point = origin.clone().add(dir.clone().multiply(i));
                origin.getWorld().spawnParticle(Particle.SONIC_BOOM, point, 1, 0, 0, 0, 0);
            }
        } else {
            HaqiFxBridge.sendRecoloredBoom(origin, dir, range);
        }

        origin.getWorld().playSound(origin, Sound.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.HOSTILE, 3.0F, 1.0F);

        Location end = origin.clone().add(dir.clone().multiply(range));
        BoundingBox box = BoundingBox.of(origin, end).expand(beamRadius + 1.0);
        for (Entity entity : origin.getWorld().getNearbyEntities(box)) {
            if (!(entity instanceof LivingEntity target) || target.equals(source) || target.isDead()) {
                continue;
            }
            Location center = target.getLocation().add(0, target.getHeight() * 0.5, 0);
            Vector toTarget = center.toVector().subtract(origin.toVector());
            double projection = toTarget.dot(dir);
            if (projection <= 0.0 || projection > range) {
                continue;
            }
            Location closest = origin.clone().add(dir.clone().multiply(projection));
            if (center.distance(closest) > beamRadius + target.getWidth() * 0.5) {
                continue;
            }
            hurt(target, source, damage);
            applyKnockback(target, toTarget.clone().normalize());
        }
    }

    private static void applyKnockback(LivingEntity target, Vector dir) {
        double resistance = 0.0;
        var attr = target.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
        if (attr != null) {
            resistance = attr.getValue();
        }
        double horizontal = 2.5 * (1.0 - resistance);
        double vertical = 0.5 * (1.0 - resistance);
        Vector push = dir.clone().multiply(horizontal);
        push.setY(dir.getY() * vertical + 0.1);
        target.setVelocity(target.getVelocity().add(push));
    }

    private static void hurt(LivingEntity target, LivingEntity source, float damage) {
        HaqiDamageGuard.enter(target.getUniqueId());
        try {
            target.damage(damage, source);
        } finally {
            HaqiDamageGuard.leave(target.getUniqueId());
        }
    }
}
