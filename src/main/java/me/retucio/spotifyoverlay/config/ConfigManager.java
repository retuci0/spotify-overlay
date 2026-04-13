package me.retucio.spotifyoverlay.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.retucio.spotifyoverlay.hud.Hud;
import me.retucio.spotifyoverlay.hud.widgets.Overlay;
import me.retucio.spotifyoverlay.spotify.SpotifyManager;
import me.retucio.spotifyoverlay.SpotifyOverlay;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class ConfigManager {

    public static ConfigManager INSTANCE = new ConfigManager();

    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final File FILE = new File("spotifyoverlay.json");

    private Config config = null;
    private boolean loaded = false;

    public void save() {
        ensure();

        try (FileWriter writer = new FileWriter(FILE)) {
            GSON.toJson(config, writer);
            SpotifyOverlay.LOGGER.info("settings saved");
        } catch (IOException e) {
            SpotifyOverlay.LOGGER.error("couldn't save settings: ", e);
        }
    }

    public void load() {
        SpotifyOverlay.LOGGER.info("loading settings...");

        if (!FILE.exists()) {
            ensure();
            save();
            return;
        }

        try (FileReader reader = new FileReader(FILE)) {
            config = GSON.fromJson(reader, Config.class);
            SpotifyOverlay.LOGGER.info("settings loaded");
        } catch (IOException e) {
            SpotifyOverlay.LOGGER.error("couldn't load settings: ", e);
            ensure();
        }
    }

    public void apply() {
        if (config == null) {
            SpotifyOverlay.LOGGER.warn("no config to apply");
            return;
        }

        Overlay overlay = Hud.INSTANCE.getOverlay();
        overlay.setX(config.x);
        overlay.setY(config.y);
        overlay.setW(config.w);
        overlay.setH(config.h);
        overlay.setVisible(config.visible);

        Hud.INSTANCE.getVolumeSlider().setValue(config.volume);

        loaded = true;
        SpotifyOverlay.LOGGER.info("settings applied");
    }

    public void ensure() {
        if (config == null) config = new Config();
    }


    public Config getConfig() {
        return config;
    }

    public boolean loaded() {
        return loaded;
    }
}
