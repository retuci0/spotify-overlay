package me.retucio.spotifyoverlay.hud.widgets.elements;

import me.retucio.spotifyoverlay.hud.Hud;
import me.retucio.spotifyoverlay.hud.widgets.Button;
import me.retucio.spotifyoverlay.spotify.SpotifyManager;
import me.retucio.spotifyoverlay.util.DrawUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

public class NextButton extends Button {

    public NextButton() {
        super("next track", "skip to next track", 648, 360, 30, 30);
    }

    @Override
    public void renderOnHudEditor(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        gui.fill(x + w, y, x + w - 5, y + h, -1);
        DrawUtil.drawTriangle(gui, x + w - 5, x, x, y + h / 2, y, y + h, -1);
        super.render(gui, mx, my, delta);
    }

    @Override
    public int defaultX() {
        return ((mc.getWindow().getGuiScaledWidth()) * 3 / 5) - w / 2;
    }

    @Override
    public int defaultY() {
        return (mc.getWindow().getGuiScaledHeight() * 3 / 4) - w / 2;
    }

    @Override
    public int defaultW() {
        return 30;
    }

    @Override
    public int defaultH() {
        return 30;
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        // only render on control panel
    }

    @Override
    public void onClick(int mx, int my, int button, int action) {
        if (action == GLFW.GLFW_PRESS) {
            SpotifyManager.INSTANCE.nextTrack();
            Hud.INSTANCE.getPauseOrResumeButton().paused = false;
        }
        super.onClick(mx, my, button, action);
    }
}
