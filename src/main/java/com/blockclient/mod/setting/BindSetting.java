package com.blockclient.mod.setting;

import java.util.function.BooleanSupplier;

/**
 * 按键绑定设置（只存 int keycode）
 */
public class BindSetting extends Setting {
    private final int defaultValue;
    public boolean holding = false;
    private int value;
    private boolean pressed = false;
    private boolean holdEnable = false;

    public BindSetting(String name, int value) {
        super(name);
        this.defaultValue = value;
        this.value = value;
    }

    public BindSetting(String name, int value, BooleanSupplier visibility) {
        super(name, visibility);
        this.defaultValue = value;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public boolean isPressed() {
        return pressed;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    public boolean isHoldEnable() {
        return holdEnable;
    }

    public void setHoldEnable(boolean holdEnable) {
        this.holdEnable = holdEnable;
    }

    public int getDefaultValue() {
        return defaultValue;
    }
}
