package me.retucio.spotifyoverlay.mixin;

import me.retucio.spotifyoverlay.SpotifyOverlay;
import me.retucio.spotifyoverlay.hud.screen.AuthScreen;
import me.retucio.spotifyoverlay.spotify.SpotifyAuth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    public abstract void setScreen(Screen screen);

    @Inject(method = "stop", at = @At("HEAD"))
    private void onShutdown(CallbackInfo ci) {
        SpotifyOverlay.INSTANCE.onShutdown();
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof TitleScreen
                && SpotifyAuth.shouldAuth
                && SpotifyAuth.authLink != null) {
            ci.cancel();
            setScreen(new AuthScreen(SpotifyAuth.authLink));
            SpotifyAuth.shouldAuth = false;
        }
    }
}
