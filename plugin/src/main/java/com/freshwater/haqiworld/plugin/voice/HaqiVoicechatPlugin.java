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
import java.util.logging.Logger;

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
        Logger log = Bukkit.getLogger();
        log.info("[FHW] Voicechat API initialized for haqi microphone hook.");
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, event -> {
            this.api = event.getVoicechat();
            Bukkit.getLogger().info("[FHW] Voicechat server started — microphone packets will drive haqi.");
        });
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophone);
    }

    private void onMicrophone(MicrophonePacketEvent event) {
        if (event.getSenderConnection() == null) {
            return;
        }
        UUID playerId = event.getSenderConnection().getPlayer().getUuid();
        byte[] opus = event.getPacket().getOpusEncodedData();
        if (opus == null || opus.length == 0) {
            OpusDecoder old = decoders.remove(playerId);
            if (old != null) {
                try {
                    old.close();
                } catch (Exception ignored) {
                }
            }
            VoiceManager.get().updateLoudness(playerId, 0.0F);
            return;
        }
        VoicechatApi localApi = this.api;
        if (localApi == null) {
            return;
        }
        OpusDecoder decoder = decoders.computeIfAbsent(playerId, id -> localApi.createDecoder());
        try {
            short[] pcm = decoder.decode(opus);
            if (pcm == null || pcm.length == 0) {
                return;
            }
            float loudness = VoiceManager.computeLoudness(pcm, config.haqiReferenceLevel);
            VoiceManager.get().updateLoudness(playerId, loudness);
            long n = packetLogCounter.incrementAndGet();
            if (n <= 5 || n % 200 == 0) {
                Bukkit.getLogger().info("[FHW] Mic loudness sample=" + String.format("%.3f", loudness)
                        + " player=" + playerId);
            }
        } catch (Exception ex) {
            // Reset decoder on bad packet so the next one can recover.
            OpusDecoder bad = decoders.remove(playerId);
            if (bad != null) {
                try {
                    bad.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
