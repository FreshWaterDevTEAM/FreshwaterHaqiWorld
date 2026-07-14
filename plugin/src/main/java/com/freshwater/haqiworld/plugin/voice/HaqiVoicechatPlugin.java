package com.freshwater.haqiworld.plugin.voice;

import com.freshwater.haqiworld.plugin.PluginConfig;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class HaqiVoicechatPlugin implements VoicechatPlugin {
    private final PluginConfig config;
    private final Map<UUID, OpusDecoder> decoders = new ConcurrentHashMap<>();
    private final AtomicLong packetLogCounter = new AtomicLong();
    private volatile VoicechatApi api;

    public HaqiVoicechatPlugin(PluginConfig config) {
        this.config = config;
    }

    @Override
    public String getPluginId() {
        return "fhw";
    }

    @Override
    public void initialize(VoicechatApi api) {
        this.api = api;
        Bukkit.getLogger().info("[FHW] Voicechat API initialized.");
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, event -> {
            this.api = event.getVoicechat();
            Bukkit.getLogger().info("[FHW] Voicechat server started — mic packets drive haqi.");
        });
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophone);
    }

    private void onMicrophone(MicrophonePacketEvent event) {
        if (event.getSenderConnection() == null) {
            return;
        }
        UUID playerId = event.getSenderConnection().getPlayer().getUuid();
        byte[] opus = event.getPacket().getOpusEncodedData();

        // SVC often interleaves empty "end of speech" frames. Do NOT zero loudness here —
        // VoiceManager timeout handles silence. Clearing here made haqi almost never fire.
        if (opus == null || opus.length == 0) {
            return;
        }

        VoicechatApi localApi = this.api;
        if (localApi == null) {
            return;
        }

        OpusDecoder decoder = decoders.computeIfAbsent(playerId, id -> localApi.createDecoder());
        try {
            short[] pcm = decoder.decode(opus);
            float loudness = VoiceManager.computeLoudness(pcm, config.haqiReferenceLevel);

            // SVC only sends non-empty opus while the client thinks you are talking.
            // Boost so soft speech still clears the haqi threshold.
            if (config.voiceActivationBoost) {
                loudness = Math.max(loudness, (float) config.voiceActivationMinLoudness);
            }

            VoiceManager.get().updateLoudness(playerId, loudness);

            long n = packetLogCounter.incrementAndGet();
            if (n <= 8 || n % 100 == 0) {
                Bukkit.getLogger().info("[FHW] Mic packet ok loudness=" + String.format("%.3f", loudness)
                        + " opusBytes=" + opus.length + " player=" + playerId);
            }
        } catch (Exception ex) {
            Bukkit.getLogger().warning("[FHW] Opus decode failed: " + ex.getMessage());
            OpusDecoder bad = decoders.remove(playerId);
            if (bad != null) {
                try {
                    bad.close();
                } catch (Exception ignored) {
                }
            }
            // Still treat "client is sending voice" as haqi if decode fails.
            if (config.voiceActivationBoost) {
                VoiceManager.get().updateLoudness(playerId, (float) config.voiceActivationMinLoudness);
            }
        }
    }
}
