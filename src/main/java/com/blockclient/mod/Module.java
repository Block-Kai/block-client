package com.blockclient.mod;

import com.blockclient.mod.setting.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 模块基类
 */
public abstract class Module {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String name;
    private final String description;
    private final Category category;
    private final BindSetting bindSetting;
    private final List<Setting> settings = new ArrayList<>();
    protected boolean state;

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.bindSetting = add(new BindSetting("Key", -1));
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public BindSetting getBindSetting() {
        return bindSetting;
    }

    public boolean isOn() {
        return state;
    }

    public boolean isOff() {
        return !state;
    }

    public void toggle() {
        if (state) {
            disable();
        } else {
            enable();
        }
    }

    public void enable() {
        if (state) return;
        state = true;
        onEnable();
    }

    public void disable() {
        if (!state) return;
        state = false;
        onDisable();
    }

    /** 获取模块的额外信息（显示在 Arraylist 中） */
    public String getInfo() {
        return null;
    }

    // ── 生命周期方法 ──

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}

    /** 2D 渲染（HUD） */
    public void onRender2D(MatrixStack matrices, float tickDelta) {}

    /** 3D 渲染（World） */
    public void onRender3D(MatrixStack matrices, float tickDelta) {}

    /** 数据包发送拦截 */
    public boolean onPacketSend(Object packet) { return false; }

    /** 数据包接收拦截 */
    public boolean onPacketReceive(Object packet) { return false; }

    // ── 设置系统 ──

    public StringSetting add(StringSetting setting) {
        settings.add(setting);
        return setting;
    }

    public ColorSetting add(ColorSetting setting) {
        settings.add(setting);
        return setting;
    }

    public SliderSetting add(SliderSetting setting) {
        settings.add(setting);
        return setting;
    }

    public BooleanSetting add(BooleanSetting setting) {
        settings.add(setting);
        return setting;
    }

    public <T extends Enum<T>> EnumSetting<T> add(EnumSetting<T> setting) {
        settings.add(setting);
        return setting;
    }

    public BindSetting add(BindSetting setting) {
        settings.add(setting);
        return setting;
    }

    public List<Setting> getSettings() {
        return settings;
    }

    public <T extends EnumSetting<?>> T addEnum(T setting) {
        settings.add(setting);
        return setting;
    }

    public StringSetting addStr(StringSetting setting) {
        settings.add(setting);
        return setting;
    }

    /** 检查玩家/世界是否为 null */
    public static boolean nullCheck() {
        return mc.player == null || mc.world == null;
    }

    /** 发送带序号的包（用于绕过部分反作弊） */
    public static void sendSequencedPacket(java.util.function.IntFunction<net.minecraft.network.packet.Packet<?>> packetFactory) {
        if (mc.getNetworkHandler() == null) return;
        try {
            var updateManager = (net.minecraft.client.network.PendingUpdateManager)
                net.minecraft.client.MinecraftClient.class.getMethod("getNetworkedPacketManager")
                    .invoke(mc);
            int seq = updateManager.getSequence();
            mc.getNetworkHandler().sendPacket(packetFactory.apply(seq));
        } catch (Exception e) {
            mc.getNetworkHandler().sendPacket(packetFactory.apply(0));
        }
    }
}
