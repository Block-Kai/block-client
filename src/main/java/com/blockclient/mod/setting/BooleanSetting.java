package com.blockclient.mod.setting;

import java.util.function.BooleanSupplier;

/**
 * 布尔设置
 */
public class BooleanSetting extends Setting {
    private final boolean defaultValue;
    private boolean value;
    private boolean hasParent = false;
    private boolean open = false;

    public BooleanSetting(String name, boolean defaultValue) {
        super(name);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public BooleanSetting(String name, boolean defaultValue, BooleanSupplier visibility) {
        super(name, visibility);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public boolean isOpen() {
        if (hasParent) return open;
        return true;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean hasParent() {
        return hasParent;
    }

    public BooleanSetting setParent() {
        this.hasParent = true;
        return this;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }
}
