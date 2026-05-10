package com.blockclient.mod.module.impl.player;

import com.blockclient.mod.Category;
import com.blockclient.mod.Module;
import com.blockclient.mod.setting.*;

import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * Freecam - 自由视角
 * 将相机从身体分离，自由移动观察
 */
public class Freecam extends Module {
    public static Freecam INSTANCE;

    public final SliderSetting speed = add(new SliderSetting("Speed", 1.0, 0.1, 5.0, 0.1));

    public double cameraX, cameraY, cameraZ;
    public float cameraYaw, cameraPitch;
    private double originalX, originalY, originalZ;
    private float originalYaw, originalPitch;

    public Freecam() {
        super("Freecam", "自由视角 - 相机与身体分离", Category.Player);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (nullCheck()) return;

        originalX = mc.player.getX();
        originalY = mc.player.getY();
        originalZ = mc.player.getZ();
        originalYaw = mc.player.getYaw();
        originalPitch = mc.player.getPitch();

        cameraX = originalX;
        cameraY = originalY;
        cameraZ = originalZ;
        cameraYaw = originalYaw;
        cameraPitch = originalPitch;

        mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
    }

    @Override
    public void onDisable() {
        mc.options.setPerspective(Perspective.FIRST_PERSON);

        if (mc.player != null) {
            mc.player.setPosition(originalX, originalY, originalZ);
            mc.player.setYaw(originalYaw);
            mc.player.setPitch(originalPitch);
            mc.player.headYaw = originalYaw;
            mc.player.bodyYaw = originalYaw;

            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(originalX, originalY, originalZ, true, false)
                );
            }
        }
    }

    @Override
    public void onTick() {
        if (nullCheck() || !isOn()) return;

        mc.player.setPosition(originalX, originalY, originalZ);
        mc.player.setVelocity(0, 0, 0);

        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.OnGroundOnly(true, false)
            );
        }

        double speedVal = speed.getValue() * 0.5;
        double yaw = Math.toRadians(mc.player.getYaw());
        double pitch = Math.toRadians(mc.player.getPitch());

        if (mc.options.forwardKey.isPressed()) {
            cameraX -= Math.sin(yaw) * Math.cos(pitch) * speedVal;
            cameraY += Math.sin(pitch) * speedVal;
            cameraZ += Math.cos(yaw) * Math.cos(pitch) * speedVal;
        }
        if (mc.options.backKey.isPressed()) {
            cameraX += Math.sin(yaw) * Math.cos(pitch) * speedVal;
            cameraY -= Math.sin(pitch) * speedVal;
            cameraZ -= Math.cos(yaw) * Math.cos(pitch) * speedVal;
        }
        if (mc.options.leftKey.isPressed()) {
            cameraX -= Math.cos(yaw) * speedVal;
            cameraZ -= Math.sin(yaw) * speedVal;
        }
        if (mc.options.rightKey.isPressed()) {
            cameraX += Math.cos(yaw) * speedVal;
            cameraZ += Math.sin(yaw) * speedVal;
        }
        if (mc.options.jumpKey.isPressed()) {
            cameraY += speedVal;
        }
        if (mc.options.sneakKey.isPressed()) {
            cameraY -= speedVal;
        }
    }
}
