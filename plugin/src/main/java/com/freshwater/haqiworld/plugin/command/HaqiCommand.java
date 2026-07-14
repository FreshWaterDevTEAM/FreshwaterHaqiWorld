package com.freshwater.haqiworld.plugin.command;

import com.freshwater.haqiworld.plugin.PluginConfig;
import com.freshwater.haqiworld.plugin.data.Leaderboard;
import com.freshwater.haqiworld.plugin.data.PlayerData;
import com.freshwater.haqiworld.plugin.haqi.HaqiTier;
import com.freshwater.haqiworld.plugin.voice.VoiceManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class HaqiCommand implements CommandExecutor, TabCompleter {
    private final PluginConfig config;
    private final Leaderboard leaderboard;

    private HaqiCommand(PluginConfig config, Leaderboard leaderboard) {
        this.config = config;
        this.leaderboard = leaderboard;
    }

    public static void register(JavaPlugin plugin, PluginConfig config, Leaderboard leaderboard) {
        HaqiCommand cmd = new HaqiCommand(config, leaderboard);
        var pluginCmd = plugin.getCommand("haqi");
        if (pluginCmd != null) {
            pluginCmd.setExecutor(cmd);
            pluginCmd.setTabCompleter(cmd);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("用法: /haqi <top|unlock>", NamedTextColor.YELLOW));
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("top")) {
            List<Leaderboard.Entry> top = leaderboard.top(config.leaderboardSize);
            if (top.isEmpty()) {
                sender.sendMessage(Component.text("暂无哈气击杀记录。", NamedTextColor.GRAY));
                return true;
            }
            sender.sendMessage(Component.text("=== 哈气击杀排行榜 ===", NamedTextColor.GOLD));
            int rank = 1;
            for (Leaderboard.Entry e : top) {
                sender.sendMessage(Component.text(rank + ". " + e.name() + " - " + e.kills() + " 击杀",
                        NamedTextColor.WHITE));
                rank++;
            }
            return true;
        }
        if (sub.equals("debug")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("必须由玩家执行。", NamedTextColor.RED));
                return true;
            }
            if (!config.enableDebugKeybind) {
                sender.sendMessage(Component.text("调试哈气已在配置中关闭。", NamedTextColor.RED));
                return true;
            }
            boolean on = args.length < 2 || !args[1].equalsIgnoreCase("off");
            if (on) {
                VoiceManager.get().setDebugOverride(player.getUniqueId(), config.debugKeybindLoudness);
            } else {
                VoiceManager.get().setDebugOverride(player.getUniqueId(), 0.0F);
            }
            return true;
        }
        if (sub.equals("unlock")) {
            if (!sender.hasPermission("fhw.unlock")) {
                sender.sendMessage(Component.text("权限不足。", NamedTextColor.RED));
                return true;
            }
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("必须由玩家执行。", NamedTextColor.RED));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(Component.text("用法: /haqi unlock <basic|upgraded|enhanced|warden>",
                        NamedTextColor.YELLOW));
                return true;
            }
            HaqiTier tier = HaqiTier.byId(args[1]);
            if (tier == null) {
                sender.sendMessage(Component.text("未知哈气阶: " + args[1], NamedTextColor.RED));
                return true;
            }
            PlayerData.setTier(player, tier);
            sender.sendMessage(Component.text("已解锁 " + tier.id() + " 哈气。", NamedTextColor.GREEN));
            return true;
        }
        sender.sendMessage(Component.text("用法: /haqi <top|unlock>", NamedTextColor.YELLOW));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("top", "unlock", "debug").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("debug")) {
            return Arrays.asList("on", "off").stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("unlock")) {
            List<String> ids = new ArrayList<>();
            for (HaqiTier t : HaqiTier.values()) {
                if (t.id().startsWith(args[1].toLowerCase(Locale.ROOT))) {
                    ids.add(t.id());
                }
            }
            return ids;
        }
        return List.of();
    }
}
