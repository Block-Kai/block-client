package com.blockclient.mod.module.impl.combat;

import com.blockclient.mod.Category;
import com.blockclient.mod.Module;
import com.blockclient.mod.setting.*;
import com.blockclient.util.*;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.util.Comparator;

public class Aura extends Module {
    public static Aura INSTANCE;

    public final SliderSetting range = add(new SliderSetting("Range", 4.5, 1.0, 7.0));
    public final SliderSetting wallRange = add(new SliderSetting("WallRange", 2.5, 1.0, 7.0));
    public final SliderSetting cooldown = add(new SliderSetting("Cooldown", 0.5, 0.0, 1.5, 0.05));
    public final SliderSetting hurtTime = add(new SliderSetting("HurtTime", 10, 0, 10, 1));
    public final BooleanSetting weaponOnly = add(new BooleanSetting("WeaponOnly", true));
    public final BooleanSetting players = add(new BooleanSetting("Players", true));
    public final BooleanSetting mobs = add(new BooleanSetting("Mobs", false));
    public final BooleanSetting animals = add(new BooleanSetting("Animals", false));
    public final BooleanSetting rotate = add(new BooleanSetting("Rotate", true));
    public final BooleanSetting renderESP = add(new BooleanSetting("Render", true));
    public final ColorSetting espColor = add(new ColorSetting("Color", new Color(255, 50, 50, 80)));
    public final ColorSetting outlineColor = add(new ColorSetting("Outline", new Color(255, 50, 50, 200)));

    public Entity target;
    private final Timer attackTimer = new Timer();

    public Aura() {
        super("Aura", "自动攻击附近目标", Category.Combat);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        target = findTarget(range.getValueFloat());
        if (target == null) return;
        if (!attackTimer.passedMs((long) (cooldown.getValue() * 1000))) return;
        if (weaponOnly.getValue() && !EntityUtil.isHoldingWeapon(mc.player)) return;
        if (target instanceof LivingEntity living && living.hurtTime > hurtTime.getValueInt()) return;

        if (rotate.getValue()) faceVector(MathUtil.getClosestPoint(target));

        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));
        mc.player.swingHand(Hand.MAIN_HAND);
        attackTimer.reset();
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (target == null || !renderESP.getValue()) return;
        Vec3d pos = MathUtil.getRenderPosition(target, tickDelta);
        Box box = target.getBoundingBox().offset(pos.subtract(new Vec3d(target.getX(), target.getY(), target.getZ())));
        Render3DUtil.draw3DBox(matrices, box, espColor.getValue(), outlineColor.getValue());
    }

    private Entity findTarget(double range) {
        return mc.world.getEntitiesByClass(Entity.class,
                mc.player.getBoundingBox().expand(range),
                e -> e != mc.player && e.isAlive() && isTargetValid(e) && e instanceof LivingEntity
            ).stream()
            .min(Comparator.comparingDouble(e -> mc.player.getEyePos().distanceTo(MathUtil.getClosestPoint(e))))
            .orElse(null);
    }

    private boolean isTargetValid(Entity entity) {
        if (entity instanceof PlayerEntity) return players.getValue();
        if (entity instanceof VillagerEntity || entity instanceof WanderingTraderEntity) return mobs.getValue();
        if (entity instanceof AnimalEntity) return animals.getValue();
        if (entity instanceof MobEntity || entity instanceof SlimeEntity) return mobs.getValue();
        return false;
    }

    private void faceVector(Vec3d vec) {
        double dx = vec.x - mc.player.getEyePos().x;
        double dy = vec.y - mc.player.getEyePos().y;
        double dz = vec.z - mc.player.getEyePos().z;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        mc.player.setYaw((float) Math.toDegrees(Math.atan2(dz, dx)) - 90);
        mc.player.setPitch((float) -Math.toDegrees(Math.atan2(dy, dist)));
        mc.player.headYaw = mc.player.getYaw();
        mc.player.bodyYaw = mc.player.getYaw();
    }
}
