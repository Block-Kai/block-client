package com.blockclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class MovementUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean isStatic() {
        Vec3d v = mc.player.getVelocity();
        return v.x == 0 && v.y == 0 && v.z == 0;
    }

    public static boolean isMoving() {
        var pi = mc.player.input.playerInput;
        return pi.forward() || pi.backward() || pi.left() || pi.right();
    }

    public static double getSpeed() {
        Vec3d v = mc.player.getVelocity();
        return Math.sqrt(v.x * v.x + v.z * v.z);
    }
}
