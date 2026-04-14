package me.retucio.spotifyoverlay.hud.widgets.checkboxes;

import me.retucio.spotifyoverlay.config.ConfigManager;
import me.retucio.spotifyoverlay.hud.widgets.Checkbox;
import me.retucio.spotifyoverlay.spotify.SpotifyManager;


public class ShuffleCheckbox extends Checkbox {

    public ShuffleCheckbox() {
        super("shuffle", "shuffle playlist", 20, 50, 10, false);
    }

    @Override
    public void onToggle() {
        SpotifyManager.INSTANCE.toggleShuffle(checked);
        ConfigManager.INSTANCE.getConfig().shuffle = checked;
        super.onToggle();
    }

    @Override
    public int defaultX() {
        return 20;
    }

    @Override
    public int defaultY() {
        return 50;
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
