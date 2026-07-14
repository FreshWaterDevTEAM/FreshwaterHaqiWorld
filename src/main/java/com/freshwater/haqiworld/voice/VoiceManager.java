package com.freshwater.haqiworld.voice;

import com.freshwater.haqiworld.Config;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores the most recent microphone loudness for each player.
 *
 * <p>Microphone packets arrive on a Simple Voice Chat networking thread, while the haqi
 * attack logic runs on the main server thread. This class is the thread-safe hand-off
 * point: the voice plugin writes loudness here, and the server tick handler reads it.
 */
public final class VoiceManager {
    private static final VoiceManager INSTANCE = new VoiceManager();

    /** Loudness window: a reading older than this (ms) is treated as silence. */
    private static final long VOICE_TIMEOUT_MS = 300L;

    private final ConcurrentHashMap<UUID, Float> loudness = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Long> lastUpdate = new ConcurrentHashMap<>();

    /** Optional debug override (set by the client keybind fallback when enabled). */
    private final ConcurrentHashMap<UUID, Float> debugOverride = new ConcurrentHashMap<>();

    private VoiceManager() {
    }

    public static VoiceManager get() {
        return INSTANCE;
    }

    /** Called from the voice thread with a freshly computed loudness (0..1). */
    public void updateLoudness(UUID player, float value) {
        loudness.put(player, clamp01(value));
        lastUpdate.put(player, System.currentTimeMillis());
    }

    /**
     * Current loudness for a player (0..1), or 0 if no recent voice. Honors the debug
     * override when present.
     */
    public float getLoudness(UUID player) {
        Float override = debugOverride.get(player);
        if (override != null) {
            return clamp01(override);
        }
        Long ts = lastUpdate.get(player);
        if (ts == null || System.currentTimeMillis() - ts > VOICE_TIMEOUT_MS) {
            return 0.0F;
        }
        return loudness.getOrDefault(player, 0.0F);
    }

    /** True when the player is haqi-ing loudly enough to fire (per config threshold). */
    public boolean isHaqiing(UUID player) {
        return getLoudness(player) >= (float) Config.haqiVolumeThreshold;
    }

    public void setDebugOverride(UUID player, float value) {
        debugOverride.put(player, clamp01(value));
    }

    public void clearDebugOverride(UUID player) {
        debugOverride.remove(player);
    }

    public void clear(UUID player) {
        loudness.remove(player);
        lastUpdate.remove(player);
        debugOverride.remove(player);
    }

    /**
     * Converts a block of 16-bit PCM samples into a normalized loudness (0..1) using RMS,
     * scaled by the configured reference level.
     */
    public static float computeLoudness(short[] pcm) {
        if (pcm == null || pcm.length == 0) {
            return 0.0F;
        }
        double sumSquares = 0.0D;
        for (short sample : pcm) {
            double normalized = sample / 32768.0D;
            sumSquares += normalized * normalized;
        }
        double rms = Math.sqrt(sumSquares / pcm.length);
        double reference = Math.max(0.0001D, Config.haqiReferenceLevel);
        return clamp01((float) (rms / reference));
    }

    private static float clamp01(float v) {
        if (v < 0.0F) return 0.0F;
        if (v > 1.0F) return 1.0F;
        return v;
    }
}
