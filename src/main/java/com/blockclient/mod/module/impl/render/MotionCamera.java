package com.blockclient.mod.module.impl.render;

import com.blockclient.mod.Category;
import com.blockclient.mod.Module;
import com.blockclient.mod.setting.*;

public class MotionCamera extends Module {
    public static MotionCamera INSTANCE;
    public final SliderSetting intensity = add(new SliderSetting("Intensity", 0.02, 0.0, 0.1, 0.005));
    public final SliderSetting speed = add(new SliderSetting("Speed", 1.0, 0.1, 3.0, 0.1));

    public MotionCamera() {
        super("MotionCamera", "动感相机", Category.Render);
        INSTANCE = this;
    }

    public double getOffsetX() {
        if (!isOn() || nullCheck() || !isMoving()) return 0;
        double t = System.currentTimeMillis() / 1000.0 * speed.getValue();
        return Math.sin(t * 2.0) * intensity.getValue();
    }

    public double getOffsetY() {
        if (!isOn() || nullCheck() || !isMoving()) return 0;
        double t = System.currentTimeMillis() / 1000.0 * speed.getValue();
        return Math.sin(t * 3.0 + 1.0) * intensity.getValue() * 0.5;
    }

    private boolean isMoving() {
        if (mc.player == null) return false;
        var pi = mc.player.input.playerInput;
        return pi.forward() || pi.backward() || pi.left() || pi.right();
    }
}
