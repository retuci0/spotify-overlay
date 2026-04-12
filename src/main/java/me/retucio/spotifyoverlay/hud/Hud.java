package me.retucio.spotifyoverlay.hud;

import me.retucio.spotifyoverlay.hud.screen.HudEditorScreen;
import me.retucio.spotifyoverlay.hud.widgets.Overlay;
import me.retucio.spotifyoverlay.hud.widgets.elements.PauseOrResumeButton;
import me.retucio.spotifyoverlay.hud.widgets.elements.NextButton;
import me.retucio.spotifyoverlay.hud.widgets.elements.PrevButton;
import me.retucio.spotifyoverlay.hud.widgets.elements.VolumeSlider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

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
        widgets.add(new VolumeSlider());
    }

    public void render(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        for (Widget widget : widgets) {
            if (widget.isVisible()) {
                widget.render(gui, mx, my, delta);
            }
        }
    }

    public void onClick(int button, int action) {
        if (!(mc.screen instanceof HudEditorScreen)) return;
        if (action == GLFW.GLFW_RELEASE && button == 0) select(null);
        for (Widget widget : widgets.reversed()) {
            if (widget.isHovered(mx, my)) {
                widget.onClick(mx, my, button, action);
                break;
            }
        }
    }

    public void onKey(int key, int action) {
        if (!(mc.screen instanceof HudEditorScreen)) return;
        if (selected != null) selected.onKey(key, action);
    }

    public void onMouseMove(int mx, int my) {
        if (!(mc.screen instanceof HudEditorScreen)) return;
        if (selected != null) selected.onMouseMove(mx, my);
        this.mx = mx;
        this.my = my;
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

    public VolumeSlider getVolumeSlider() {
            for (Widget widget : widgets) {
                if (widget instanceof VolumeSlider slider) {
                    return slider;
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
