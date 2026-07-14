package com.freshwater.haqiworld.command;

import com.freshwater.haqiworld.Config;
import com.freshwater.haqiworld.haqi.HaqiTier;
import com.freshwater.haqiworld.data.HaqiLeaderboard;
import com.freshwater.haqiworld.data.HaqiPlayerData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;

import java.util.List;

/**
 * {@code /haqi top} - shows the kill leaderboard.
 * {@code /haqi unlock <tier>} - operator helper to unlock a tier for testing.
 */
public final class HaqiCommand {

    private HaqiCommand() {
    }

    public static void register() {
        RegisterCommandsEvent.BUS.addListener(event -> build(event.getDispatcher()));
    }

    private static void build(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("haqi")
                .then(Commands.literal("top").executes(HaqiCommand::showLeaderboard))
                .then(Commands.literal("unlock")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("tier", StringArgumentType.word())
                                .executes(HaqiCommand::unlockTier))));
    }

    private static int showLeaderboard(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        List<HaqiLeaderboard.Entry> top = HaqiLeaderboard.get(source.getServer()).top(Config.leaderboardSize);
        if (top.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("fhw.leaderboard.empty"), false);
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("fhw.leaderboard.header"), false);
        int rank = 1;
        for (HaqiLeaderboard.Entry entry : top) {
            final int r = rank++;
            source.sendSuccess(() -> Component.translatable("fhw.leaderboard.entry",
                    r, entry.name(), entry.kills()), false);
        }
        return top.size();
    }

    private static int unlockTier(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String tierId = StringArgumentType.getString(ctx, "tier");
        HaqiTier tier = null;
        for (HaqiTier candidate : HaqiTier.values()) {
            if (candidate.id().equalsIgnoreCase(tierId)) {
                tier = candidate;
                break;
            }
        }
        if (tier == null) {
            source.sendFailure(Component.literal("Unknown haqi tier: " + tierId));
            return 0;
        }
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("Must be run by a player."));
            return 0;
        }
        HaqiPlayerData.unlock(player, tier);
        HaqiTier unlocked = tier;
        source.sendSuccess(() -> Component.translatable("fhw.haqi.unlocked",
                Component.translatable("fhw.tier." + unlocked.id())), false);
        return 1;
    }
}
