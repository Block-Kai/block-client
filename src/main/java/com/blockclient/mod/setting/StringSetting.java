package com.blockclient.mod.setting;

/**
 * 字符串设置
 */
public class StringSetting extends Setting {
    private final String defaultValue;
    private String value;

    public StringSetting(String name, String defaultValue) {
        super(name);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
