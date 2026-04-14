package me.retucio.spotifyoverlay.hud.widgets;

import me.retucio.spotifyoverlay.hud.Widget;
import me.retucio.spotifyoverlay.util.ChatUtil;
import me.retucio.spotifyoverlay.util.DrawUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public abstract class Checkbox extends Widget {

    protected boolean checked;

    public Checkbox(String name, String desc, int x, int y, int size, boolean defaultChecked) {
        super(name, desc, x, y, size, size);
        this.checked = defaultChecked;
    }

    @Override
    public void renderOnControlPanel(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        DrawUtil.drawRectOutline(gui, x, y, w, h, 1, -1);
        if (checked) gui.fill(x + 2, y + 2, x + w - 2, y + h - 2, -1);
        gui.text(mc.font, name, x + w + 5, y + h / 2 - mc.font.lineHeight / 2, -1);
        if (isHovered(mx, my)) renderTooltip(gui, mx, my, delta);
    }

    @Override
    public void renderTooltip(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        gui.setTooltipForNextFrame(Component.nullToEmpty(desc), mx, my);
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        // (only render on control panel)
    }

    @Override
    public void onClick(int mx, int my, int button, int action) {
        if (action == GLFW.GLFW_RELEASE) toggle();
        super.onClick(mx, my, button, action);
    }

    public void onToggle() {
        ChatUtil.info("toggled " + name + " " + (checked ? "on" : "off"));
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void toggle() {
        checked = !checked;
        onToggle();
    }
}
