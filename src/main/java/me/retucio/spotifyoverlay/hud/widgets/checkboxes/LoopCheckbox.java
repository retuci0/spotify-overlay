package me.retucio.spotifyoverlay.hud.widgets.checkboxes;

import me.retucio.spotifyoverlay.config.ConfigManager;
import me.retucio.spotifyoverlay.hud.widgets.Checkbox;
import me.retucio.spotifyoverlay.spotify.SpotifyManager;


public class LoopCheckbox extends Checkbox {

    public LoopCheckbox() {
        super("loop", "loop over the same song (will override shuffle)", 20, 65, 10, false);
    }

    @Override
    public void onToggle() {
        SpotifyManager.INSTANCE.toggleLoop(checked);
        ConfigManager.getConfig().loop = checked;
        super.onToggle();
    }

    @Override
    public int defaultX() {
        return 20;
    }

    @Override
    public int defaultY() {
        return 65;
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
