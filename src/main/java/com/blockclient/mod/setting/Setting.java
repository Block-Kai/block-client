package com.blockclient.mod.setting;

import java.util.function.BooleanSupplier;

/**
 * 设置基类
 */
public abstract class Setting {
    private final String name;
    private final BooleanSupplier visibility;

    public Setting(String name) {
        this.name = name;
        this.visibility = null;
    }

    public Setting(String name, BooleanSupplier visibility) {
        this.name = name;
        this.visibility = visibility;
    }

    public boolean isVisible() {
        return visibility == null || visibility.getAsBoolean();
    }

    public String getName() {
        return name;
    }
}
