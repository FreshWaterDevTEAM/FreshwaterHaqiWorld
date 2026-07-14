package com.freshwater.haqiworld.network;

import com.freshwater.haqiworld.voice.VoiceManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

/**
 * Client to server packet used by the debug keybind to simulate haqi loudness without a
 * real microphone. A value of 0 clears the debug override (back to real voice input).
 */
public record HaqiInputPacket(float loudness) {

    public static void encode(HaqiInputPacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.loudness);
    }

    public static HaqiInputPacket decode(FriendlyByteBuf buf) {
        return new HaqiInputPacket(buf.readFloat());
    }

    public static void handle(HaqiInputPacket msg, CustomPayloadEvent.Context ctx) {
        ServerPlayer sender = ctx.getSender();
        if (sender == null) {
            return;
        }
        if (msg.loudness <= 0.0F) {
            VoiceManager.get().clearDebugOverride(sender.getUUID());
        } else {
            VoiceManager.get().setDebugOverride(sender.getUUID(), msg.loudness);
        }
    }
}
