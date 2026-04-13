package me.retucio.spotifyoverlay.hud;

import me.retucio.spotifyoverlay.util.DrawUtil;
import me.retucio.spotifyoverlay.util.KeyUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.List;

public abstract class Widget {

    protected final Minecraft mc = Minecraft.getInstance();

    protected final int PADDING = 2;

    protected final String name, desc;

    protected int x, y, w, h;
    protected int dx, dy;
    protected boolean visible;

    protected boolean dragging;

    public Widget(String name, String desc, int x, int y, int w, int h) {
        this.name = name;
        this.desc = desc;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.visible = true;
        this.dragging = false;
    }

    protected void onMouseMove(int mx, int my) {

    }

    protected void onHover(int mx, int my) {

    }

    protected void onKey(int key, int action) {

    }

    protected void onClick(int mx, int my, int button, int action) {
        if (action != GLFW.GLFW_PRESS) return;
        if (button == 0) Hud.INSTANCE.select(this);
        else if (button == 1) {
            if (KeyUtil.isShiftDown()) {
                resetPosition();
            } else {
                visible = !visible;
            }
        }
    }

    public void render(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        renderTooltip(gui, mx, my, delta);
    }

    public void renderOnHudEditor(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        int outlineColor, bgColor;
        outlineColor = Hud.INSTANCE.isSelected(this) ? Color.GREEN.getRGB() : Color.RED.darker().getRGB();
        bgColor = this.visible ? Color.GREEN.getRGB() : Color.RED.darker().getRGB();

        gui.fill(x, y , x + w , y + h, bgColor);
        DrawUtil.drawRectOutline(gui, x - PADDING, y - PADDING, w + PADDING, h + PADDING, PADDING, outlineColor);
        render(gui, mx, my, delta);
    }

    public void renderTooltip(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        if (!isHovered(mx, my)) return;
        gui.setComponentTooltipForNextFrame(mc.font,
                List.of(
                    Component.nullToEmpty(name),
                    Component.nullToEmpty(desc)
                ),
                mx,
                my
        );
    }

    public boolean isHovered(int mx, int my) {
        return mx >= x && mx <= x + w
            && my >= y && my <= y + h;
    }

    void resetPosition() {
        x = defaultX();
        y = defaultY();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public abstract int defaultX();
    public abstract int defaultY();
    public abstract int defaultW();
    public abstract int defaultH();
}
