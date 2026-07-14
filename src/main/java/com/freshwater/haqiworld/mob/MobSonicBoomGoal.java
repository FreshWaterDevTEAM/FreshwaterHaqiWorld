package com.freshwater.haqiworld.mob;

import com.freshwater.haqiworld.Config;
import com.freshwater.haqiworld.combat.SonicBoomService;
import com.freshwater.haqiworld.registry.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Lets strong mobs fire the same sonic boom as players. Attached to iron golems, wither
 * skeletons, vindicators and the ender dragon. Fires at the mob's current target on a
 * cooldown, with a clear line of sight and within range.
 */
public class MobSonicBoomGoal extends Goal {
    private static final double MIN_RANGE = 4.0D;
    private static final double MAX_RANGE = 22.0D;
    private static final double BEAM_RADIUS = 2.0D;

    private final Mob mob;
    private int cooldown;

    public MobSonicBoomGoal(Mob mob) {
        this.mob = mob;
        setFlags(EnumSet.noneOf(Goal.Flag.class));
    }

    @Override
    public boolean canUse() {
        if (!Config.mobsCanHaqi) {
            return false;
        }
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        double distance = mob.distanceTo(target);
        return distance >= MIN_RANGE && distance <= MAX_RANGE && mob.hasLineOfSight(target);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) {
            return;
        }
        mob.getLookControl().setLookAt(target);
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (!mob.hasLineOfSight(target)) {
            return;
        }
        if (!(mob.level() instanceof ServerLevel level)) {
            return;
        }

        Vec3 origin = mob.getEyePosition();
        Vec3 direction = target.getEyePosition().subtract(origin);
        double range = Math.min(MAX_RANGE, direction.length() + 2.0D);
        level.playSound(null, origin.x, origin.y, origin.z,
                ModSounds.HAQI_MOB.get(), SoundSource.HOSTILE, 1.5F, 0.8F + level.random.nextFloat() * 0.3F);
        SonicBoomService.fire(level, mob, origin, direction, range, BEAM_RADIUS,
                (float) Config.mobHaqiDamage, level.damageSources().sonicBoom(mob));
        cooldown = Config.mobHaqiCooldownTicks;
    }

    @Override
    public void stop() {
        cooldown = 0;
    }
}
