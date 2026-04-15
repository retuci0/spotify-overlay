package me.retucio.spotifyoverlay.hud.widgets.sliders;

import me.retucio.spotifyoverlay.config.ConfigManager;
import me.retucio.spotifyoverlay.hud.widgets.Slider;
import me.retucio.spotifyoverlay.spotify.SpotifyManager;
import me.retucio.spotifyoverlay.util.ChatUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;


public class VolumeSlider extends Slider {

    public VolumeSlider() {
        super("volume", "slider for volume", 0, 0, 20, 500, 80, 0, 100, 1);
    }

    @Override
    protected int trackSize() { return h; }

    @Override
    protected float percent(int mx, int my) { return (y + h - my) / (float) h; }

    @Override
    protected void drawFilled(GuiGraphicsExtractor gui, int filled) {
        gui.fill(x, y + h - filled, x + w, y + h, -1);
    }

    @Override
    public void renderOnControlPanel(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        String text = (int) value + "%";
        gui.text(mc.font, text, x + w / 2 - mc.font.width(text) / 2, y - 2 * mc.font.lineHeight, -1, true);
        super.render(gui, mx, my, delta);
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        // draw on control panel only
    }

    @Override
    public void onClick(int mx, int my, int button, int action) {
        if (action == GLFW.GLFW_RELEASE && button == 0) {
            SpotifyManager.INSTANCE.setVolume((int) value);
            ChatUtil.info("changed volume to " + (int) value + "%.");
        }
        super.onClick(mx, my, button, action);
    }

    @Override
    protected void onChange() {
        ConfigManager.getConfig().volume = (int) value;
    }

    @Override public int defaultX() { return mc.getWindow().getGuiScaledWidth() - 80; }
    @Override public int defaultY() { return 100; }
    @Override public int defaultW() { return 20; }
    @Override public int defaultH() { return 400; }
}