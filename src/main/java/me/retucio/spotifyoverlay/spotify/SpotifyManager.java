package me.retucio.spotifyoverlay.spotify;

import com.google.gson.*;
import com.mojang.blaze3d.platform.NativeImage;
import me.retucio.spotifyoverlay.SpotifyOverlay;
import me.retucio.spotifyoverlay.config.Config;
import me.retucio.spotifyoverlay.config.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class SpotifyManager {

    public final static SpotifyManager INSTANCE = new SpotifyManager();
    private final Minecraft mc = Minecraft.getInstance();

    private Song currentSong = Song.empty();
    private int currentProgress = -1;
    private NativeImage currentCover = null;
    private boolean isPlaying = false;

    public void updatePlaybackState() {
        Config config = ConfigManager.INSTANCE.getConfig();
        if (config.accessToken == null || config.accessToken.isEmpty()) {
            return;
        }

        try {
            updateAlbumCover();

            URL url = new URL("https://api.spotify.com/v1/me/player");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + config.accessToken);

            int resCode = conn.getResponseCode();

            // token expired
            if (resCode == 401) {
                SpotifyOverlay.LOGGER.info("token expired, attempting refresh...");
                if (refreshToken()) {
                    updatePlaybackState(); // retry after refresh
                }
                return;
            }

            // rate limit
            if (resCode == 429) {
                String retryAfter = conn.getHeaderField("Retry-After");
                int waitSeconds = (retryAfter != null) ? Integer.parseInt(retryAfter) : 2;
                SpotifyOverlay.LOGGER.warn("rate limited, retrying after {}s", waitSeconds);
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

                    String albumCoverUrl = null;
                    if (item.has("album")) {
                        JsonObject album = item.getAsJsonObject("album");
                        if (album.has("images")) {
                            JsonArray images = album.getAsJsonArray("images");
                            if (!images.isEmpty()) {
                                JsonObject firstImage = images.get(0).getAsJsonObject();
                                albumCoverUrl = firstImage.get("url").getAsString();
                            }
                        }
                    }

                    int duration = item.get("duration_ms").getAsInt();

                    int progress = content.get("progress_ms").getAsInt();
                    boolean playing = content.get("is_playing").getAsBoolean();

                    // update cache
                    currentSong = new Song(name, artists, albumCoverUrl, duration, false);
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

    public void updateAlbumCover() {
        HttpURLConnection conn = null;
        try {
            if (currentSong.cover().isEmpty()) return;
            URL url = new URL(currentSong.cover());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                SpotifyOverlay.LOGGER.error("failed to fetch cover: HTTP {}", responseCode);
            }

            BufferedImage bufferedImage = ImageIO.read(conn.getInputStream());
            if (bufferedImage == null) {
                SpotifyOverlay.LOGGER.error("couldn't decode the image");
            }

            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            NativeImage nativeImage = new NativeImage(width, height, true);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int argb = bufferedImage.getRGB(x, y);
                    nativeImage.setPixel(x, y, argb);
                }
            }

            currentCover = nativeImage;
            mc.execute(() -> mc.getTextureManager().register(Identifier.fromNamespaceAndPath(SpotifyOverlay.MOD_ID, "cover"),
                    new DynamicTexture(() -> "cover", currentCover)));
        } catch (Exception e) {
            SpotifyOverlay.LOGGER.error("couldn't fetch album cover", e);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public NativeImage getAlbumCover() {
        return currentCover;
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