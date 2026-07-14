package com.freshwater.haqiworld.combat;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Fires a Warden-style sonic boom: a straight beam of {@link ParticleTypes#SONIC_BOOM}
 * particles accompanied by the Warden boom sound, dealing damage and knockback to every
 * living entity inside the beam.
 *
 * <p>Damage and knockback mirror the vanilla {@code SonicBoom} warden behavior, but the
 * beam is a cylinder (so it can hit several mobs) whose length scales with loudness.
 */
public final class SonicBoomService {

    private SonicBoomService() {
    }

    /**
     * @param level        the server level the boom happens in
     * @param source       the entity firing the boom (excluded from hits)
     * @param origin       beam start (usually the source's eye position)
     * @param direction    beam direction (will be normalized)
     * @param range        beam length in blocks
     * @param beamRadius   beam radius in blocks
     * @param damage       damage dealt to each entity hit
     * @param damageSource the damage source to attribute hits to
     */
    public static void fire(ServerLevel level, Entity source, Vec3 origin, Vec3 direction,
                            double range, double beamRadius, float damage, DamageSource damageSource) {
        Vec3 dir = direction.normalize();
        if (dir.lengthSqr() < 1.0E-6) {
            return;
        }

        spawnBeamParticles(level, origin, dir, range);
        level.playSound(null, origin.x, origin.y, origin.z,
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 3.0F, 1.0F);

        AABB searchBox = new AABB(origin, origin.add(dir.scale(range))).inflate(beamRadius + 1.0D);
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, searchBox,
                e -> e != source && e.isAlive());

        for (LivingEntity target : candidates) {
            Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            Vec3 toTarget = center.subtract(origin);
            double projection = toTarget.dot(dir);
            if (projection <= 0.0D || projection > range) {
                continue;
            }
            Vec3 closestPointOnBeam = origin.add(dir.scale(projection));
            double perpendicular = center.distanceTo(closestPointOnBeam);
            if (perpendicular > beamRadius + target.getBbWidth() * 0.5D) {
                continue;
            }

            if (target.hurtServer(level, damageSource, damage)) {
                applyKnockback(target, toTarget.normalize());
            }
        }
    }

    private static void spawnBeamParticles(ServerLevel level, Vec3 origin, Vec3 dir, double range) {
        int steps = Mth.floor(range) + 1;
        for (int i = 1; i <= steps; i++) {
            Vec3 point = origin.add(dir.scale(i));
            level.sendParticles(ParticleTypes.SONIC_BOOM, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static void applyKnockback(LivingEntity target, Vec3 dir) {
        double resistance = target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        double horizontal = 2.5D * (1.0D - resistance);
        double vertical = 0.5D * (1.0D - resistance);
        target.push(dir.x() * horizontal, dir.y() * vertical + 0.1D, dir.z() * horizontal);
        target.hurtMarked = true;
    }
}
