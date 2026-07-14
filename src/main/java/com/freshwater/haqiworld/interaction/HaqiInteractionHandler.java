package com.freshwater.haqiworld.interaction;

import com.freshwater.haqiworld.haqi.HaqiItem;
import com.freshwater.haqiworld.haqi.HaqiTier;
import com.freshwater.haqiworld.data.HaqiPlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/**
 * Handles "absorbing" a haqi: sneak + right click with a haqi item permanently unlocks
 * that tier for the player (backpack-slot-upgrade style) and consumes one item. After
 * that the player fires at their highest unlocked tier whenever they hold any haqi.
 */
public final class HaqiInteractionHandler {

    private HaqiInteractionHandler() {
    }

    public static void register() {
        PlayerInteractEvent.RightClickItem.BUS.addListener(HaqiInteractionHandler::onRightClickItem);
    }

    private static boolean onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide()) {
            return false;
        }
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isShiftKeyDown()) {
            return false;
        }
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof HaqiItem haqi)) {
            return false;
        }

        HaqiTier tier = haqi.getTier();
        if (HaqiPlayerData.unlock(player, tier)) {
            stack.shrink(1);
            player.displayClientMessage(
                    Component.translatable("fhw.haqi.unlocked", Component.translatable("fhw.tier." + tier.id())), true);
        } else {
            player.displayClientMessage(Component.translatable("fhw.haqi.already_unlocked"), true);
        }
        return true;
    }
}
