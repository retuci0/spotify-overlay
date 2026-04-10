package me.retucio.spotifyoverlay.spotify;

import com.sun.net.httpserver.HttpServer;
import me.retucio.spotifyoverlay.SpotifyOverlay;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public class CallbackServer {

    private HttpServer server;
    private final CompletableFuture<String> AUTHCODE_FUTURE = new CompletableFuture<>();

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(4161), 0);
        server.createContext("/callback", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            SpotifyOverlay.LOGGER.info("callback received. full query: {}", query);

            if (query != null && query.contains("code=")) {
                String code = query.split("code=")[1].split("&")[0];
                String response = "authorization successful! you can close this window.";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                AUTHCODE_FUTURE.complete(code);
            } else if (query != null && query.contains("error=")) {
                String error = query.split("error=")[1].split("&")[0];
                SpotifyOverlay.LOGGER.error("spotify returned error: {}", error);
                String response = "authorization failed: " + error;
                exchange.sendResponseHeaders(400, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                AUTHCODE_FUTURE.completeExceptionally(new RuntimeException("spotify error: " + error));
            } else {
                SpotifyOverlay.LOGGER.error("no code or error in query. query was: {}", query);
                String response = "authorization failed: no code received.";
                exchange.sendResponseHeaders(400, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                AUTHCODE_FUTURE.completeExceptionally(new RuntimeException("no code received"));
            }
        });
        server.start();
        SpotifyOverlay.LOGGER.info("callback server started on {}", SpotifyOverlay.REDIRECT_URI);
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

    public CompletableFuture<String> getAuthorizationCodeFuture() {
        return AUTHCODE_FUTURE;
    }
}
