package me.retucio.spotifyoverlay;

import com.mojang.blaze3d.platform.Window;
import me.retucio.spotifyoverlay.config.Config;
import me.retucio.spotifyoverlay.config.ConfigManager;
import me.retucio.spotifyoverlay.hud.Hud;
import me.retucio.spotifyoverlay.hud.screen.AuthScreen;
import me.retucio.spotifyoverlay.hud.screen.HudEditorScreen;
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

	@Override
	public void onInitialize() {
		ConfigManager.INSTANCE.load();

		String token = ConfigManager.INSTANCE.getConfig().accessToken;
		if (token == null || token.isEmpty()) {
			LOGGER.warn("no token found. starting OAuth flow...");
			new Thread(() -> {
				try {
					AuthorizationCodeCredentials creds = SpotifyAuth.authorize().join();
					Config config = ConfigManager.INSTANCE.getConfig();
					config.accessToken = creds.getAccessToken();
					config.refreshToken = creds.getRefreshToken();
					Config.getSpotifyApi().setAccessToken(config.accessToken);
					Config.getSpotifyApi().setRefreshToken(config.refreshToken);
					LOGGER.info("spotify connected successfully!");
					if (mc.screen instanceof AuthScreen screen) {
						screen.onClose();
					}

					// set volume to stored value
					SpotifyManager.INSTANCE.setVolume(ConfigManager.INSTANCE.getConfig().volume);

					startPolling();
				} catch (Exception e) {
					LOGGER.error("failed to connect to Spotify", e);
				}
			}).start();
		} else {
			LOGGER.info("token loaded: {}", token);
			Config.getSpotifyApi().setAccessToken(token);
			Config.getSpotifyApi().setRefreshToken(ConfigManager.INSTANCE.getConfig().refreshToken);

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
	}

	public void onClick(int button, int action) {
		if (action == GLFW.GLFW_PRESS) handleKeybinds(button);
		Hud.INSTANCE.onClick(button, action);
	}

	private void handleKeybinds(int input) {
		for (KeyMapping bind : KeyBinds.spotifyKeys) {
			if (bind.matches(new KeyEvent(input, 0, 0))) {
				if (bind == KeyBinds.OPEN_CONTROL_PANEL) {
					if (mc.screen == null) {
						mc.setScreen(HudEditorScreen.INSTANCE);
					}
				} else if (bind == KeyBinds.PAUSE_OR_RESUME) {
					SpotifyManager.INSTANCE.pauseOrResume();
				} else if (bind == KeyBinds.PREV_TRACK) {
					SpotifyManager.INSTANCE.prevTrack();
				} else if (bind == KeyBinds.NEXT_TRACK) {
					SpotifyManager.INSTANCE.nextTrack();
//				} else if (bind == KeyBinds.FORWARD_5S) {
//					SpotifyManager.INSTANCE.forward(5000);
//				} else if (bind == KeyBinds.BACKWARD_5S) {
//					SpotifyManager.INSTANCE.backward(5000);
//				} else if (bind == KeyBinds.VOLUME_UP) {
//					SpotifyManager.INSTANCE.increaseVolume();
//				} else if (bind == KeyBinds.VOLUME_DOWN) {
//					SpotifyManager.INSTANCE.decreaseVolume();
//				} else if (bind == KeyBinds.TOGGLE_SHUFFLE) {
//					SpotifyManager.INSTANCE.toggleShuffle();
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