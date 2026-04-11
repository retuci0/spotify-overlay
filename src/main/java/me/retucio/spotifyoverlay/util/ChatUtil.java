package me.retucio.spotifyoverlay.util;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ChatUtil {

    private static final Minecraft mc = Minecraft.getInstance();

    public static void info(String text) {
        mc.gui.getChat().addClientSystemMessage(Component.nullToEmpty(ChatFormatting.DARK_GREEN + "[spotify] " + ChatFormatting.RESET + text + ChatFormatting.RESET));
    }

    public static void warn(String text) {
        mc.gui.getChat().addClientSystemMessage(Component.nullToEmpty(ChatFormatting.DARK_GREEN + "[spotify] " + ChatFormatting.YELLOW + text + ChatFormatting.RESET));
    }

    public static void error(String text) {
        mc.gui.getChat().addClientSystemMessage(Component.nullToEmpty(ChatFormatting.DARK_GREEN + "[spotify] " + ChatFormatting.RED + text + ChatFormatting.RESET));
    }
}
