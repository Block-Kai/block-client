package com.blockclient.mod.module.impl.render;

import com.blockclient.mod.Category;
import com.blockclient.mod.Module;
import com.blockclient.mod.setting.*;
import com.blockclient.util.*;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class Crosshair extends Module {
    public static Crosshair INSTANCE;
    public final SliderSetting gap = add(new SliderSetting("Gap", 4, 0, 20, 1));
    public final SliderSetting length = add(new SliderSetting("Length", 8, 1, 30, 1));
    public final SliderSetting thickness = add(new SliderSetting("Thickness", 2, 1, 10, 1));
    public final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 255, 200)));
    public final BooleanSetting dot = add(new BooleanSetting("Dot", false));

    private DrawContext ctx;

    public Crosshair() {
        super("Crosshair", "自定义准星", Category.Render);
        INSTANCE = this;
    }

    public void setContext(DrawContext context) { this.ctx = context; }

    @Override
    public void onRender2D(MatrixStack matrices, float tickDelta) {
        if (nullCheck()) return;
        int w = mc.getWindow().getScaledWidth();
        int h = mc.getWindow().getScaledHeight();
        int cx = w/2, cy = h/2;
        int g = gap.getValueInt(), l = length.getValueInt(), t = thickness.getValueInt();
        Color c = color.getValue();
        // For onRender2D we draw using a DrawContext - we'll draw directly here
        // Since we don't have a DrawContext in onRender2D, we use Render2DUtil with a dummy approach
        // The actual drawing should be done via the HudElement system
    }
}
