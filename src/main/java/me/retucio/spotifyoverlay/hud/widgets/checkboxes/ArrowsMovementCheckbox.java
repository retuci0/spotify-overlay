package me.retucio.spotifyoverlay.hud.widgets.checkboxes;

import me.retucio.spotifyoverlay.config.ConfigManager;
import me.retucio.spotifyoverlay.hud.widgets.Checkbox;

public class ArrowsMovementCheckbox extends Checkbox {

    public ArrowsMovementCheckbox() {
        super("arrows movement", "use the arrow keys when the overlay is selected to move it", 20, 105, 10, true);
    }

    @Override
    public void onToggle() {
        ConfigManager.getConfig().arrowsMovement = checked;
    }

    @Override
    public int defaultX() {
        return 20;
    }

    @Override
    public int defaultY() {
        return 105;
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
