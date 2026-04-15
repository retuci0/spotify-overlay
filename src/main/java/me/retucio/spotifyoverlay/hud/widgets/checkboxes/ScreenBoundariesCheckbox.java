package me.retucio.spotifyoverlay.hud.widgets.checkboxes;

import me.retucio.spotifyoverlay.config.ConfigManager;
import me.retucio.spotifyoverlay.hud.widgets.Checkbox;


public class ScreenBoundariesCheckbox extends Checkbox {

    public ScreenBoundariesCheckbox() {
        super("screen boundaries", "avoid moving the overlay out of the screen", 20, 120, 10, false);
    }

    @Override
    public void onToggle() {
        ConfigManager.getConfig().screenBoundaries = checked;
    }

    @Override
    public int defaultX() {
        return 20;
    }

    @Override
    public int defaultY() {
        return 120;
    }

    @Override
    public int defaultW() {
        return 10;
    }

    @Override
    public int defaultH() {
        return 10;
    }
}
