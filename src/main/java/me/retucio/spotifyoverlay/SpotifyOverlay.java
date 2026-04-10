package me.retucio.spotifyoverlay;

import com.mojang.blaze3d.platform.Window;
import me.retucio.spotifyoverlay.config.Config;
import me.retucio.spotifyoverlay.config.ConfigManager;
import me.retucio.spotifyoverlay.hud.Hud;
import me.retucio.spotifyoverlay.spotify.SpotifyAuth;
import me.retucio.spotifyoverlay.spotify.SpotifyManager;
import net.fabricmc.api.ModInitializer;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
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
					ConfigManager.INSTANCE.save();
					Config.getSpotifyApi().setAccessToken(config.accessToken);
					Config.getSpotifyApi().setRefreshToken(config.refreshToken);
					LOGGER.info("spotify connected successfully!");

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
	}

	public void onShutdown() {
		LOGGER.info("shutting down");
		stopPolling();
	}

	private void startPolling() {
		if (scheduler != null && !scheduler.isShutdown()) {
			return;  // already polling
		}

		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(
				SpotifyManager.INSTANCE::updatePlaybackState,
				0, 2, TimeUnit.SECONDS
		);  // every 2 seconds

		LOGGER.info("Started Spotify polling every 2 seconds");
	}

	public void stopPolling() {
		if (scheduler != null) {
			scheduler.shutdown();
		}
	}



	public void onKey(int key, int action) {

	}

	public void onClick(int button, int action) {
		Hud.INSTANCE.onClick(button, action);
	}

	public void onRender(GuiGraphicsExtractor gui, DeltaTracker dt) {
		Window window = mc.getWindow();
		int mx = Mth.floor(mc.mouseHandler.getScaledXPos(window));
		int my = Mth.floor(mc.mouseHandler.getScaledYPos(window));
		Hud.INSTANCE.render(gui, mx, my, dt.getGameTimeDeltaTicks());
	}
}