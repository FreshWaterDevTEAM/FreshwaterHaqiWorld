package com.freshwater.haqiworld.client;

import com.freshwater.haqiworld.Config;
import com.freshwater.haqiworld.network.HaqiInputPacket;
import com.freshwater.haqiworld.network.HaqiNetwork;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import org.lwjgl.glfw.GLFW;

/**
 * Client-side setup: registers the debug haqi keybind (a microphone-free fallback for
 * testing) and, while it is held, streams a simulated loudness to the server.
 */
public final class HaqiClient {
    private static final KeyMapping HAQI_KEY = new KeyMapping(
            "key.fhw.haqi", GLFW.GLFW_KEY_H, KeyMapping.Category.GAMEPLAY);

    private static boolean wasDown = false;

    private HaqiClient() {
    }

    public static void init(BusGroup modBusGroup) {
        RegisterKeyMappingsEvent.BUS.addListener(HaqiClient::onRegisterKeyMappings);
        TickEvent.ClientTickEvent.Post.BUS.addListener(HaqiClient::onClientTick);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(HAQI_KEY);
    }

    private static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        if (!Config.enableDebugKeybind) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        boolean down = HAQI_KEY.isDown();
        if (down && !wasDown) {
            HaqiNetwork.sendToServer(new HaqiInputPacket((float) Config.debugKeybindLoudness));
        } else if (!down && wasDown) {
            HaqiNetwork.sendToServer(new HaqiInputPacket(0.0F));
        }
        wasDown = down;
    }
}
