package com.blockclient.mod.module.impl.render;

import com.blockclient.mod.Category;
import com.blockclient.mod.Module;
import com.blockclient.mod.setting.*;

/**
 * Ambience - 环境滤镜
 */
public class Ambience extends Module {
    public static Ambience INSTANCE;

    public final EnumSetting<Time> time = add(new EnumSetting<>("Time", Time.Normal));
    public final BooleanSetting clearWeather = add(new BooleanSetting("ClearWeather", true));
    public final SliderSetting gamma = add(new SliderSetting("Gamma", 1.0, 0.1, 5.0, 0.1));

    private double originalGamma;

    public Ambience() {
        super("Ambience", "环境滤镜", Category.Render);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (mc.options != null) {
            originalGamma = mc.options.getGamma().getValue();
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.getGamma().setValue(originalGamma);
        }
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;
        mc.options.getGamma().setValue(gamma.getValue() * 0.5);
        if (clearWeather.getValue()) {
            mc.world.setRainGradient(0);
            mc.world.setThunderGradient(0);
        }
    }

    public long getTimeOffset() {
        return switch (time.getValue()) {
            case Day -> 1000;
            case Night -> 13000;
            case Sunset -> 12000;
            case Normal -> 0;
        };
    }

    public enum Time { Normal, Day, Night, Sunset }
}
