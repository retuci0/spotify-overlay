package me.retucio.spotifyoverlay.hud;

import me.retucio.spotifyoverlay.config.Config;
import me.retucio.spotifyoverlay.config.ConfigManager;
import me.retucio.spotifyoverlay.util.DrawUtil;
import me.retucio.spotifyoverlay.util.KeyUtil;
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

    private int prevmx, prevmy;

    private static final int EDGE_TOLERANCE = 5;
    private static final int MIN_W = 80;
    private static final int MIN_H = 40;

    protected ResizeMode resize = ResizeMode.NONE;

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
        if (!isSelected()) return;

        int dx = mx - prevmx;
        int dy = my - prevmy;
        prevmx = mx;
        prevmy = my;

        if (dragging) {
            x += dx;
            y += dy;
        } else if (resize != ResizeMode.NONE) {
            int newX = x, newY = y, newW = w, newH = h;

            switch (resize) {
                case LEFT_SIDE:
                    newW = w - dx;
                    newX = x + dx;
                    break;
                case RIGHT_SIDE:
                    newW = w + dx;
                    break;
                case TOP_SIDE:
                    newH = h - dy;
                    newY = y + dy;
                    break;
                case BOTTOM_SIDE:
                    newH = h + dy;
                    break;
                case TOP_LEFT_CORNER:
                    newW = w - dx;
                    newX = x + dx;
                    newH = h - dy;
                    newY = y + dy;
                    break;
                case TOP_RIGHT_CORNER:
                    newW = w + dx;
                    newH = h - dy;
                    newY = y + dy;
                    break;
                case BOTTOM_LEFT_CORNER:
                    newW = w - dx;
                    newX = x + dx;
                    newH = h + dy;
                    break;
                case BOTTOM_RIGHT_CORNER:
                    newW = w + dx;
                    newH = h + dy;
                    break;
            }

            if (newW >= MIN_W) {
                w = newW;
                x = newX;
            }
            if (newH >= MIN_H) {
                h = newH;
                y = newY;
            }

            Config config = ConfigManager.INSTANCE.getConfig();
            config.x = this.x;
            config.y = this.y;
            config.w = this.w;
            config.h = this.h;
        }
    }

    protected void onHover(int mx, int my) {

    }

    protected void onKey(int key, int action) {

    }

    protected void onClick(int mx, int my, int button, int action) {
        if (action != GLFW.GLFW_PRESS) {
            if (action == GLFW.GLFW_RELEASE) {
                dragging = false;
                resize = ResizeMode.NONE;
            }
            return;
        }

        prevmx = mx;
        prevmy = my;

        if (button == 0) {
            ResizeMode hit = getResizeModeAt(mx, my);
            if (hit != ResizeMode.NONE) {
                resize = hit;
                dragging = false;
                select();
                prevmx = mx;
                prevmy = my;
                return;
            }
            if (isHovered(mx, my)) {
                dragging = true;
                resize = ResizeMode.NONE;
                select();
            }
        } else if (button == 1) {
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

    public void renderOnControlPanel(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        int outlineColor, bgColor;
        outlineColor = isSelected() ? Color.GREEN.getRGB() : Color.RED.darker().getRGB();
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

    public boolean isSelected() {
        return Hud.INSTANCE.isSelected(this);
    }

    public void select() {
        Hud.INSTANCE.select(this);
    }

    public abstract int defaultX();
    public abstract int defaultY();
    public abstract int defaultW();
    public abstract int defaultH();

    public enum ResizeMode {
        NONE,
        LEFT_SIDE,
        RIGHT_SIDE,
        TOP_SIDE,
        BOTTOM_SIDE,
        TOP_LEFT_CORNER,
        TOP_RIGHT_CORNER,
        BOTTOM_LEFT_CORNER,
        BOTTOM_RIGHT_CORNER;
    }

    protected ResizeMode getResizeModeAt(int mx, int my) {
        boolean onLeft = Math.abs(mx - x) <= EDGE_TOLERANCE;
        boolean onRight = Math.abs(mx - (x + w)) <= EDGE_TOLERANCE;
        boolean onTop = Math.abs(my - y) <= EDGE_TOLERANCE;
        boolean onBottom = Math.abs(my - (y + h)) <= EDGE_TOLERANCE;

        if (onLeft && onTop) return ResizeMode.TOP_LEFT_CORNER;
        if (onRight && onTop) return ResizeMode.TOP_RIGHT_CORNER;
        if (onLeft && onBottom) return ResizeMode.BOTTOM_LEFT_CORNER;
        if (onRight && onBottom) return ResizeMode.BOTTOM_RIGHT_CORNER;
        if (onLeft) return ResizeMode.LEFT_SIDE;
        if (onRight) return ResizeMode.RIGHT_SIDE;
        if (onTop) return ResizeMode.TOP_SIDE;
        if (onBottom) return ResizeMode.BOTTOM_SIDE;
        return ResizeMode.NONE;
    }
}
