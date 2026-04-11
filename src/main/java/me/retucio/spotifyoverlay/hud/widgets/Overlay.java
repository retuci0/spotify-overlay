package me.retucio.spotifyoverlay.hud.widgets;

import com.mojang.blaze3d.platform.NativeImage;
import me.retucio.spotifyoverlay.SpotifyOverlay;
import me.retucio.spotifyoverlay.config.ConfigManager;
import me.retucio.spotifyoverlay.hud.Widget;
import me.retucio.spotifyoverlay.spotify.Song;
import me.retucio.spotifyoverlay.spotify.SpotifyManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class Overlay extends Widget {

    private final int WHITE = Color.WHITE.getRGB();
    private final int BG_COLOR = new Color(100, 100, 100, 80).getRGB();

    public Overlay(int x, int y, int w, int h) {
        super("overlay", "overlay that shows the currently playing track on spotify", x, y, w, h);
    }

    @Override
    protected void onClick(int mx, int my, int button, int action) {
        if (action == GLFW.GLFW_PRESS) {
            dragging = true;
            dx = mx - x;
            dy = my - y;
        } else if (action == GLFW.GLFW_RELEASE) {
            dragging = false;
            ConfigManager.INSTANCE.getConfig().x = this.x;
            ConfigManager.INSTANCE.getConfig().y = this.y;
        }

        super.onClick(mx, my, button, action);
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        if (dragging) {
            x = mx - dx;
            y = my - dy;
        }

        Song current = SpotifyManager.INSTANCE.getCurrentSong();

        gui.fill(x, y, x + w, y + h, BG_COLOR);

        int cs = Math.min(h - 10, 48);
        int cx = x + 5;
        int cy = y + (h - cs) / 2;

        NativeImage cover = SpotifyManager.INSTANCE.getAlbumCover();
        if (cover == null) {
            gui.fill(cx, cy, cx + cs, cy + cs, Color.BLACK.getRGB());
        } else {
            gui.blit(RenderPipelines.GUI_TEXTURED,
                    Identifier.fromNamespaceAndPath(
                            SpotifyOverlay.MOD_ID,
                            "cover"
                    ), cx, cy, cs, cs, cs, cs, cs, cs
            );
        }

        int textX = cx + cs + 8;
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
            gui.text(mc.font, artistsText, textX, artistsY, WHITE, false);
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
