package com.freshwater.haqiworld.plugin.pack;

import com.freshwater.haqiworld.plugin.PluginConfig;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class ResourcePackService implements Listener {
    private final JavaPlugin plugin;
    private final PluginConfig config;
    private Path packFile;
    private byte[] sha1;
    private HttpServer httpServer;
    private String servedUrl;

    public ResourcePackService(JavaPlugin plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void start() {
        if (!config.resourcePackEnabled) {
            return;
        }
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            packFile = plugin.getDataFolder().toPath().resolve("fhw-resourcepack.zip");
            try (InputStream in = plugin.getResource("pack.zip")) {
                if (in == null) {
                    plugin.getLogger().warning("Embedded pack.zip missing — resource pack push disabled.");
                    return;
                }
                Files.copy(in, packFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            sha1 = sha1(packFile);

            String configuredUrl = config.resourcePackUrl == null ? "" : config.resourcePackUrl.trim();
            if (!configuredUrl.isEmpty()) {
                servedUrl = configuredUrl;
                plugin.getLogger().info("Using configured resource pack URL: " + servedUrl);
                return;
            }

            String host = config.resourcePackHost == null || config.resourcePackHost.isBlank()
                    ? "127.0.0.1" : config.resourcePackHost.trim();
            httpServer = HttpServer.create(new InetSocketAddress(config.resourcePackPort), 0);
            httpServer.createContext("/pack.zip", exchange -> {
                byte[] data = Files.readAllBytes(packFile);
                exchange.getResponseHeaders().add("Content-Type", "application/zip");
                exchange.sendResponseHeaders(200, data.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(data);
                }
            });
            httpServer.start();
            servedUrl = "http://" + host + ":" + config.resourcePackPort + "/pack.zip";
            plugin.getLogger().info("Hosting resource pack at " + servedUrl
                    + " (set resource-pack.host / resource-pack.url for public servers)");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to prepare resource pack: " + e.getMessage());
        }
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!config.resourcePackEnabled || servedUrl == null || sha1 == null) {
            return;
        }
        event.getPlayer().setResourcePack(servedUrl, sha1, config.resourcePackForce);
    }

    private static byte[] sha1(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.update(Files.readAllBytes(file));
        return digest.digest();
    }
}
