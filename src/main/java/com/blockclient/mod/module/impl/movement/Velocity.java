package com.blockclient.mod.module.impl.movement;

import com.blockclient.mod.Category;
import com.blockclient.mod.Module;
import com.blockclient.mod.setting.*;
import com.blockclient.util.*;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;

/**
 * Velocity - 防击退
 * 取消/减少受到的击退和爆炸击飞
 */
public class Velocity extends Module {
    public static Velocity INSTANCE;

    public final EnumSetting<Mode> mode = add(new EnumSetting<>("Mode", Mode.Plain));
    public final SliderSetting horizontal = add(new SliderSetting("Horizontal", 0.0, 0.0, 100.0, 1.0).setSuffix("%"));
    public final SliderSetting vertical = add(new SliderSetting("Vertical", 0.0, 0.0, 100.0, 1.0).setSuffix("%"));

    public Velocity() {
        super("Velocity", "减少/取消击退", Category.Movement);
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        return mode.getValue().name();
    }

    /** 处理速度包（从 mixin 调用） */
    public void onVelocityPacket(EntityVelocityUpdateS2CPacket packet) {
        if (nullCheck()) return;
        if (packet.getEntityId() != mc.player.getId()) return;

        switch (mode.getValue()) {
            case Plain:
            case Grim:
            case None:
                // 直接取消：在 mixin 中处理
                break;
            case Wall:
                // 减少水平速度
                break;
        }
    }

    /** 处理爆炸包 */
    public void onExplosionPacket(ExplosionS2CPacket packet) {
        if (nullCheck()) return;
        // 完全取消爆炸击飞
    }

    /** 修改玩家速度（从 mixin 调用） */
    public void modifyVelocity(double x, double y, double z, double[] velocity) {
        switch (mode.getValue()) {
            case Plain:
            case Grim:
                velocity[0] = velocity[0] * (horizontal.getValue() / 100.0);
                velocity[1] = velocity[1] * (vertical.getValue() / 100.0);
                velocity[2] = velocity[2] * (horizontal.getValue() / 100.0);
                break;
            case Wall:
                velocity[1] = velocity[1] * (vertical.getValue() / 100.0);
                break;
            case None:
                break; // 不修改
        }
    }

    public enum Mode {
        Plain,      // 直接减%
        Grim,       // Grim 模式
        Wall,       // 只减水平
        None        // 不处理
    }
}
