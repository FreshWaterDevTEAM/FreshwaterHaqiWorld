package com.freshwater.haqiworld.mob;

import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

/**
 * Attaches the sonic-boom goal to the strong mobs that are allowed to haqi back at the
 * player. The ender dragon is handled separately in the tick manager because it does not
 * run a standard goal selector.
 */
public final class MobHaqiHandler {

    private MobHaqiHandler() {
    }

    public static void register() {
        EntityJoinLevelEvent.BUS.addListener(MobHaqiHandler::onEntityJoin);
    }

    private static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof IronGolem golem) {
            golem.goalSelector.addGoal(1, new MobSonicBoomGoal(golem));
        } else if (event.getEntity() instanceof WitherSkeleton witherSkeleton) {
            witherSkeleton.goalSelector.addGoal(1, new MobSonicBoomGoal(witherSkeleton));
        } else if (event.getEntity() instanceof Vindicator vindicator) {
            vindicator.goalSelector.addGoal(1, new MobSonicBoomGoal(vindicator));
        }
    }
}
