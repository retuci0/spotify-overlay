package me.retucio.spotifyoverlay.config;

import me.retucio.spotifyoverlay.SpotifyOverlay;
import se.michaelthelin.spotify.SpotifyApi;

import java.io.Serializable;


public class Config implements Serializable {

    public String accessToken = "";
    public String refreshToken = "";

    public int x = 758, y = 2;
    public int w = 200, h = 60;

    public boolean visible = true;

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
