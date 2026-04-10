package me.retucio.spotifyoverlay.spotify;

import com.google.gson.*;
import me.retucio.spotifyoverlay.SpotifyOverlay;
import me.retucio.spotifyoverlay.config.Config;
import me.retucio.spotifyoverlay.config.ConfigManager;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class SpotifyManager {

    public static SpotifyManager INSTANCE = new SpotifyManager();

    private Song currentSong = Song.empty();
    private int currentProgress = -1;
    private boolean isPlaying = false;

    public void updatePlaybackState() {
        Config config = ConfigManager.INSTANCE.getConfig();
        if (config.accessToken == null || config.accessToken.isEmpty()) {
            return;
        }

        try {
            URL url = new URL("https://api.spotify.com/v1/me/player");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + config.accessToken);

            int resCode = conn.getResponseCode();

            // token expired
            if (resCode == 401) {
                SpotifyOverlay.LOGGER.info("Token expired, attempting refresh...");
                if (refreshToken()) {
                    updatePlaybackState(); // retry after refresh
                }
                return;
            }

            // rate limit
            if (resCode == 429) {
                String retryAfter = conn.getHeaderField("Retry-After");
                int waitSeconds = (retryAfter != null) ? Integer.parseInt(retryAfter) : 2;
                SpotifyOverlay.LOGGER.warn("Rate limited, skipping this poll. Retry after {}s.", waitSeconds);
                return;
            }

            // no song playing
            if (resCode == 204) {
                currentSong = Song.empty();
                currentProgress = -1;
                isPlaying = false;
                return;
            }

            // song playing
            if (resCode == 200) {
                try (Scanner scanner = new Scanner(conn.getInputStream())) {
                    String resBody = scanner.useDelimiter("\\A").next();
                    JsonObject content = JsonParser.parseString(resBody).getAsJsonObject();

                    if (!content.has("item") || content.get("item").isJsonNull()) {
                        currentSong = Song.empty();
                        currentProgress = -1;
                        isPlaying = false;
                        return;
                    }

                    JsonObject item = content.getAsJsonObject("item");
                    String name = item.get("name").getAsString();

                    // parse artists
                    JsonArray artistsArray = item.get("artists").getAsJsonArray();
                    List<String> artists = new ArrayList<>();
                    for (JsonElement artistElement : artistsArray) {
                        JsonObject artistObj = artistElement.getAsJsonObject();
                        artists.add(artistObj.get("name").getAsString());
                    }

                    int duration = item.get("duration_ms").getAsInt();

                    int progress = content.get("progress_ms").getAsInt();
                    boolean playing = content.get("is_playing").getAsBoolean();

                    // update cache
                    currentSong = new Song(name, artists, duration, false);
                    currentProgress = progress;
                    isPlaying = playing;
                }
            } else {
                SpotifyOverlay.LOGGER.warn("unexpected response code: {}", resCode);
            }
        } catch (Exception e) {
            SpotifyOverlay.LOGGER.error("failed to update playback state", e);
        }
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    private boolean refreshToken() {
        try {
            AuthorizationCodeCredentials newCreds = Config.getSpotifyApi()
                    .authorizationCodePKCERefresh()
                    .build()
                    .execute();

            Config config = ConfigManager.INSTANCE.getConfig();
            config.accessToken = newCreds.getAccessToken();
            config.refreshToken = newCreds.getRefreshToken();
            ConfigManager.INSTANCE.save();

            Config.getSpotifyApi().setAccessToken(config.accessToken);
            Config.getSpotifyApi().setRefreshToken(config.refreshToken);

            SpotifyOverlay.LOGGER.info("token refreshed successfully");
            return true;
        } catch (Exception e) {
            SpotifyOverlay.LOGGER.error("failed to refresh token", e);
            return false;
        }
    }
}