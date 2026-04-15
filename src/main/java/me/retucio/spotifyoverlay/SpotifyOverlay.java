package me.retucio.spotifyoverlay;

import com.mojang.blaze3d.platform.Window;
import me.retucio.spotifyoverlay.config.Config;
import me.retucio.spotifyoverlay.config.ConfigManager;
import me.retucio.spotifyoverlay.hud.Hud;
import me.retucio.spotifyoverlay.hud.screen.AuthScreen;
import me.retucio.spotifyoverlay.hud.screen.ControlPanelScreen;
import me.retucio.spotifyoverlay.spotify.SpotifyAuth;
import me.retucio.spotifyoverlay.spotify.SpotifyManager;
import me.retucio.spotifyoverlay.util.KeyBinds;
import net.fabricmc.api.ModInitializer;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class SpotifyOverlay implements ModInitializer {

	public static final SpotifyOverlay INSTANCE = new SpotifyOverlay();

	public static final Minecraft mc = Minecraft.getInstance();
	private ScheduledExecutorService scheduler = null;

	public static final String MOD_ID = "spotifyoverlay";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final String CLIENT_ID = "3d114c83ef2a41bfa3665688a4f6c4e3";
	public static final URI REDIRECT_URI = SpotifyHttpManager.makeUri("http://127.0.0.1:4161/callback");

	private long lastVolumeChangeTime = 0;
	private static final long VOLUME_CHANGE_COOLDOWN_MS = 150;

	@Override
	public void onInitialize() {
		ConfigManager.INSTANCE.load();

		String token = ConfigManager.getConfig().accessToken;
		if (token == null || token.isEmpty()) {
			LOGGER.warn("no token found. starting OAuth flow...");
			new Thread(() -> {
				try {
					AuthorizationCodeCredentials creds = SpotifyAuth.authorize().join();
					Config config = ConfigManager.getConfig();
					config.accessToken = creds.getAccessToken();
					config.refreshToken = creds.getRefreshToken();
					Config.getSpotifyApi().setAccessToken(config.accessToken);
					Config.getSpotifyApi().setRefreshToken(config.refreshToken);
					LOGGER.info("spotify connected successfully!");
					if (mc.screen instanceof AuthScreen screen) {
						screen.onClose();
					}

					// set volume to stored value
					SpotifyManager.INSTANCE.setVolume(ConfigManager.getConfig().volume);

					startPolling();
				} catch (Exception e) {
					LOGGER.error("failed to connect to Spotify", e);
				}
			}).start();
		} else {
			LOGGER.info("token loaded: {}", token);
			Config.getSpotifyApi().setAccessToken(token);
			Config.getSpotifyApi().setRefreshToken(ConfigManager.getConfig().refreshToken);

			startPolling();
		}

		mc.execute(() -> ConfigManager.INSTANCE.apply());
	}

	public void onTick() {

	}

	public void onShutdown() {
		LOGGER.info("shutting down");
		ConfigManager.INSTANCE.save();
		stopPolling();
	}

	private void startPolling() {
		if (scheduler != null && !scheduler.isShutdown()) {
			return;  // already polling
		}

		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(
				SpotifyManager.INSTANCE::updatePlaybackState,
				0, 1, TimeUnit.SECONDS
		);  // every second
	}

	public void stopPolling() {
		if (scheduler != null) {
			scheduler.shutdown();
		}
	}

	public void onKey(int key, int action) {
		Hud.INSTANCE.onKey(key, action);
		if (action == GLFW.GLFW_PRESS) handleKeybinds(key);
		handleVolumeKeybinds(key, action);
	}

	public void onClick(int button, int action) {
		Hud.INSTANCE.onClick(button, action);
		if (action == GLFW.GLFW_PRESS) handleKeybinds(button);
		handleVolumeKeybinds(button, action);
	}

	private void handleKeybinds(int input) {
		for (KeyMapping bind : KeyBinds.spotifyKeys) {
			if (bind.matches(new KeyEvent(input, 0, 0))) {
				if (bind == KeyBinds.OPEN_CONTROL_PANEL) {
					if (mc.screen == null) {
						mc.setScreen(ControlPanelScreen.INSTANCE);
					}
				} else if (bind == KeyBinds.PAUSE_OR_RESUME) {
					SpotifyManager.INSTANCE.pauseOrResume();
				} else if (bind == KeyBinds.PREV_TRACK) {
					SpotifyManager.INSTANCE.prevTrack();
				} else if (bind == KeyBinds.NEXT_TRACK) {
					SpotifyManager.INSTANCE.nextTrack();
				} else if (bind == KeyBinds.TOGGLE_SHUFFLE) {
					SpotifyManager.INSTANCE.toggleShuffle();
				}
			}
		}
	}

	private void handleVolumeKeybinds(int input, int action) {
		if (!Hud.INSTANCE.isSelected(null)) return;

		if (action != GLFW.GLFW_PRESS && action != GLFW.GLFW_REPEAT) {
			return;
		}

		long now = System.currentTimeMillis();
		if (now - lastVolumeChangeTime < VOLUME_CHANGE_COOLDOWN_MS) {
			return;
		}

		for (KeyMapping bind : KeyBinds.spotifyKeys) {
			if (bind.matches(new KeyEvent(input, 0, 0))) {
				Config config = ConfigManager.getConfig();
				int newVolume;

				if (bind == KeyBinds.VOLUME_UP && config.volume < 100) {
					lastVolumeChangeTime = now;
					SpotifyManager.INSTANCE.increaseVolume();
					newVolume = Math.min(100, config.volume + 1);
					config.volume = newVolume;
					Hud.INSTANCE.getVolumeSlider().setValue(newVolume);
					ConfigManager.INSTANCE.save();
					break;
				} else if (bind == KeyBinds.VOLUME_DOWN && config.volume > 0) {
					lastVolumeChangeTime = now;
					SpotifyManager.INSTANCE.decreaseVolume();
					newVolume = Math.max(0, config.volume - 1);
					config.volume = newVolume;
					Hud.INSTANCE.getVolumeSlider().setValue(newVolume);
					ConfigManager.INSTANCE.save();
					break;
				}
			}
		}
	}

	public void onRender(GuiGraphicsExtractor gui, DeltaTracker dt) {
		Window window = mc.getWindow();
		int mx = Mth.floor(mc.mouseHandler.getScaledXPos(window));
		int my = Mth.floor(mc.mouseHandler.getScaledYPos(window));
		Hud.INSTANCE.render(gui, mx, my, dt.getGameTimeDeltaTicks());
	}
}