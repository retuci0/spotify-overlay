package me.retucio.spotifyoverlay.mixin;

import me.retucio.spotifyoverlay.SpotifyOverlay;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {

    @Inject(method = "keyPress", at = @At("HEAD"))
    private void onKey(long handle, int action, KeyEvent event, CallbackInfo ci) {
        SpotifyOverlay.INSTANCE.onKey(event.key(), action);
    }
}
