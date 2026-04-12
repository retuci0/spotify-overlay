package me.retucio.spotifyoverlay.mixin;

import com.mojang.blaze3d.platform.Window;
import me.retucio.spotifyoverlay.SpotifyOverlay;
import me.retucio.spotifyoverlay.hud.Hud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Inject(method = "onButton", at = @At("HEAD"))
    private void onClick(long handle, MouseButtonInfo info, int action, CallbackInfo ci) {
        SpotifyOverlay.INSTANCE.onClick(info.button(), action);
    }

    @Inject(method = "onMove", at = @At("HEAD"))
    private void onMouseMove(long handle, double mx, double my, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();
        int sx = Mth.floor(mc.mouseHandler.getScaledXPos(window));
        int sy = Mth.floor(mc.mouseHandler.getScaledYPos(window));
        Hud.INSTANCE.onMouseMove(sx, sy);
    }
}
