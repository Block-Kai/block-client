package com.blockclient.mod.module.impl.render;

import com.blockclient.mod.Category;
import com.blockclient.mod.Module;
import com.blockclient.mod.setting.*;
import com.blockclient.util.*;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;

public class ESP extends Module {
    public static ESP INSTANCE;

    public final BooleanSetting players = add(new BooleanSetting("Players", true));
    public final BooleanSetting mobs = add(new BooleanSetting("Mobs", false));
    public final ColorSetting fillColor = add(new ColorSetting("Fill", new Color(255, 255, 255, 30)));
    public final ColorSetting outlineColor = add(new ColorSetting("Outline", new Color(255, 255, 255, 150)));

    public ESP() {
        super("ESP", "实体透视", Category.Render);
        INSTANCE = this;
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (nullCheck()) return;
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living) || !living.isAlive() || living == mc.player) continue;
            if (!shouldRender(entity)) continue;

            Vec3d pos = MathUtil.getRenderPosition(entity, tickDelta);
            Box box = entity.getBoundingBox().offset(pos.subtract(new Vec3d(entity.getX(), entity.getY(), entity.getZ())));
            Render3DUtil.draw3DBox(matrices, box, fillColor.getValue(), outlineColor.getValue());
        }
    }

    private boolean shouldRender(Entity e) {
        return e instanceof PlayerEntity && players.getValue() || mobs.getValue();
    }
}
