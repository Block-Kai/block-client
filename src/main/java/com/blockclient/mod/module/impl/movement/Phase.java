package com.blockclient.mod.module.impl.movement;

import com.blockclient.mod.Category;
import com.blockclient.mod.Module;
import com.blockclient.mod.setting.*;
import com.blockclient.util.*;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Phase extends Module {
    public static Phase INSTANCE;
    public final SliderSetting speed = add(new SliderSetting("Speed", 1.0, 0.1, 3.0, 0.1));
    public final BooleanSetting onGround = add(new BooleanSetting("OnGround", true));

    public Phase() {
        super("Phase", "穿墙", Category.Movement);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        Vec3d pos = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        double yaw = Math.toRadians(mc.player.getYaw());
        Vec3d forward = new Vec3d(-Math.sin(yaw), 0, Math.cos(yaw)).normalize();
        Vec3d checkPos = pos.add(forward.multiply(0.5));

        var blockState = mc.world.getBlockState(BlockPos.ofFloored(checkPos.x, pos.y, checkPos.z));

        if (!blockState.isAir() && blockState.getBlock().getBlastResistance() < 6000) {
            double ps = speed.getValue() * 0.5;
            double nx = pos.x + forward.x * ps;
            double nz = pos.z + forward.z * ps;

            mc.player.setPosition(nx, pos.y, nz);
            mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.PositionAndOnGround(nx, pos.y, nz, onGround.getValue(), false));

            if (mc.world.getBlockState(BlockPos.ofFloored(pos.x, pos.y - 1, pos.z)).isAir()) {
                mc.player.setVelocity(mc.player.getVelocity().x, -0.5, mc.player.getVelocity().z);
            }
        }
    }
}
