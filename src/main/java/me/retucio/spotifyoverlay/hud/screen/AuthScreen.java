package me.retucio.spotifyoverlay.hud.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import java.net.URI;

public class AuthScreen extends Screen {

    private final URI authLink;

    private final Minecraft mc = Minecraft.getInstance();

    public AuthScreen(URI authLink) {
        this.authLink = authLink;
        super(Component.nullToEmpty("spotify auth"));
    }

    @Override
    protected void init() {
        clearWidgets();
        LinearLayout layout = LinearLayout.vertical();
        super.init();
        layout.defaultCellSetting().alignHorizontallyCenter().padding(10);

        final Button noButton = Button.builder(
                Component.nullToEmpty("no thanks"),
                button -> this.onClose()
        ).build();

        final Button openButton = Button.builder(
                Component.nullToEmpty("open in browser"),
                button -> clickUrlAction(mc, this, authLink)
        ).build();

        final Button copyButton = Button.builder(
                Component.nullToEmpty("copy to clipboard"),
                button -> {
                    mc.keyboardHandler.setClipboard(authLink.toASCIIString());
                    onClose();
                }
        ).build();

        layout.addChild(new StringWidget(Component.nullToEmpty("authenticate with spotify"), this.font));

        layout.addChild(noButton);
        layout.addChild(openButton);
        layout.addChild(copyButton);

        layout.arrangeElements();

        layout.setPosition(
                (this.width - layout.getWidth()) / 2,
                (this.height - layout.getHeight()) / 2
        );

        layout.visitWidgets(this::addRenderableWidget);
    }

    @Override
    public boolean canInterruptWithAnotherScreen() {
        return true;
    }

    @Override
    public void onClose() {
        mc.execute(() -> mc.setScreen(new TitleScreen()));
    }
}
