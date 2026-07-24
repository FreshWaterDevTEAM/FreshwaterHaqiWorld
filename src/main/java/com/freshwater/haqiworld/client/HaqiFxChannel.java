package com.freshwater.haqiworld.client;

import com.freshwater.haqiworld.FreshwaterHaqiWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.EventNetworkChannel;

/**
 * Receives Paper plugin messages on {@code fhw:fx} and spawns recolored haqi sonic-boom particles.
 */
public final class HaqiFxChannel {
    public static final Identifier CHANNEL_ID = Identifier.fromNamespaceAndPath(FreshwaterHaqiWorld.MODID, "fx");
    private static EventNetworkChannel channel;

    private HaqiFxChannel() {
    }

    public static void init() {
        channel = ChannelBuilder.named(CHANNEL_ID)
                .networkProtocolVersion(1)
                .optional()
                .eventNetworkChannel();
        channel.addListener(HaqiFxChannel::onPayload);
    }

    private static void onPayload(CustomPayloadEvent event) {
        FriendlyByteBuf buf = event.getPayload();
        if (buf == null) {
            return;
        }
        // Copy needed fields before the buffer may be released.
        final byte version;
        final double ox;
        final double oy;
        final double oz;
        final float dx;
        final float dy;
        final float dz;
        final float range;
        try {
            version = buf.readByte();
            if (version != 1) {
                event.getSource().setPacketHandled(true);
                return;
            }
            ox = buf.readDouble();
            oy = buf.readDouble();
            oz = buf.readDouble();
            dx = buf.readFloat();
            dy = buf.readFloat();
            dz = buf.readFloat();
            range = buf.readFloat();
        } catch (Exception e) {
            FreshwaterHaqiWorld.LOGGER.warn("Bad fhw:fx payload", e);
            event.getSource().setPacketHandled(true);
            return;
        }
        event.getSource().enqueueWork(() -> spawnBoom(ox, oy, oz, dx, dy, dz, range));
        event.getSource().setPacketHandled(true);
    }

    private static void spawnBoom(double ox, double oy, double oz,
                                  float dx, float dy, float dz, float range) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || ModParticles.HAQI_SONIC_BOOM.get() == null) {
            return;
        }
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0E-6 || range < 1.0F) {
            return;
        }
        double ndx = dx / len;
        double ndy = dy / len;
        double ndz = dz / len;
        int steps = Math.max(1, (int) Math.floor(range));
        for (int i = 1; i <= steps; i++) {
            level.addParticle(ModParticles.HAQI_SONIC_BOOM.get(),
                    ox + ndx * i, oy + ndy * i, oz + ndz * i,
                    0.0, 0.0, 0.0);
        }
    }
}
