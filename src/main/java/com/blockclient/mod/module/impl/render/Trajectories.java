package com.blockclient.mod.module.impl.render;

import com.blockclient.mod.Category;
import com.blockclient.mod.Module;
import com.blockclient.mod.setting.*;
import com.blockclient.util.*;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.*;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Trajectories extends Module {
    public static Trajectories INSTANCE;
    public final ColorSetting lineColor = add(new ColorSetting("LineColor", new Color(255, 255, 255, 200)));
    public final SliderSetting steps = add(new SliderSetting("Steps", 30, 10, 100, 1));

    public Trajectories() {
        super("Trajectories", "投掷物轨迹", Category.Render);
        INSTANCE = this;
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (nullCheck()) return;
        ItemStack stack = mc.player.getMainHandStack();
        if (isThrowable(stack)) drawTrajectory(stack, 1.5, 0.03);
        else if (stack.getItem() instanceof BowItem && mc.options.attackKey.isPressed()) {
            float pull = mc.player.getItemUseTime();
            if (pull > 0) drawTrajectory(stack, 3.0 * pull, 0.05);
        }
    }

    private boolean isThrowable(ItemStack stack) {
        Item i = stack.getItem();
        return i instanceof SnowballItem || i instanceof EggItem || i instanceof EnderPearlItem
            || i instanceof ExperienceBottleItem || i instanceof SplashPotionItem || i instanceof LingeringPotionItem;
    }

    private void drawTrajectory(ItemStack stack, double velocity, double gravity) {
        Vec3d pos = mc.player.getEyePos();
        Vec3d look = mc.player.getRotationVec(1.0f);
        double vx = look.x * velocity, vy = look.y * velocity, vz = look.z * velocity;
        List<Vec3d> points = new ArrayList<>();
        for (int i = 0; i < steps.getValueInt(); i++) {
            points.add(pos);
            vx *= 0.99; vy = vy * 0.99 - gravity; vz *= 0.99;
            pos = pos.add(vx, vy, vz);
            if (pos.y < mc.world.getBottomY()) break;
        }
        for (int i = 0; i < points.size() - 1; i++) {
            Color c = ColorUtil.injectAlpha(lineColor.getValue(), (int)(255 * (1.0 - (double)i / points.size())));
            Render3DUtil.drawLine(points.get(i), points.get(i + 1), c);
        }
    }
}
