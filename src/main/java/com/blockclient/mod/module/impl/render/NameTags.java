package com.blockclient.mod.module.impl.render;

import com.blockclient.mod.Category;
import com.blockclient.mod.Module;
import com.blockclient.mod.setting.*;
import com.blockclient.util.*;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;

public class NameTags extends Module {
    public static NameTags INSTANCE;
    public final BooleanSetting health = add(new BooleanSetting("Health", true));
    public final SliderSetting scale = add(new SliderSetting("Scale", 1.0, 0.5, 3.0, 0.1));
    public final SliderSetting range = add(new SliderSetting("Range", 64, 10, 128, 1));

    public NameTags() {
        super("NameTags", "3D 名字标签", Category.Render);
        INSTANCE = this;
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (nullCheck()) return;
        double sq = range.getValue() * range.getValue();
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive() || mc.player.squaredDistanceTo(p) > sq) continue;
            Vec3d pos = MathUtil.getRenderPosition(p, tickDelta);
            String name = p.getName().getString();
            if (health.getValue()) {
                float hp = p.getHealth() / 2.0f;
                String color = hp > 6 ? "§a" : hp > 3 ? "§e" : "§c";
                name += " " + color + String.format("%.1f", hp);
            }
            Render3DUtil.drawText3D(name, new Vec3d(pos.x, pos.y + p.getHeight() + 0.5, pos.z),
                new Color(255, 255, 255));
        }
    }
}
