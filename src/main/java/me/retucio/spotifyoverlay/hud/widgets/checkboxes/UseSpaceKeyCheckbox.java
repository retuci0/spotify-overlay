package me.retucio.spotifyoverlay.hud.widgets.checkboxes;

import me.retucio.spotifyoverlay.config.ConfigManager;
import me.retucio.spotifyoverlay.hud.widgets.Checkbox;

public class UseSpaceKeyCheckbox extends Checkbox {

    public UseSpaceKeyCheckbox() {
        super("space key", "space to pause / resume", 20, 90, 10, true);
    }

    @Override
    public void onToggle() {
        ConfigManager.getConfig().useSpaceKey = checked;
    }

    @Override
    public int defaultX() {
        return 20;
    }

    @Override
    public int defaultY() {
        return 90;
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
