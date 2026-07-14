package com.freshwater.haqiworld.plugin.voice;

import com.freshwater.haqiworld.plugin.PluginConfig;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VoiceManager {
    private static final VoiceManager INSTANCE = new VoiceManager();
    private static final long VOICE_TIMEOUT_MS = 700L;
    /** Absolute peak below this is treated as silence (SVC decoded levels are often tiny). */
    private static final double SILENCE_PEAK = 1.0e-6;

    private final Map<UUID, Float> loudness = new ConcurrentHashMap<>();
    private final Map<UUID, Long> updatedAt = new ConcurrentHashMap<>();
    private final Map<UUID, Float> debugOverride = new ConcurrentHashMap<>();
    /** Per-player recent peak ceiling for relative (AGC) loudness. */
    private final Map<UUID, Double> peakCeiling = new ConcurrentHashMap<>();
    private final Map<UUID, Long> peakCeilingAt = new ConcurrentHashMap<>();

    private VoiceManager() {
    }

    public static VoiceManager get() {
        return INSTANCE;
    }

    public void updateLoudness(UUID id, float value) {
        float previous = loudness.getOrDefault(id, 0.0F);
        float merged = Math.max(value, previous * 0.55F);
        loudness.put(id, merged);
        updatedAt.put(id, System.currentTimeMillis());
    }

    /**
     * Converts one PCM frame into 0..1 loudness using relative AGC so soft/loud
     * still differ even when absolute SVC sample values are tiny (~1e-5).
     */
    public float ingestPcm(UUID id, short[] pcm) {
        PeakStats stats = measure(pcm);
        if (stats.peak < SILENCE_PEAK) {
            return getLoudness(id);
        }

        long now = System.currentTimeMillis();
        double ceiling = peakCeiling.getOrDefault(id, stats.peak);
        Long last = peakCeilingAt.get(id);
        if (last != null) {
            double dt = Math.max(0.0, (now - last) / 1000.0);
            // ~1.2s half-life — shout raises ceiling, then it slowly forgets
            ceiling *= Math.exp(-dt * 0.55);
        }
        ceiling = Math.max(ceiling, stats.peak);
        // Keep a little headroom so "almost as loud as your recent max" ≈ full power
        double denom = Math.max(ceiling * 0.90, SILENCE_PEAK);
        peakCeiling.put(id, ceiling);
        peakCeilingAt.put(id, now);

        double relative = Math.min(1.0, stats.peak / denom);
        // Mild curve: mid volumes still readable
        float loudnessValue = (float) Math.pow(relative, 0.75);

        updateLoudness(id, loudnessValue);
        return loudnessValue;
    }

    public float getLoudness(UUID id) {
        Float debug = debugOverride.get(id);
        if (debug != null) {
            return debug;
        }
        Long at = updatedAt.get(id);
        if (at == null || System.currentTimeMillis() - at > VOICE_TIMEOUT_MS) {
            return 0.0F;
        }
        return loudness.getOrDefault(id, 0.0F);
    }

    public boolean isHaqiing(UUID id, PluginConfig config) {
        return getLoudness(id) >= (float) config.haqiVolumeThreshold;
    }

    public void setDebugOverride(UUID id, float value) {
        if (value <= 0.0F) {
            debugOverride.remove(id);
        } else {
            debugOverride.put(id, value);
        }
    }

    public void clear(UUID id) {
        loudness.remove(id);
        updatedAt.remove(id);
        debugOverride.remove(id);
        peakCeiling.remove(id);
        peakCeilingAt.remove(id);
    }

    public void clearAll() {
        loudness.clear();
        updatedAt.clear();
        debugOverride.clear();
        peakCeiling.clear();
        peakCeilingAt.clear();
    }

    public static PeakStats measure(short[] pcm) {
        if (pcm == null || pcm.length == 0) {
            return new PeakStats(0.0, 0.0);
        }
        double sum = 0.0;
        double peak = 0.0;
        for (short sample : pcm) {
            double n = sample / 32768.0;
            double a = Math.abs(n);
            if (a > peak) {
                peak = a;
            }
            sum += n * n;
        }
        double rms = Math.sqrt(sum / pcm.length);
        return new PeakStats(Math.max(rms, peak), peak);
    }

    /** @deprecated use {@link #ingestPcm(UUID, short[])} */
    public static float computeLoudness(short[] pcm, double referenceLevel, double gain) {
        PeakStats stats = measure(pcm);
        if (stats.peak < SILENCE_PEAK) {
            return 0.0F;
        }
        double ref = Math.max(0.0001, referenceLevel);
        double normalized = (stats.energy / ref) * Math.max(0.1, gain);
        return (float) Math.max(0.0, Math.min(1.0, normalized));
    }

    public record PeakStats(double energy, double peak) {
    }
}
