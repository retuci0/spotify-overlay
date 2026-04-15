package me.retucio.spotifyoverlay.hud.screen;

import me.retucio.spotifyoverlay.config.ConfigManager;
import me.retucio.spotifyoverlay.hud.Hud;
import me.retucio.spotifyoverlay.hud.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.Iterator;

public class ControlPanelScreen extends Screen {

    public final static ControlPanelScreen INSTANCE = new ControlPanelScreen();
    private final Minecraft mc = Minecraft.getInstance();

    protected ControlPanelScreen() {
        super(Component.nullToEmpty("control panel"));
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        gui.pose().pushMatrix();
        gui.pose().scale(2.0f);
        gui.text(mc.font, "control panel", 10, 10, -1);
        gui.pose().popMatrix();

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
    public boolean keyPressed(@NonNull KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_SPACE && ConfigManager.getConfig().useSpaceKey) {
            Hud.INSTANCE.getPauseOrResumeButton().pauseOrResume();
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        Hud.INSTANCE.select(null);
        super.onClose();
    }

    @Override
    protected void extractBlurredBackground(@NonNull GuiGraphicsExtractor gui) {
        if (ConfigManager.getConfig().blur) {
            super.extractBlurredBackground(gui);
        }
    }
}
