package com.freshwater.haqiworld.client;

import com.freshwater.haqiworld.Config;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import org.lwjgl.glfw.GLFW;

/**
 * Debug haqi keybind: tells the Paper plugin to simulate microphone loudness via
 * {@code /haqi debug on|off} (works Forge-client → Paper-server without custom payloads).
 */
public final class HaqiClient {
    private static final KeyMapping HAQI_KEY = new KeyMapping(
            "key.fhw.haqi", GLFW.GLFW_KEY_H, KeyMapping.Category.GAMEPLAY);
    private static final KeyMapping GIVE_KEY = new KeyMapping(
            "key.fhw.give", GLFW.GLFW_KEY_G, KeyMapping.Category.GAMEPLAY);

    private static boolean wasDown = false;

    private HaqiClient() {
    }

    public static void init(BusGroup modBusGroup) {
        RegisterKeyMappingsEvent.BUS.addListener(HaqiClient::onRegisterKeyMappings);
        TickEvent.ClientTickEvent.Post.BUS.addListener(HaqiClient::onClientTick);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(HAQI_KEY);
        event.register(GIVE_KEY);
    }

    private static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            return;
        }
        while (GIVE_KEY.consumeClick()) {
            mc.player.connection.sendCommand("haqi give all");
        }
        if (!Config.enableDebugKeybind) {
            return;
        }
        boolean down = HAQI_KEY.isDown();
        if (down && !wasDown) {
            mc.player.connection.sendCommand("haqi debug on");
        } else if (!down && wasDown) {
            mc.player.connection.sendCommand("haqi debug off");
        }
        wasDown = down;
    }
}
