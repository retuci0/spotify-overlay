package me.retucio.spotifyoverlay.mixin;

import com.mojang.blaze3d.platform.Window;

import me.retucio.spotifyoverlay.hud.Hud;
import me.retucio.spotifyoverlay.hud.Widget;
import me.retucio.spotifyoverlay.hud.widgets.Overlay;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;


@Mixin(Window.class)
public abstract class WindowMixin {

    @Inject(method = "onResize", at = @At("RETURN"))
    private void onWindowResize(long handle, int newWidth, int newHeight, CallbackInfo ci) {
        for (Iterator<Widget> it = Hud.INSTANCE.getWigets(); it.hasNext(); ) {
            Widget widget = it.next();

            if (widget instanceof Overlay) continue;

            widget.setX(widget.defaultX());
            widget.setY(widget.defaultY());
            widget.setW(widget.defaultW());
            widget.setH(widget.defaultH());
        }
    }
}
