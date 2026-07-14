package com.freshwater.haqiworld.plugin.voice;

import com.freshwater.haqiworld.plugin.PluginConfig;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VoiceManager {
    private static final VoiceManager INSTANCE = new VoiceManager();
    private static final long VOICE_TIMEOUT_MS = 300L;

    private final Map<UUID, Float> loudness = new ConcurrentHashMap<>();
    private final Map<UUID, Long> updatedAt = new ConcurrentHashMap<>();
    private final Map<UUID, Float> debugOverride = new ConcurrentHashMap<>();

    private VoiceManager() {
    }

    public static VoiceManager get() {
        return INSTANCE;
    }

    public void updateLoudness(UUID id, float value) {
        loudness.put(id, value);
        updatedAt.put(id, System.currentTimeMillis());
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
    }

    public void clearAll() {
        loudness.clear();
        updatedAt.clear();
        debugOverride.clear();
    }

    public static float computeLoudness(short[] pcm, double referenceLevel) {
        if (pcm == null || pcm.length == 0) {
            return 0.0F;
        }
        double sum = 0.0;
        for (short sample : pcm) {
            double n = sample / 32768.0;
            sum += n * n;
        }
        double rms = Math.sqrt(sum / pcm.length);
        double ref = Math.max(0.0001, referenceLevel);
        return (float) Math.max(0.0, Math.min(1.0, rms / ref));
    }
}
