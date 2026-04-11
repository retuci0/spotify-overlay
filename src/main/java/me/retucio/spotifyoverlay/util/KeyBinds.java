package me.retucio.spotifyoverlay.util;

import com.mojang.blaze3d.platform.InputConstants;
import me.retucio.spotifyoverlay.SpotifyOverlay;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class KeyBinds {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(SpotifyOverlay.MOD_ID, "spotify"));

    public static final KeyMapping OPEN_CONTROL_PANEL = new KeyMapping("control panel", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_HOME, CATEGORY);
    public static final KeyMapping PAUSE_OR_RESUME = new KeyMapping("pause or resume", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, CATEGORY);
    public static final KeyMapping PREV_TRACK = new KeyMapping("previous track", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_PAGE_DOWN, CATEGORY);
    public static final KeyMapping NEXT_TRACK = new KeyMapping("next track", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_PAGE_UP, CATEGORY);
    public static final KeyMapping FORWARD_5S = new KeyMapping("foward 5 sec", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_BRACKET, CATEGORY);
    public static final KeyMapping BACKWARD_5S = new KeyMapping("backward 5 sec", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_BRACKET, CATEGORY);
    public static final KeyMapping VOLUME_UP = new KeyMapping("volume up", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UP, CATEGORY);
    public static final KeyMapping VOLUME_DOWN = new KeyMapping("volume down", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_DOWN, CATEGORY);
    public static final KeyMapping TOGGLE_SHUFFLE = new KeyMapping("toggle shuffle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY);

    public static List<KeyMapping> spotifyKeys = List.of(OPEN_CONTROL_PANEL, PAUSE_OR_RESUME, PREV_TRACK, NEXT_TRACK,
            FORWARD_5S, BACKWARD_5S, VOLUME_UP, VOLUME_DOWN, TOGGLE_SHUFFLE);

    public static KeyMapping[] apply(KeyMapping[] binds) {
        KeyMapping[] newBinds = new KeyMapping[binds.length + 9];

        System.arraycopy(binds, 0, newBinds, 0, binds.length);

        for (int i = 0; i < 9; i++) {
            newBinds[binds.length + i] = spotifyKeys.get(i);
        }

        return newBinds;
    }
}