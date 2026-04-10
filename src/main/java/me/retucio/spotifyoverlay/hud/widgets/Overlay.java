package me.retucio.spotifyoverlay.hud.widgets;

import me.retucio.spotifyoverlay.hud.Widget;
import me.retucio.spotifyoverlay.spotify.Song;
import me.retucio.spotifyoverlay.spotify.SpotifyManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.FormattedText;

import java.awt.*;

public class Overlay extends Widget {

    private final int WHITE = Color.WHITE.getRGB();
    private final int GRAY = Color.GRAY.getRGB();
    private final int BG_COLOR = new Color(100, 100, 100, 80).getRGB();

    public Overlay(int x, int y, int w, int h) {
        super("overlay", "overlay that shows the currently playing track on spotify", x, y, w, h);
    }

    @Override
    protected void onHover(int mx, int my) {

    }

    @Override
    protected void onClick(int mx, int my, int button, int action) {

    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        Song current = SpotifyManager.INSTANCE.getCurrentSong();

        gui.fill(x, y, x + w, y + h, BG_COLOR);

        int coverSize = Math.min(h - 10, 48);
        int coverX = x + 5;
        int coverY = y + (h - coverSize) / 2;
        gui.fill(coverX, coverY, coverX + coverSize, coverY + coverSize, 0xFF000000);

        int textX = coverX + coverSize + 8;
        int textWidth = w - (textX - x) - 8;

        String title = current.isEmpty() ? "no song playing :(" : current.name();
        int titleWidth = mc.font.width(title);
        if (titleWidth > textWidth) {
            title = mc.font.substrByWidth(FormattedText.of(title), textWidth - 8).getString() + "...";
        }
        int titleY = y + (h / 3) - mc.font.lineHeight / 2;
        gui.text(mc.font, title, textX, titleY, WHITE, true);

        String artistsText = current.isEmpty() ? "" : String.join(", ", current.artists());
        if (!artistsText.isEmpty()) {
            int artistsY = titleY + mc.font.lineHeight + 2;
            gui.text(mc.font, artistsText, textX, artistsY, GRAY, false);
        }

        int barWidth = w - 20 - 48;
        int barHeight = 4;
        int barX = x + 10 + 48;
        int barY = y + h - barHeight - 12;

        gui.fill(barX, barY, barX + barWidth, barY + barHeight, BG_COLOR);

        if (!current.isEmpty() && current.duration() > 0) {
            int progress = SpotifyManager.INSTANCE.getCurrentProgress();
            if (progress > 0) {
                int filledWidth = (int) ((long) barWidth * progress / current.duration());
                gui.fill(barX, barY, barX + filledWidth, barY + barHeight, WHITE);
            }
        }
    }
}
