package com.blockclient.mod.module.impl.render;

import com.blockclient.mod.Category;
import com.blockclient.mod.Module;
import com.blockclient.mod.setting.*;
import com.blockclient.util.*;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.util.concurrent.ConcurrentHashMap;

public class LogoutSpots extends Module {
    public static LogoutSpots INSTANCE;
    private final ConcurrentHashMap<String, LogoutSpot> spots = new ConcurrentHashMap<>();
    public final SliderSetting range = add(new SliderSetting("Range", 128, 16, 512, 16));
    public final ColorSetting color = add(new ColorSetting("Color", new Color(255, 50, 50, 100)));
    public final ColorSetting outlineColor = add(new ColorSetting("Outline", new Color(255, 50, 50, 200)));

    public LogoutSpots() {
        super("LogoutSpots", "下线记录", Category.Render);
        INSTANCE = this;
    }

    public void onPlayerLogout(String name, Vec3d pos) {
        spots.put(name, new LogoutSpot(name, pos, System.currentTimeMillis()));
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        long now = System.currentTimeMillis();
        spots.entrySet().removeIf(e -> now - e.getValue().time > 30000);
        for (LogoutSpot s : spots.values()) {
            if (mc.player.squaredDistanceTo(s.pos) > range.getValue() * range.getValue()) continue;
            Box box = new Box(s.pos, s.pos.add(1, 2, 1));
            Render3DUtil.draw3DBox(matrices, box, color.getValue(), outlineColor.getValue());
            Render3DUtil.drawText3D("§c" + s.name + " §7(logged out)",
                new Vec3d(s.pos.x + 0.5, s.pos.y + 2.5, s.pos.z + 0.5), new Color(255, 255, 255));
        }
    }

    private record LogoutSpot(String name, Vec3d pos, long time) {}
}
