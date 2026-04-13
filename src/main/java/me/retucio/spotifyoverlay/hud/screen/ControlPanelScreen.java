package me.retucio.spotifyoverlay.hud.screen;

import me.retucio.spotifyoverlay.hud.Hud;
import me.retucio.spotifyoverlay.hud.Widget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.Iterator;

public class ControlPanelScreen extends Screen {

    public final static ControlPanelScreen INSTANCE = new ControlPanelScreen();

    protected ControlPanelScreen() {
        super(Component.nullToEmpty("control panel"));
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        Iterator<Widget> it = Hud.INSTANCE.getWigets();
        while (it.hasNext()) {
            Widget widget = it.next();
            widget.renderOnControlPanel(gui, mouseX, mouseY, delta);
        }
        super.extractRenderState(gui, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubled) {
        return super.mouseClicked(event, doubled);
    }

    @Override
    public void onClose() {
        Hud.INSTANCE.select(null);
        super.onClose();
    }
}
