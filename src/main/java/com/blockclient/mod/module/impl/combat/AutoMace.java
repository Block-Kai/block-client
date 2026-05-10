package com.blockclient.mod.module.impl.combat;

import com.blockclient.mod.Category;
import com.blockclient.mod.Module;
import com.blockclient.mod.setting.*;
import com.blockclient.util.*;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

/**
 * AutoMace - 自动狼牙棒攻击
 */
public class AutoMace extends Module {
    public static AutoMace INSTANCE;

    public final SliderSetting targetRange = add(new SliderSetting("TargetRange", 10.0, 1.0, 30.0, 0.5).setSuffix("m"));
    public final SliderSetting attackRange = add(new SliderSetting("AttackRange", 5.0, 1.0, 7.0, 0.1).setSuffix("m"));
    public final BooleanSetting autoSwitch = add(new BooleanSetting("AutoSwitch", true));
    public final BooleanSetting rotate = add(new BooleanSetting("Rotate", true));

    private PlayerEntity target;
    private final Timer timer = new Timer();

    public AutoMace() {
        super("AutoMace", "自动使用狼牙棒攻击", Category.Combat);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;
        target = findTarget();
        if (target == null || mc.player.distanceTo(target) > attackRange.getValue()) return;
        if (!timer.passedMs(500)) return;

        int maceSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.MACE) { maceSlot = i; break; }
        }
        if (maceSlot == -1) return;

        if (autoSwitch.getValue()) {
            mc.player.getInventory().setSelectedSlot(maceSlot);
            InventoryUtil.syncSlot();
        }
        if (rotate.getValue()) faceVector(MathUtil.getClosestPoint(target));

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        timer.reset();
    }

    private PlayerEntity findTarget() {
        double rangeSq = targetRange.getValue() * targetRange.getValue();
        PlayerEntity closest = null;
        double minDist = Double.MAX_VALUE;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive() || p.squaredDistanceTo(mc.player) > rangeSq) continue;
            double d = mc.player.distanceTo(p);
            if (d < minDist) { minDist = d; closest = p; }
        }
        return closest;
    }

    private void faceVector(Vec3d vec) {
        double dx = vec.x - mc.player.getEyePos().x;
        double dy = vec.y - mc.player.getEyePos().y;
        double dz = vec.z - mc.player.getEyePos().z;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        mc.player.setYaw((float) Math.toDegrees(Math.atan2(dz, dx)) - 90);
        mc.player.setPitch((float) -Math.toDegrees(Math.atan2(dy, dist)));
        mc.player.headYaw = mc.player.getYaw();
    }
}
