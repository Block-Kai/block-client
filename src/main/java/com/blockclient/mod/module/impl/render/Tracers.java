package com.blockclient.mod.module.impl.render;

import com.blockclient.mod.Category;
import com.blockclient.mod.Module;
import com.blockclient.mod.setting.*;
import com.blockclient.util.*;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;

public class Tracers extends Module {
    public static Tracers INSTANCE;

    public final BooleanSetting players = add(new BooleanSetting("Players", true));
    public final BooleanSetting mobs = add(new BooleanSetting("Mobs", false));
    public final SliderSetting lineWidth = add(new SliderSetting("Width", 1.0, 0.5, 5.0, 0.5));
    public final ColorSetting color = add(new ColorSetting("Color", new Color(255, 100, 100, 200)));

    public Tracers() {
        super("Tracers", "3D 射线", Category.Render);
        INSTANCE = this;
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (nullCheck()) return;

        Vec3d playerPos = MathUtil.getRenderPosition(mc.player, tickDelta)
            .add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living) || !living.isAlive() || living == mc.player) continue;
            if (!(entity instanceof PlayerEntity) || !players.getValue()) continue;

            Vec3d targetPos = MathUtil.getRenderPosition(entity, tickDelta)
                .add(0, entity.getHeight() / 2, 0);

            Render3DUtil.drawLine(playerPos, targetPos, color.getValue());
        }
    }
}
