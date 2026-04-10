package me.retucio.spotifyoverlay.mixin;

import me.retucio.spotifyoverlay.SpotifyOverlay;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
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
}
