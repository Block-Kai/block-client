package com.blockclient.mod.setting;

import java.util.function.BooleanSupplier;

/**
 * 枚举设置
 */
public class EnumSetting<T extends Enum<T>> extends Setting {
    private final T defaultValue;
    private T value;

    public EnumSetting(String name, T defaultValue) {
        super(name);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public EnumSetting(String name, T defaultValue, BooleanSupplier visibility) {
        super(name, visibility);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean is(T mode) {
        return value == mode;
    }
}
