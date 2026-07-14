package com.freshwater.haqiworld.plugin;

import com.freshwater.haqiworld.plugin.combat.HaqiAttackTask;
import com.freshwater.haqiworld.plugin.data.PlayerData;
import com.freshwater.haqiworld.plugin.haqi.HaqiItems;
import com.freshwater.haqiworld.plugin.haqi.HaqiTier;
import com.freshwater.haqiworld.plugin.voice.VoiceManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerListener implements Listener {
    private final FreshwaterHaqiWorldPlugin plugin;

    public PlayerListener(FreshwaterHaqiWorldPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!PlayerData.isStarterGiven(player)) {
            PlayerData.unlock(player, HaqiTier.BASIC);
            player.getInventory().addItem(HaqiItems.createHaqi(HaqiTier.BASIC));
            PlayerData.setStarterGiven(player);
        }
        player.sendMessage(net.kyori.adventure.text.Component.text(
                "[哈气] 手持哈气物品后对着麦克风哈气；物品不足用 /haqi give；调试按住 H；状态 /haqi status",
                net.kyori.adventure.text.format.NamedTextColor.AQUA));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        VoiceManager.get().clear(event.getPlayer().getUniqueId());
    }
}
