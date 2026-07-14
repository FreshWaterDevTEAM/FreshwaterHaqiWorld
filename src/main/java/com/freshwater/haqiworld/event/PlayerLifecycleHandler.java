package com.freshwater.haqiworld.event;

import com.freshwater.haqiworld.combat.HaqiAttackManager;
import com.freshwater.haqiworld.haqi.HaqiTier;
import com.freshwater.haqiworld.data.HaqiPlayerData;
import com.freshwater.haqiworld.registry.ModItems;
import com.freshwater.haqiworld.voice.VoiceManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Player join/respawn bookkeeping: grant the starter haqi on first join, keep the unlocked
 * tier across death, and clean up transient state on logout.
 */
public final class PlayerLifecycleHandler {

    private PlayerLifecycleHandler() {
    }

    public static void register() {
        PlayerEvent.PlayerLoggedInEvent.BUS.addListener(PlayerLifecycleHandler::onLogin);
        PlayerEvent.PlayerLoggedOutEvent.BUS.addListener(PlayerLifecycleHandler::onLogout);
        PlayerEvent.Clone.BUS.addListener(PlayerLifecycleHandler::onClone);
    }

    private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (HaqiPlayerData.getUnlockedLevel(player) <= 0
                && !player.getPersistentData().getBooleanOr("fhw_starter_given", false)) {
            HaqiPlayerData.unlock(player, HaqiTier.BASIC);
            player.addItem(new ItemStack(ModItems.HAQI_BASIC.get()));
            player.getPersistentData().putBoolean("fhw_starter_given", true);
        }
    }

    private static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        VoiceManager.get().clear(event.getEntity().getUUID());
        HaqiAttackManager.clearPlayer(event.getEntity().getUUID());
    }

    private static void onClone(PlayerEvent.Clone event) {
        HaqiPlayerData.copy(event.getOriginal(), event.getEntity());
        event.getEntity().getPersistentData().putBoolean("fhw_starter_given",
                event.getOriginal().getPersistentData().getBooleanOr("fhw_starter_given", false));
    }
}
