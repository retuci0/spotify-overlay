package me.retucio.spotifyoverlay.hud;

import me.retucio.spotifyoverlay.hud.widgets.Button;
import me.retucio.spotifyoverlay.hud.widgets.Overlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Hud {

    public static final Hud INSTANCE = new Hud();
    private final Minecraft mc = Minecraft.getInstance();

    private int mx, my;

    private final List<Widget> widgets = new ArrayList<>();
    private @Nullable Widget selected = null;

    public Hud() {
        addWidgets();
    }

    private void addWidgets() {
        widgets.add(new Overlay(mc.getWindow().getGuiScaledWidth() - 202, 2, 200, 60));
    }

    public void render(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        this.mx = mx;
        this.my = my;

        for (Widget widget : widgets) {
            if (widget.isVisible()) {
                widget.render(gui, mx, my, delta);
            }
        }
    }

    public void onClick(int button, int action) {
        for (Widget widget : widgets) {
            if (widget.isHovered(mx, my)) {
                widget.onClick(mx, my, button, action);
                selected = widget;
                break;
            }
        }
    }

    public boolean isSelected(Widget widget) {
        return widget == selected;
    }
}
