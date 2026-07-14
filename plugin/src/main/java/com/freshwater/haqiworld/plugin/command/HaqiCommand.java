package com.freshwater.haqiworld.plugin.command;

import com.freshwater.haqiworld.plugin.PluginConfig;
import com.freshwater.haqiworld.plugin.data.Leaderboard;
import com.freshwater.haqiworld.plugin.data.PlayerData;
import com.freshwater.haqiworld.plugin.haqi.HaqiItems;
import com.freshwater.haqiworld.plugin.haqi.HaqiTier;
import com.freshwater.haqiworld.plugin.voice.VoiceManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
            sender.sendMessage(Component.text(
                    "用法: /haqi <give|giveall|clear|top|unlock|debug|status>", NamedTextColor.YELLOW));
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        return switch (sub) {
            case "top" -> showTop(sender);
            case "debug" -> debug(sender, args);
            case "unlock" -> unlock(sender, args);
            case "give" -> give(sender, args);
            case "giveall" -> giveAll(sender);
            case "clear" -> clear(sender, args);
            case "status" -> status(sender);
            default -> {
                sender.sendMessage(Component.text(
                        "用法: /haqi <give|giveall|clear|top|unlock|debug|status>", NamedTextColor.YELLOW));
                yield true;
            }
        };
    }

    private boolean showTop(CommandSender sender) {
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

    private boolean debug(CommandSender sender, String[] args) {
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
            player.sendActionBar(Component.text("调试哈气：开", NamedTextColor.GREEN));
        } else {
            VoiceManager.get().setDebugOverride(player.getUniqueId(), 0.0F);
            player.sendActionBar(Component.text("调试哈气：关", NamedTextColor.GRAY));
        }
        return true;
    }

    private boolean unlock(CommandSender sender, String[] args) {
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

    private boolean give(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fhw.give")) {
            sender.sendMessage(Component.text("权限不足（需要 OP / fhw.give）。", NamedTextColor.RED));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("必须由玩家执行。控制台请用 /haqi giveall。", NamedTextColor.RED));
            return true;
        }
        String which = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "all";
        List<ItemStack> gifts = new ArrayList<>();
        if (which.equals("all")) {
            for (HaqiTier tier : HaqiTier.values()) {
                gifts.add(HaqiItems.createHaqi(tier));
            }
            gifts.add(HaqiItems.createWardenEcho());
        } else if (which.equals("echo") || which.equals("warden_echo")) {
            gifts.add(HaqiItems.createWardenEcho());
        } else {
            HaqiTier tier = HaqiTier.byId(which);
            if (tier == null) {
                sender.sendMessage(Component.text("用法: /haqi give [all|basic|upgraded|enhanced|warden|echo]",
                        NamedTextColor.YELLOW));
                return true;
            }
            gifts.add(HaqiItems.createHaqi(tier));
        }
        for (ItemStack stack : gifts) {
            player.getInventory().addItem(stack);
        }
        // Also unlock the highest given haqi so they can use it immediately.
        if (which.equals("all")) {
            PlayerData.setTier(player, HaqiTier.WARDEN);
        } else {
            HaqiTier tier = HaqiTier.byId(which);
            if (tier != null) {
                PlayerData.setTier(player, tier);
            }
        }
        sender.sendMessage(Component.text("已发放哈气物品，请手持后对着目标哈气（或按住 H 调试）。",
                NamedTextColor.GREEN));
        return true;
    }

    /** Give every online player one basic haqi item and ensure BASIC unlock. */
    private boolean giveAll(CommandSender sender) {
        if (!sender.hasPermission("fhw.give")) {
            sender.sendMessage(Component.text("权限不足（需要 OP / fhw.give）。", NamedTextColor.RED));
            return true;
        }
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getInventory().addItem(HaqiItems.createHaqi(HaqiTier.BASIC));
            if (PlayerData.getUnlockedLevel(player) < HaqiTier.BASIC.level()) {
                PlayerData.setTier(player, HaqiTier.BASIC);
            }
            player.sendMessage(Component.text("[哈气] 管理员发放了一份基础哈气。", NamedTextColor.AQUA));
            count++;
        }
        sender.sendMessage(Component.text("已向 " + count + " 名在线玩家发放基础哈气。", NamedTextColor.GREEN));
        return true;
    }

    /**
     * Clear haqi items from inventory and reset unlock to BASIC.
     * Usage: /haqi clear [player|all]
     */
    private boolean clear(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fhw.clear")) {
            sender.sendMessage(Component.text("权限不足（需要 OP / fhw.clear）。", NamedTextColor.RED));
            return true;
        }
        if (args.length < 2) {
            if (sender instanceof Player self) {
                int removed = clearPlayer(self);
                sender.sendMessage(Component.text(
                        "已清除你的哈气物品（" + removed + "）并退回基础解锁。", NamedTextColor.GREEN));
                return true;
            }
            sender.sendMessage(Component.text("用法: /haqi clear <玩家名|all>", NamedTextColor.YELLOW));
            return true;
        }
        String target = args[1];
        if (target.equalsIgnoreCase("all") || target.equalsIgnoreCase("@a")) {
            int players = 0;
            int items = 0;
            for (Player player : Bukkit.getOnlinePlayers()) {
                items += clearPlayer(player);
                players++;
            }
            sender.sendMessage(Component.text(
                    "已清除 " + players + " 名在线玩家的哈气（共 " + items + " 件）并全部退回基础解锁。",
                    NamedTextColor.GREEN));
            return true;
        }
        Player player = Bukkit.getPlayerExact(target);
        if (player == null) {
            sender.sendMessage(Component.text("玩家不在线: " + target, NamedTextColor.RED));
            return true;
        }
        int removed = clearPlayer(player);
        sender.sendMessage(Component.text(
                "已清除 " + player.getName() + " 的哈气物品（" + removed + "）并退回基础解锁。",
                NamedTextColor.GREEN));
        player.sendMessage(Component.text("[哈气] 管理员已清除你的哈气并退回基础解锁。", NamedTextColor.YELLOW));
        return true;
    }

    private static int clearPlayer(Player player) {
        int removed = HaqiItems.clearHaqiItems(player);
        PlayerData.setTier(player, HaqiTier.BASIC);
        return removed;
    }

    private boolean status(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("必须由玩家执行。", NamedTextColor.RED));
            return true;
        }
        float loudness = VoiceManager.get().getLoudness(player.getUniqueId());
        HaqiTier unlocked = PlayerData.getUnlockedTier(player);
        HaqiTier held = HaqiItems.heldHaqiTier(player);
        sender.sendMessage(Component.text("=== 哈气状态 ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("响度: " + String.format("%.0f%% (%.3f)", loudness * 100.0F, loudness)
                + "  阈值 " + String.format("%.2f", config.haqiVolumeThreshold), NamedTextColor.WHITE));
        sender.sendMessage(Component.text("已解锁: " + unlocked.id(), NamedTextColor.WHITE));
        sender.sendMessage(Component.text("手持哈气: " + (held == null ? "无" : held.id()), NamedTextColor.WHITE));
        sender.sendMessage(Component.text("需要手持物品: " + config.requireHaqiItem, NamedTextColor.WHITE));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(List.of("top", "debug", "status"));
            if (sender.hasPermission("fhw.give")) {
                subs.add("give");
                subs.add("giveall");
            }
            if (sender.hasPermission("fhw.unlock")) {
                subs.add("unlock");
            }
            if (sender.hasPermission("fhw.clear")) {
                subs.add("clear");
            }
            return subs.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("debug")) {
            return Arrays.asList("on", "off").stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("clear") && sender.hasPermission("fhw.clear")) {
            List<String> names = new ArrayList<>();
            names.add("all");
            for (Player p : Bukkit.getOnlinePlayers()) {
                names.add(p.getName());
            }
            return names.stream()
                    .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("unlock") || args[0].equalsIgnoreCase("give"))) {
            List<String> ids = new ArrayList<>(List.of("all", "echo"));
            for (HaqiTier t : HaqiTier.values()) {
                ids.add(t.id());
            }
            return ids.stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
