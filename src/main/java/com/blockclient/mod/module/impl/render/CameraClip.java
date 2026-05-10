package com.blockclient.mod.module.impl.render;

import com.blockclient.mod.Category;
import com.blockclient.mod.Module;
import com.blockclient.mod.setting.*;

/**
 * CameraClip - 第三人称相机不穿墙
 * 在第三人称时，相机不会穿过方块（透视效果）
 */
public class CameraClip extends Module {
    public static CameraClip INSTANCE;

    public final SliderSetting distance = add(new SliderSetting("Distance", 4.0, 1.0, 10.0, 0.5));
    public final BooleanSetting noClip = add(new BooleanSetting("NoClip", true));

    public CameraClip() {
        super("CameraClip", "第三人称相机不穿墙", Category.Render);
        INSTANCE = this;
    }

    public boolean shouldClip() {
        return isOn() && noClip.getValue();
    }

    public double getCameraDistance() {
        return distance.getValue();
    }
}
