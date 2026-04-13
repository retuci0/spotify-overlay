package me.retucio.spotifyoverlay.hud.widgets.sliders;

import me.retucio.spotifyoverlay.hud.widgets.Slider;
import me.retucio.spotifyoverlay.spotify.SpotifyManager;
import me.retucio.spotifyoverlay.util.DrawUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;


public class ProgressSlider extends Slider {

    public ProgressSlider() {
        super(
                "progress", "current track progress",
                200, 316, 560, 4,
                0, 0, 100, 1
        );
    }

    @Override
    protected int trackSize() { return w; }

    @Override
    protected float percent(int mx, int my)  { return (mx - x) / (float) w; }

    @Override
    protected void drawFilled(GuiGraphicsExtractor gui, int filled) {
        gui.fill(x, y, x + filled, y + h, -1);
    }

    @Override
    public void renderOnHudEditor(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        super.render(gui, mx, my, delta);

        String progress = getTimestamp(value);
        gui.text(mc.font, progress, x, y + 20, -1, true);

        String duration = getTimestamp(max);
        gui.text(mc.font, duration, x + w - mc.font.width(duration), y + 20, -1, true);

        double percent = (value - min) / (max - min);
        int filled = (int) (percent * w);
        DrawUtil.drawCircle(gui, x + filled, y + h / 2, 5, -1);

        if (isHovered(mx, my)) {
            gui.fill(mx, y, mx + 1, y + h, -1);
        }
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        value = SpotifyManager.INSTANCE.getCurrentProgress() / 1000.0f;
        max = SpotifyManager.INSTANCE.getCurrentSong().duration() / 1000.0f;

        // (draw on control panel only)
    }

    @Override
    public void renderTooltip(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        if (!isHovered(mx, my)) return;

        String progress = getTimestamp(value);
        String duration = getTimestamp(max);

        float previewValue = getValue(mx, my);
        String preview = getTimestamp(previewValue);

        gui.setComponentTooltipForNextFrame(mc.font,
                List.of(
                        Component.nullToEmpty(progress + " / " + duration),
                        Component.nullToEmpty("> " + preview)
                ),
                mx, my
        );
    }

    private String getTimestamp(float value) {
        int mins = (int) value / 60;
        int secs = (int) value % 60;  // haha secs
        return String.format("%d:%02d", mins, secs);
    }

    @Override public int defaultX() { return 200; }
    @Override public int defaultY() { return 316; }
    @Override public int defaultW() { return 560; }
    @Override public int defaultH() { return   4; }
}