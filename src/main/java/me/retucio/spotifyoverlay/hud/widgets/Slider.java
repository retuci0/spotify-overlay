package me.retucio.spotifyoverlay.hud.widgets;

import me.retucio.spotifyoverlay.hud.Hud;
import me.retucio.spotifyoverlay.hud.Widget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

import java.awt.*;


public abstract class Slider extends Widget {

    private static final Color BG_COLOR = new Color(120, 120, 120, 180);

    protected boolean dragging = false;
    protected float min, max, increment;
    protected float value;

    protected Slider(String name, String desc, int x, int y, int w, int h, float defaultValue, float min, float max, float increment) {
        super(name, desc, x, y, w, h);
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    protected abstract int trackSize();

    protected abstract float percent(int mx, int my);

    protected abstract void drawFilled(GuiGraphicsExtractor gui, int filled);

    @Override
    public void render(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        Color bg = isHovered(mx, my) ? BG_COLOR.brighter() : BG_COLOR;
        gui.fill(x, y, x + w, y + h, bg.getRGB());

        int filled = (int) (((value - min) / (max - min)) * trackSize());
        drawFilled(gui, filled);

        dragging = dragging && Hud.INSTANCE.isSelected(this);
        super.render(gui, mx, my, delta);
    }

    @Override
    public void onClick(int mx, int my, int button, int action) {
        if (button != 0) return;
        if (action == GLFW.GLFW_PRESS) dragging = true;
        else if (action == GLFW.GLFW_RELEASE) dragging = false;
        if (dragging) updateValue(mx, my);
        super.onClick(mx, my, button, action);
    }

    @Override
    public void onMouseMove(int mx, int my) {
        if (dragging && Hud.INSTANCE.isSelected(this)) {
            updateValue(mx, my);
        }
    }

    private void updateValue(int mx, int my) {
        float value = getValue(mx, my);
        if (value != this.value) setValue(value);
    }

    public float getValue() { return value; }

    public float getValue(int mx, int my) {
        float raw = min + percent(mx, my) * (max - min);
        return Math.round(Math.clamp(raw, min, max) / increment) * increment;
    }

    public void setValue(float value) {
        this.value = Math.clamp(value, min, max);
        onChange();
    }

    protected void onChange() {}
}