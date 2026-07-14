package com.freshwater.haqiworld.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Persistent, world-saved tally of haqi kills per player, used for the multiplayer
 * leaderboard.
 */
public class HaqiLeaderboard extends SavedData {

    /** One leaderboard row. */
    public record Entry(UUID id, String name, int kills) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.CODEC.fieldOf("id").forGetter(Entry::id),
                Codec.STRING.fieldOf("name").forGetter(Entry::name),
                Codec.INT.fieldOf("kills").forGetter(Entry::kills)
        ).apply(instance, Entry::new));
    }

    private static final Codec<HaqiLeaderboard> CODEC = Entry.CODEC.listOf()
            .fieldOf("entries")
            .codec()
            .xmap(HaqiLeaderboard::fromEntries, HaqiLeaderboard::toEntries);

    public static final SavedDataType<HaqiLeaderboard> TYPE = new SavedDataType<>(
            "fhw_haqi_leaderboard", HaqiLeaderboard::new, CODEC, DataFixTypes.SAVED_DATA_SCOREBOARD);

    private final Map<UUID, Integer> kills = new HashMap<>();
    private final Map<UUID, String> names = new HashMap<>();

    public HaqiLeaderboard() {
    }

    private static HaqiLeaderboard fromEntries(List<Entry> entries) {
        HaqiLeaderboard board = new HaqiLeaderboard();
        for (Entry entry : entries) {
            board.kills.put(entry.id(), entry.kills());
            board.names.put(entry.id(), entry.name());
        }
        return board;
    }

    private List<Entry> toEntries() {
        List<Entry> entries = new ArrayList<>(kills.size());
        kills.forEach((id, count) -> entries.add(new Entry(id, names.getOrDefault(id, "?"), count)));
        return entries;
    }

    public void addKill(ServerPlayer player) {
        kills.merge(player.getUUID(), 1, Integer::sum);
        names.put(player.getUUID(), player.getName().getString());
        setDirty();
    }

    public int getKills(UUID player) {
        return kills.getOrDefault(player, 0);
    }

    /** Returns the top entries, highest kill count first. */
    public List<Entry> top(int limit) {
        return toEntries().stream()
                .sorted(Comparator.comparingInt(Entry::kills).reversed())
                .limit(limit)
                .toList();
    }

    public static HaqiLeaderboard get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }
}
