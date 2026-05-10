package com.blockclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 数学工具
 */
public class MathUtil {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static float clamp(float num, float min, float max) {
        return num < min ? min : Math.min(num, max);
    }

    public static double clamp(double value, double min, double max) {
        if (value < min) return min;
        return Math.min(value, max);
    }

    public static double round(double value, int places) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double square(double input) {
        return input * input;
    }

    public static double interpolate(double previous, double current, double delta) {
        return previous + (current - previous) * delta;
    }

    public static float interpolate(float previous, float current, float delta) {
        return previous + (current - previous) * delta;
    }

    /** 获取实体的插值位置 */
    public static Vec3d getRenderPosition(Entity entity, float tickDelta) {
        return new Vec3d(
            interpolate(entity.lastRenderX, entity.getX(), tickDelta),
            interpolate(entity.lastRenderY, entity.getY(), tickDelta),
            interpolate(entity.lastRenderZ, entity.getZ(), tickDelta)
        );
    }

    public static Vec3d getRenderPosition(Entity entity) {
        return getRenderPosition(entity, mc.getRenderTickCounter().getTickProgress(true));
    }

    /** 获取点到盒子的最近点 */
    public static Vec3d getClosestPointToBox(Vec3d pos, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        double closestX = Math.max(minX, Math.min(pos.x, maxX));
        double closestY = Math.max(minY, Math.min(pos.y, maxY));
        double closestZ = Math.max(minZ, Math.min(pos.z, maxZ));
        return new Vec3d(closestX, closestY, closestZ);
    }

    public static Vec3d getClosestPointToBox(Vec3d eyePos, Box boundingBox) {
        return getClosestPointToBox(eyePos, boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
    }

    public static Vec3d getClosestPoint(Entity entity) {
        return getClosestPointToBox(mc.player.getEyePos(), entity.getBoundingBox());
    }

    public static double getDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dX = x2 - x1;
        double dY = y2 - y1;
        double dZ = z2 - z1;
        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }
}
