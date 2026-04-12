package me.retucio.spotifyoverlay.hud.widgets.elements;

import me.retucio.spotifyoverlay.hud.widgets.Button;
import me.retucio.spotifyoverlay.spotify.SpotifyManager;
import me.retucio.spotifyoverlay.util.DrawUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.awt.*;


public class PauseOrResumeButton extends Button {

    private final int BG_COLOR = new Color(120, 120, 120, 180).getRGB();

    public boolean paused = false;

    public PauseOrResumeButton() {
        super("pause or resume", "pause or resume playback", 540, 540, 50, 50);
    }

    @Override
    public void renderOnHudEditor(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        DrawUtil.drawCircle(gui, x, y, w / 2, BG_COLOR);
        if (paused) {
            DrawUtil.drawTriangle(gui, x - 5, x - 5, x + 6, y - 6, y + 6, y, -1);
        } else {
            gui.fill(x - 7, y - 10, x - 3, y + 10, -1);
            gui.fill(x + 7, y - 10, x + 3, y + 10, -1);
        }
        super.render(gui, mx, my, delta);
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        // don't render outside of control panel
    }

    @Override
    public void onClick(int mx, int my, int button, int action) {
        if (action == GLFW.GLFW_PRESS) {
            paused = !paused;
            if (!SpotifyManager.INSTANCE.pauseOrResume()) {
                paused = !paused;  // revert
            } else {
                paused = SpotifyManager.INSTANCE.isPlaying();
            }
        }
        super.onClick(mx, my, button, action);
    }

    @Override
    public void renderTooltip(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        if (!isHovered(mx, my)) return;
        gui.setTooltipForNextFrame(
                Component.nullToEmpty(
                        paused
                                ? "resume"
                                : "pause"
                ),
                mx, my
        );
    }

    @Override
    public boolean isHovered(int mx, int my) {
        int dx = mx - x;
        int dy = my - y;
        int r = w / 2;
        return dx * dx + dy * dy <= r * r;
    }

    @Override
    public int defaultX() {
        return mc.getWindow().getGuiScaledWidth() / 2;
    }

    @Override
    public int defaultY() {
        return mc.getWindow().getGuiScaledHeight() * 3 / 4;
    }

    @Override
    public int defaultW() {
        return 50;
    }

    @Override
    public int defaultH() {
        return 50;
    }
}
