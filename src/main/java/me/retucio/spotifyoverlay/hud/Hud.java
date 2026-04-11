package me.retucio.spotifyoverlay.hud;

import me.retucio.spotifyoverlay.hud.widgets.Overlay;
import me.retucio.spotifyoverlay.hud.widgets.buttons.PauseOrResumeButton;
import me.retucio.spotifyoverlay.hud.widgets.buttons.NextButton;
import me.retucio.spotifyoverlay.hud.widgets.buttons.PrevButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
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
        widgets.add(new Overlay());
        widgets.add(new PauseOrResumeButton());
        widgets.add(new PrevButton());
        widgets.add(new NextButton());
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
        for (Widget widget : widgets.reversed()) {
            if (widget.isHovered(mx, my)) {
                widget.onClick(mx, my, button, action);
                break;
            }
        }
    }

    public Overlay getOverlay() {
        for (Widget widget : widgets) {
            if (widget instanceof Overlay overlay) {
                return overlay;
            }
        }
        return null;
    }

    public PauseOrResumeButton getPauseOrResumeButton() {
        for (Widget widget : widgets) {
            if (widget instanceof PauseOrResumeButton button) {
                return button;
            }
        }
        return null;
    }

    public boolean isSelected(Widget widget) {
        return widget == selected;
    }

    public void select(Widget widget) {
        this.selected = widget;
    }

    public Iterator<Widget> getWigets() {
        return widgets.iterator();
    }
}
