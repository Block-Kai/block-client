package com.blockclient.util;

import net.minecraft.client.gui.DrawContext;
import java.awt.Color;

/**
 * 2D 渲染工具（屏幕空间）- 1.21.4 compatible
 */
public class Render2DUtil {

    /** 填充矩形 */
    public static void drawRect(DrawContext ctx, float x, float y, float width, float height, Color c) {
        drawRect(ctx, x, y, width, height, c.getRGB());
    }

    public static void drawRect(DrawContext ctx, float x, float y, float width, float height, int color) {
        ctx.fill((int)x, (int)y, (int)(x + width), (int)(y + height), color);
    }

    /** 圆角矩形（简单实现，使用正方形近似） */
    public static void drawRoundedRect(DrawContext ctx, float x, float y, float width, float height, float radius, Color c) {
        if (width <= 0 || height <= 0) return;
        float r = Math.min(radius, Math.min(width, height) / 2.0f);
        // 中心 + 边
        drawRect(ctx, x + r, y, width - 2 * r, height, c);
        drawRect(ctx, x, y + r, r, height - 2 * r, c);
        drawRect(ctx, x + width - r, y + r, r, height - 2 * r, c);
        // 四个角（用小的矩形近似）
        drawRect(ctx, x, y, r, r, c);
        drawRect(ctx, x + width - r, y, r, r, c);
        drawRect(ctx, x, y + height - r, r, r, c);
        drawRect(ctx, x + width - r, y + height - r, r, r, c);
    }
}
