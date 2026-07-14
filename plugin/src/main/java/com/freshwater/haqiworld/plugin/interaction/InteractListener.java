package com.freshwater.haqiworld.plugin.interaction;

import com.freshwater.haqiworld.plugin.PluginConfig;
import com.freshwater.haqiworld.plugin.data.PlayerData;
import com.freshwater.haqiworld.plugin.haqi.HaqiItems;
import com.freshwater.haqiworld.plugin.haqi.HaqiTier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class InteractListener implements Listener {
    private final PluginConfig config;

    public InteractListener(JavaPlugin plugin, PluginConfig config) {
        this.config = config;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            return;
        }
        ItemStack stack = player.getInventory().getItemInMainHand();
        HaqiTier tier = HaqiItems.getHaqiTier(stack);
        if (tier == null) {
            return;
        }
        event.setCancelled(true);
        if (PlayerData.unlock(player, tier)) {
            stack.setAmount(stack.getAmount() - 1);
            player.sendActionBar(Component.translatable("fhw.haqi.unlocked",
                    Component.translatable("fhw.tier." + tier.id())).color(NamedTextColor.GREEN));
            player.sendMessage(Component.text("你已永久解锁了 ", NamedTextColor.GREEN)
                    .append(Component.text(tier.id(), NamedTextColor.AQUA))
                    .append(Component.text(" 哈气！", NamedTextColor.GREEN)));
        } else {
            player.sendMessage(Component.text("你已经掌握了同等或更强的哈气。", NamedTextColor.YELLOW));
        }
    }
}
