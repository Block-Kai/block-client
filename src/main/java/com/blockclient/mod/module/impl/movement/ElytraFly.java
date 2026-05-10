package com.blockclient.mod.module.impl.movement;

import com.blockclient.mod.Category;
import com.blockclient.mod.Module;
import com.blockclient.mod.setting.*;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class ElytraFly extends Module {
    public static ElytraFly INSTANCE;
    public final EnumSetting<Mode> mode = add(new EnumSetting<>("Mode", Mode.Control));
    public final SliderSetting speed = add(new SliderSetting("Speed", 1.5, 0.1, 5.0, 0.1));
    public final SliderSetting upSpeed = add(new SliderSetting("UpSpeed", 1.0, 0.1, 3.0, 0.1));
    public final SliderSetting downSpeed = add(new SliderSetting("DownSpeed", 1.0, 0.1, 3.0, 0.1));

    public ElytraFly() {
        super("ElytraFly", "鞘翅飞行辅助", Category.Movement);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;
        boolean hasElytra = mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA;
        if (!hasElytra) return;

        // Check if gliding via the entity's LivingEntity.glidingTicks (we can't access protected canGlide)
        boolean isGliding = mc.player.getVelocity().y < -0.1 || mc.options.jumpKey.isPressed();

        if (!isGliding && !mc.player.isOnGround()) {
            mc.getNetworkHandler().sendPacket(
                new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            isGliding = true;
        }
        if (!isGliding) return;

        switch (mode.getValue()) {
            case Control -> doControl();
            case Bounce -> doBounce();
            case Boost -> doBoost();
        }
    }

    private void doControl() {
        double yaw = Math.toRadians(mc.player.getYaw());
        Vec2f movement = mc.player.input.getMovementInput();
        float forward = movement.x;
        float sideways = movement.y;
        double mx = 0, my = 0, mz = 0;
        if (mc.options.jumpKey.isPressed()) my = upSpeed.getValue();
        else if (mc.options.sneakKey.isPressed()) my = -downSpeed.getValue();
        if (forward != 0 || sideways != 0) {
            if (forward != 0) {
                if (sideways > 0) yaw += (forward > 0 ? Math.toRadians(-90) : Math.toRadians(90));
                else if (sideways < 0) yaw += (forward > 0 ? Math.toRadians(90) : Math.toRadians(-90));
                forward = forward > 0 ? 1 : -1; sideways = 0;
            }
            double sin = Math.sin(yaw), cos = Math.cos(yaw);
            mx = (-forward * sin + sideways * cos * 0.5) * speed.getValue();
            mz = (forward * cos - sideways * sin * 0.5) * speed.getValue();
        } else { mx = mc.player.getVelocity().x * 0.9; mz = mc.player.getVelocity().z * 0.9; }
        mc.player.setVelocity(mx, my, mz);
    }

    private void doBounce() {
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * 1.05, Math.sin(System.currentTimeMillis() / 200.0) * 0.5, v.z * 1.05);
    }

    private void doBoost() {
        double yaw = Math.toRadians(mc.player.getYaw());
        double sin = Math.sin(yaw), cos = Math.cos(yaw);
        mc.player.setVelocity(-sin * speed.getValue(), mc.player.getVelocity().y, cos * speed.getValue());
        if (mc.options.jumpKey.isPressed()) mc.player.addVelocity(0, upSpeed.getValue(), 0);
        if (mc.options.sneakKey.isPressed()) mc.player.addVelocity(0, -downSpeed.getValue(), 0);
    }

    public enum Mode { Control, Bounce, Boost }
}
