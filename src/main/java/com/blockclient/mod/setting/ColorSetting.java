package com.blockclient.mod.setting;

import java.awt.Color;
import java.util.function.BooleanSupplier;

/**
 * 颜色设置
 */
public class ColorSetting extends Setting {
    private final Color defaultValue;
    public boolean rainbow = false;
    public boolean injectBoolean = false;
    public boolean booleanValue = false;
    private Color value;

    public ColorSetting(String name) {
        this(name, new Color(255, 255, 255));
    }

    public ColorSetting(String name, BooleanSupplier visibility) {
        super(name, visibility);
        this.value = new Color(255, 255, 255);
        this.defaultValue = new Color(255, 255, 255);
    }

    public ColorSetting(String name, Color defaultValue) {
        super(name);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public ColorSetting(String name, Color defaultValue, BooleanSupplier visibility) {
        super(name, visibility);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public ColorSetting(String name, int defaultValue) {
        this(name, new Color(defaultValue, true));
    }

    public Color getValue() {
        return value;
    }

    public void setValue(Color value) {
        this.value = value;
    }

    public ColorSetting injectBoolean(boolean value) {
        this.injectBoolean = true;
        this.booleanValue = value;
        return this;
    }
}
