package me.retucio.spotifyoverlay.spotify;

import com.google.gson.*;
import com.mojang.blaze3d.platform.NativeImage;
import me.retucio.spotifyoverlay.SpotifyOverlay;
import me.retucio.spotifyoverlay.config.Config;
import me.retucio.spotifyoverlay.config.ConfigManager;
import me.retucio.spotifyoverlay.util.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.Identifier;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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


    /* update methods */

    public void updatePlaybackState() {
        Config config = getConfig();
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
                    updatePlaybackState();  // retry after refresh
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
                if (currentCover != null) {
                    currentCover.close();
                    currentCover = null;
                }
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

            int resCode = conn.getResponseCode();

            if (resCode != 200) {
                SpotifyOverlay.LOGGER.error("failed to fetch cover: HTTP {}", resCode);
            }

            BufferedImage bufferedImage = ImageIO.read(conn.getInputStream());
            if (bufferedImage == null) {
                SpotifyOverlay.LOGGER.error("couldn't decode the image");
                return;
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

            // call from main thread
            mc.execute(() -> {
                if (currentCover != null) currentCover.close();
                currentCover = nativeImage;

                Identifier id = Identifier.fromNamespaceAndPath(SpotifyOverlay.MOD_ID, "albumcover");
                AbstractTexture oldTexture = mc.getTextureManager().getTexture(id);
                if (oldTexture != null) oldTexture.close();

                mc.getTextureManager().register(id, new DynamicTexture(() -> "albumcover", currentCover));
            });
        } catch (Exception e) {
            SpotifyOverlay.LOGGER.error("couldn't fetch album cover", e);
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
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


    /* actions */

    public boolean pauseOrResume() {
        if (isPlaying()) {
            return pause();
        } else {
            return resume();
        }
    }

    public boolean pause() {
        try {
            HttpURLConnection conn = getConnection("https://api.spotify.com/v1/me/player/pause", "PUT");
            writeEmptyBody(conn);

            int resCode = conn.getResponseCode();
            if (resCode == 204 || resCode == 200) {
                ChatUtil.info("playback paused.");
                return true;
            } else if (resCode == 404) {
                ChatUtil.error("no device is active!");
            } else {
                SpotifyOverlay.LOGGER.error("couldn't pause playback: HTTP {}", resCode);
            }
        } catch (Exception e) {
            SpotifyOverlay.LOGGER.error("couldn't pause playback", e);
        }

        return false;
    }

    public boolean resume() {
        try {
            HttpURLConnection conn = getConnection("https://api.spotify.com/v1/me/player/play", "PUT");
            writeEmptyBody(conn);

            int resCode = conn.getResponseCode();
            if (resCode == 204 || resCode == 200) {
                ChatUtil.info("playback resumed.");
                return true;
            } else if (resCode == 404) {
                ChatUtil.error("no device is active!");
            } else {
                SpotifyOverlay.LOGGER.error("couldn't resume playback: HTTP {}", resCode);
            }
        } catch (Exception e) {
            SpotifyOverlay.LOGGER.error("couldn't resume playback", e);
        }

        return false;
    }

    // todo: impl



    public void prevTrack() {
        try {
            HttpURLConnection conn = getConnection("https://api.spotify.com/v1/me/player/previous", "POST");
            writeEmptyBody(conn);

            int resCode = conn.getResponseCode();
            if (resCode == 204 || resCode == 200) {
                ChatUtil.info("skipped to previous track.");
            } else {
                SpotifyOverlay.LOGGER.error("couldn't skip to previous track: HTTP {}", resCode);
            }
        } catch (Exception e) {
            SpotifyOverlay.LOGGER.error("couldn't skip to previous track", e);
        }
    }

    public void nextTrack() {
        try {
            HttpURLConnection conn = getConnection("https://api.spotify.com/v1/me/player/next", "POST");
            writeEmptyBody(conn);

            int resCode = conn.getResponseCode();
            if (resCode == 204 || resCode == 200) {
                ChatUtil.info("skipped to next track.");
            } else {
                SpotifyOverlay.LOGGER.error("couldn't skip to next track: HTTP {}", resCode);
            }
        } catch (Exception e) {
            SpotifyOverlay.LOGGER.error("couldn't skip to next track", e);
        }
    }

    public void setVolume(int percent) {
        if (percent < 0 || percent > 100) {
            SpotifyOverlay.LOGGER.error("illegal volume percentage: {}%", percent);
            return;
        }

        try {
            HttpURLConnection conn = getConnection("https://api.spotify.com/v1/me/player/volume?volume_percent=" + percent, "PUT");
            writeEmptyBody(conn);

            int resCode = conn.getResponseCode();
            if (resCode == 204 || resCode == 200) {
                SpotifyOverlay.LOGGER.info("set volume to {}%", percent);
            } else {
                SpotifyOverlay.LOGGER.error("couldn't change volume: HTTP {}", resCode);
            }
        } catch (Exception e) {
            SpotifyOverlay.LOGGER.error("couldn't change volume", e);
        }
    }

    public void seekTo(int position) {
        try {
            HttpURLConnection conn = getConnection("https://api.spotify.com/v1/me/player/seek?position_ms=" + position, "PUT");
            writeEmptyBody(conn);

            int resCode = conn.getResponseCode();
            if (resCode == 204 || resCode == 200) {
                SpotifyOverlay.LOGGER.info("skipped to {} ms", position);
            } else {
                SpotifyOverlay.LOGGER.error("couldn't skip to {} ms: HTTP {}", position, resCode);
            }
        } catch (Exception e) {
            SpotifyOverlay.LOGGER.error("couldn't skip to {} ms", position, e);
        }
    }

    public void toggleShuffle(boolean shuffle) {
        try {
            HttpURLConnection conn = getConnection("https://api.spotify.com/v1/me/player/shuffle?state=" + shuffle, "PUT");
            writeEmptyBody(conn);

            int resCode = conn.getResponseCode();
            if (resCode == 204 || resCode == 200) {
                SpotifyOverlay.LOGGER.info("toggled playback shuffle {}", shuffle ? "on" : "off");
            } else {
                SpotifyOverlay.LOGGER.info("couldn't toggle playback shuffle: HTTP {}", resCode);
            }
        } catch (Exception e) {
            SpotifyOverlay.LOGGER.info("couldn't toggle playback shuffle", e);
        }
    }

    public void toggleLoop(boolean loop) {
        try {
            HttpURLConnection conn = getConnection("https://api.spotify.com/v1/me/player/repeat?state=" + (loop ? "track" : "false"), "PUT");
            writeEmptyBody(conn);

            int resCode = conn.getResponseCode();
            if (resCode == 204 || resCode == 200) {
                SpotifyOverlay.LOGGER.info("toggled playback loop {}", loop ? "on" : "off");
            } else {
                SpotifyOverlay.LOGGER.info("couldn't toggle playback loop: HTTP {}", resCode);
            }
        } catch (Exception e) {
            SpotifyOverlay.LOGGER.info("couldn't toggle playback loop", e);
        }
    }


    /* getters */

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

    public Config getConfig() {
        return ConfigManager.INSTANCE.getConfig();
    }

    @SuppressWarnings("deprecation")
    public HttpURLConnection getConnection(String url, String method) throws IOException {
        URL url1 = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Authorization", "Bearer " + getConfig().accessToken);
        conn.setDoOutput(true);
        return conn;
    }

    public void writeEmptyBody(HttpURLConnection conn) throws IOException {
        try (OutputStream os = conn.getOutputStream()) {
            os.write("{}".getBytes(StandardCharsets.UTF_8));
        }
    }
}