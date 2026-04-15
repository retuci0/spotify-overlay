package me.retucio.spotifyoverlay.hud.widgets.checkboxes;

import me.retucio.spotifyoverlay.config.ConfigManager;
import me.retucio.spotifyoverlay.hud.widgets.Checkbox;

public class BlurCheckbox extends Checkbox {

    public BlurCheckbox() {
        super("blur", "apply blur to control panel screen background", 20, 135, 10, true);
    }

    @Override
    public void onToggle() {
        ConfigManager.getConfig().blur = checked;
    }

    @Override
    public int defaultX() {
        return 20;
    }

    @Override
    public int defaultY() {
        return 135;
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
