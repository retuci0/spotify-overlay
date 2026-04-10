package me.retucio.spotifyoverlay.hud.widgets;

import me.retucio.spotifyoverlay.hud.Widget;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.awt.*;

public class Button extends Widget {

    public Button(String name, String desc, int x, int y, int w, int h) {
        super(name, desc, x, y, w, h);
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        gui.fill(x, y, x + w, y + h, -1);
        gui.text(mc.font, name, x, y, 0);
        super.render(gui, mx, my, delta);
    }

    @Override
    protected void onHover(int mx, int my) {

    }

    @Override
    protected void onClick(int mx, int my, int button, int action) {

    }
}
