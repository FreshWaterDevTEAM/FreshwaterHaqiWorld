package com.freshwater.haqiworld.voice;

import com.freshwater.haqiworld.FreshwaterHaqiWorld;
import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple Voice Chat plugin that listens to incoming microphone packets, decodes them to
 * PCM and feeds a normalized loudness into {@link VoiceManager}.
 *
 * <p>Discovered and instantiated by Simple Voice Chat through the {@link ForgeVoicechatPlugin}
 * annotation. All work here happens off the main server thread, so it never touches
 * Minecraft state directly - it only writes to the thread-safe {@link VoiceManager}.
 */
@ForgeVoicechatPlugin
public class HaqiVoicePlugin implements VoicechatPlugin {

    private final ConcurrentHashMap<UUID, OpusDecoder> decoders = new ConcurrentHashMap<>();
    private volatile VoicechatApi api;

    @Override
    public String getPluginId() {
        return FreshwaterHaqiWorld.MODID;
    }

    @Override
    public void initialize(VoicechatApi api) {
        this.api = api;
        FreshwaterHaqiWorld.LOGGER.info("Simple Voice Chat detected - haqi voice input enabled.");
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
    }

    private void onMicrophonePacket(MicrophonePacketEvent event) {
        if (api == null) {
            return;
        }
        VoicechatConnection sender = event.getSenderConnection();
        if (sender == null || sender.getPlayer() == null) {
            return;
        }
        UUID playerId = sender.getPlayer().getUuid();
        byte[] opus = event.getPacket().getOpusEncodedData();

        // An empty opus payload signals the end of a voice activation: reset the decoder
        // so the next utterance starts cleanly, and report silence.
        if (opus == null || opus.length == 0) {
            OpusDecoder decoder = decoders.get(playerId);
            if (decoder != null) {
                decoder.resetState();
            }
            VoiceManager.get().updateLoudness(playerId, 0.0F);
            return;
        }

        OpusDecoder decoder = decoders.computeIfAbsent(playerId, id -> api.createDecoder());
        short[] pcm;
        try {
            pcm = decoder.decode(opus);
        } catch (Exception e) {
            decoder.resetState();
            return;
        }

        float loudness = VoiceManager.computeLoudness(pcm);
        VoiceManager.get().updateLoudness(playerId, loudness);
    }
}
