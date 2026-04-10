package me.retucio.spotifyoverlay.spotify;

import me.retucio.spotifyoverlay.SpotifyOverlay;
import me.retucio.spotifyoverlay.config.Config;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

import java.awt.*;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class SpotifyAuth {

    private static String codeVerifier;

    public static CompletableFuture<AuthorizationCodeCredentials> authorize() {
        CompletableFuture<AuthorizationCodeCredentials> future = new CompletableFuture<>();

        try {
            codeVerifier = PKCEUtil.generateCodeVerifier();
            String codeChallenge = PKCEUtil.generateCodeChallenge(codeVerifier);

            URI authUri = Config.getSpotifyApi()
                    .authorizationCodePKCEUri(codeChallenge)
                    .scope("user-read-currently-playing user-read-playback-state")
                    .build()
                    .execute();

            // open browser, if supported
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(authUri);
            } else {
                SpotifyOverlay.LOGGER.info(authUri.toASCIIString());
            }

            CallbackServer server = new CallbackServer();
            server.start();

            server.getAuthorizationCodeFuture().thenAccept(authCode -> {
                server.stop();
                try {
                    AuthorizationCodeCredentials credentials = Config.getSpotifyApi()
                            .authorizationCodePKCE(authCode, codeVerifier)
                            .build()
                            .execute();

                    Config.getSpotifyApi().setAccessToken(credentials.getAccessToken());
                    Config.getSpotifyApi().setRefreshToken(credentials.getRefreshToken());
                    future.complete(credentials);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }).exceptionally(ex -> {
                server.stop();
                future.completeExceptionally(ex);
                return null;
            });

        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }
}