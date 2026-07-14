package com.freshwater.haqiworld.plugin.voice;

import com.freshwater.haqiworld.plugin.PluginConfig;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HaqiVoicechatPlugin implements VoicechatPlugin {
    private final PluginConfig config;
    private final Map<UUID, OpusDecoder> decoders = new ConcurrentHashMap<>();
    private VoicechatApi api;

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
    }

    @Override
    public void registerEvents(EventRegistration registration) {
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
                old.close();
            }
            VoiceManager.get().updateLoudness(playerId, 0.0F);
            return;
        }
        if (api == null) {
            return;
        }
        OpusDecoder decoder = decoders.computeIfAbsent(playerId, id -> api.createDecoder());
        try {
            short[] pcm = decoder.decode(opus);
            float loudness = VoiceManager.computeLoudness(pcm, config.haqiReferenceLevel);
            VoiceManager.get().updateLoudness(playerId, loudness);
        } catch (Exception ignored) {
            // Drop bad packets; next good packet will recover.
        }
    }
}
