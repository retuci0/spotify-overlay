package me.retucio.spotifyoverlay.config;

import me.retucio.spotifyoverlay.SpotifyOverlay;
import se.michaelthelin.spotify.SpotifyApi;

import java.io.Serializable;


public class Config implements Serializable {

    public String accessToken = "";
    public String refreshToken = "";

    public int x = 0, y = 0;
    public float scale = 1.0f;

    public int volume = 80;

    public static SpotifyApi spotifyApi = null;

    public static SpotifyApi getSpotifyApi() {
        if (spotifyApi == null) {
            spotifyApi = new SpotifyApi.Builder()
                    .setClientId(SpotifyOverlay.CLIENT_ID)
                    .setRedirectUri(SpotifyOverlay.REDIRECT_URI)
                    .build();
        }
        return spotifyApi;
    }
}
