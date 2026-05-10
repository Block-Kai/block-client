package com.blockclient.mod.setting;

import java.util.function.BooleanSupplier;

/**
 * 滑动条设置
 */
public class SliderSetting extends Setting {
    private final double defaultValue;
    private final double minValue;
    private final double maxValue;
    private final double increment;
    private double value;
    private String suffix = "";

    public SliderSetting(String name, double value, double min, double max, double increment) {
        super(name);
        this.value = value;
        this.defaultValue = value;
        this.minValue = min;
        this.maxValue = max;
        this.increment = increment;
    }

    public SliderSetting(String name, double value, double min, double max) {
        this(name, value, min, max, 0.1);
    }

    public SliderSetting(String name, int value, int min, int max) {
        this(name, (double) value, (double) min, (double) max, 1.0);
    }

    public SliderSetting(String name, double value, double min, double max, double increment, BooleanSupplier visibility) {
        super(name, visibility);
        this.value = value;
        this.defaultValue = value;
        this.minValue = min;
        this.maxValue = max;
        this.increment = increment;
    }

    public SliderSetting(String name, int value, int min, int max, BooleanSupplier visibility) {
        this(name, (double) value, (double) min, (double) max, 1.0, visibility);
    }

    public double getValue() {
        return value;
    }

    public float getValueFloat() {
        return (float) value;
    }

    public int getValueInt() {
        return (int) value;
    }

    public void setValue(double value) {
        this.value = Math.round(value / increment) * increment;
    }

    public double getMin() {
        return minValue;
    }

    public double getMax() {
        return maxValue;
    }

    public double getIncrement() {
        return increment;
    }

    public String getSuffix() {
        return suffix;
    }

    public SliderSetting setSuffix(String suffix) {
        this.suffix = suffix;
        return this;
    }
}
