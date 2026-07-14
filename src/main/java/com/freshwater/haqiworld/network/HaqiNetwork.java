package com.freshwater.haqiworld.network;

import com.freshwater.haqiworld.FreshwaterHaqiWorld;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

/**
 * Network channel for FreshwaterHaqiWorld. Currently carries the debug haqi input packet
 * (client to server); declared optional so vanilla and non-modded clients can still
 * connect.
 */
public final class HaqiNetwork {
    private static final int PROTOCOL_VERSION = 1;
    private static SimpleChannel channel;

    private HaqiNetwork() {
    }

    public static void register() {
        channel = ChannelBuilder
                .named(Identifier.fromNamespaceAndPath(FreshwaterHaqiWorld.MODID, "main"))
                .networkProtocolVersion(PROTOCOL_VERSION)
                .optional()
                .simpleChannel();

        channel.messageBuilder(HaqiInputPacket.class)
                .direction(PacketFlow.SERVERBOUND)
                .encoder(HaqiInputPacket::encode)
                .decoder(HaqiInputPacket::decode)
                .consumerMainThread(HaqiInputPacket::handle)
                .add();
    }

    public static void sendToServer(HaqiInputPacket packet) {
        channel.send(packet, PacketDistributor.SERVER.noArg());
    }
}
