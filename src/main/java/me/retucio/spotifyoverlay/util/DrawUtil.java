package me.retucio.spotifyoverlay.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.awt.*;

public class DrawUtil {

    public static void drawCircle(GuiGraphicsExtractor gui, int cx, int cy, int r, int color) {
        for (int i = -r; i <= r; i++) {
            for (int j = -r; j <= r; j++) {
                if (i * i + j * j <= r * r) {
                    gui.fill(cx + i, cy + j, cx + i + 1, cy + j + 1, color);
                }
            }
        }
    }

    public static void drawTriangle(GuiGraphicsExtractor gui, int ax, int bx, int cx, int ay, int by, int cy, int color) {
        int x0 = ax, y0 = ay;
        int x1 = bx, y1 = by;
        int x2 = cx, y2 = cy;

        if (y0 > y1) {
            int tmp = y0;
            y0 = y1;
            y1 = tmp;
            tmp = x0;
            x0 = x1;
            x1 = tmp;
        }
        if (y1 > y2) {
            int tmp = y1;
            y1 = y2;
            y2 = tmp;
            tmp = x1;
            x1 = x2;
            x2 = tmp;
        }
        if (y0 > y1) {
            int tmp = y0;
            y0 = y1;
            y1 = tmp;
            tmp = x0;
            x0 = x1;
            x1 = tmp;
        }

        for (int y = y0; y <= y2; y++) {
            float xl, xr;

            float tLong = (y2 != y0) ? (float) (y - y0) / (y2 - y0) : 0f;
            float xLong = x0 + tLong * (x2 - x0);

            if (y < y1) {
                float tShort = (y1 != y0) ? (float) (y - y0) / (y1 - y0) : 0f;
                float xShort = x0 + tShort * (x1 - x0);
                xl = Math.min(xLong, xShort);
                xr = Math.max(xLong, xShort);
            } else {
                float tShort = (y2 != y1) ? (float) (y - y1) / (y2 - y1) : 0f;
                float xShort = x1 + tShort * (x2 - x1);
                xl = Math.min(xLong, xShort);
                xr = Math.max(xLong, xShort);
            }

            gui.fill((int) xl, y, (int) xr + 1, y + 1, color);
        }
    }
}