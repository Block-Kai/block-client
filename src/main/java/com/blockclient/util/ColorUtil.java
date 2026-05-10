package com.blockclient.util;

import java.awt.Color;

/**
 * 颜色工具
 */
public class ColorUtil {

    /** 注入 alpha 通道 */
    public static Color injectAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, alpha)));
    }

    public static Color injectAlpha(int color, int alpha) {
        Color c = new Color(color);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, alpha)));
    }

    /** 脉冲颜色 */
    public static Color pulseColor(Color color, int index, int speed, float offset) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float pulse = (float) (Math.sin((System.currentTimeMillis() / (double) speed + index * offset)) + 1.0) / 2.0f;
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], Math.max(0.3f, pulse)));
    }

    /** 在两个颜色之间渐变 */
    public static Color fadeColor(Color from, Color to, double progress) {
        double p = Math.max(0, Math.min(1, progress));
        int r = (int) (from.getRed() + (to.getRed() - from.getRed()) * p);
        int g = (int) (from.getGreen() + (to.getGreen() - from.getGreen()) * p);
        int b = (int) (from.getBlue() + (to.getBlue() - from.getBlue()) * p);
        int a = (int) (from.getAlpha() + (to.getAlpha() - from.getAlpha()) * p);
        return new Color(r, g, b, a);
    }
}
