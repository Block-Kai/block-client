package com.blockclient.util;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * 旋转计算工具
 */
public class RotationUtil {

    /** 获取朝向某一点的旋转角度 */
    public static float[] getRotationTo(Vec3d from, Vec3d to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double dist = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

        return new float[]{
            MathHelper.wrapDegrees(yaw),
            MathHelper.wrapDegrees(pitch)
        };
    }

    /** 获取水平角度差 */
    public static float getYawTo(Vec3d from, Vec3d to) {
        return getRotationTo(from, to)[0];
    }

    /** 获取垂直角度差 */
    public static float getPitchTo(Vec3d from, Vec3d to) {
        return getRotationTo(from, to)[1];
    }
}
