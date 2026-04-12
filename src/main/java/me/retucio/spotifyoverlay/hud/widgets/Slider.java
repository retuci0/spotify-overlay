package me.retucio.spotifyoverlay.hud.widgets;

import me.retucio.spotifyoverlay.hud.Hud;
import me.retucio.spotifyoverlay.hud.Widget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.text.DecimalFormat;

public abstract class Slider extends Widget {

    private final Color BG_COLOR = new Color(120, 120, 120, 180);

    protected boolean dragging = false;
    protected final float min, max;
    protected float value;
    protected final float increment;

    public Slider(String name, String desc, int x, int y, int w, int h, float defaultValue, float min, float max, float increment) {
        super(name, desc, x, y, w, h);
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        Color bgColor = BG_COLOR;
        if (isHovered(mx, my)) {
            bgColor = bgColor.brighter();
        }

        gui.fill(x, y, x + w, y + h, bgColor.getRGB());

        double percent = (value - min) / (max - min);
        int filled = (int) (percent * h);
        gui.fill(x, y + h - filled, x + w, y + h, -1);

        dragging = dragging && Hud.INSTANCE.isSelected(this);

        super.render(gui, mx, my, delta);
    }

    @Override
    public void onClick(int mx, int my, int button, int action) {
        if (button != 0) return;
        if (action == GLFW.GLFW_PRESS) {
            dragging = true;
        } else if (action == GLFW.GLFW_RELEASE) {
            dragging = false;
        }

        if (dragging) updateValue(my);
        super.onClick(mx, my, button, action);
    }

    @Override
    public void onMouseMove(int mx, int my) {
        if (dragging && Hud.INSTANCE.isSelected(this)) {
            updateValue(my);
        }
    }

    private void updateValue(int my) {
        float newVal = min + ((y + h - my) / (float) h) * (max - min);
        newVal = Math.max(min, Math.min(max, newVal));
        newVal = Math.round(newVal / increment) * increment;
        if (newVal != value) setValue(newVal);
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
        onChange();
    }

    protected abstract void onChange();
}
