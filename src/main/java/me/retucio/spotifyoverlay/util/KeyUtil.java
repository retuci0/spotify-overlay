package me.retucio.spotifyoverlay.util;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class KeyUtil {

    private static final Minecraft mc = Minecraft.getInstance();

    public static boolean isKeyDown(int key) {
        return GLFW.glfwGetKey(mc.getWindow().handle(), key) != GLFW.GLFW_RELEASE;
    }

    public static boolean isShiftDown() {
        return isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)
            || isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }
}
