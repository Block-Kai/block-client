package com.blockclient.mod.module.impl.combat;

import com.blockclient.mod.Category;
import com.blockclient.mod.Module;
import com.blockclient.mod.setting.*;
import com.blockclient.util.*;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;

import java.awt.Color;
import java.util.Comparator;

/**
 * AutoCrystal - 自动放置/破坏水晶
 */
public class AutoCrystal extends Module {
    public static AutoCrystal INSTANCE;

    public final BooleanSetting place = add(new BooleanSetting("Place", true));
    public final SliderSetting placeRange = add(new SliderSetting("PlaceRange", 5.0, 1.0, 6.0, 0.1).setSuffix("m"));
    public final SliderSetting placeDelay = add(new SliderSetting("PlaceDelay", 100, 0, 500).setSuffix("ms"));
    public final BooleanSetting breakSetting = add(new BooleanSetting("Break", true));
    public final SliderSetting breakRange = add(new SliderSetting("BreakRange", 4.5, 1.0, 6.0, 0.1).setSuffix("m"));
    public final SliderSetting breakDelay = add(new SliderSetting("BreakDelay", 100, 0, 500).setSuffix("ms"));
    public final SliderSetting targetRange = add(new SliderSetting("TargetRange", 12.0, 1.0, 20.0).setSuffix("m"));
    public final SliderSetting minDamage = add(new SliderSetting("MinDamage", 5.0, 0.0, 36.0).setSuffix("dmg"));
    public final BooleanSetting rotate = add(new BooleanSetting("Rotate", true));
    public final BooleanSetting render = add(new BooleanSetting("Render", true));
    public final ColorSetting boxColor = add(new ColorSetting("BoxColor", new Color(255, 255, 255, 100)));
    public final ColorSetting outlineColor = add(new ColorSetting("OutlineColor", new Color(255, 255, 255, 200)));

    private final Timer placeTimer = new Timer();
    private final Timer breakTimer = new Timer();
    private BlockPos currentPlacePos;

    public AutoCrystal() {
        super("AutoCrystal", "自动放置和破坏末地水晶", Category.Combat);
        INSTANCE = this;
    }

    @Override
    public void onDisable() { currentPlacePos = null; }

    @Override
    public void onTick() {
        if (nullCheck()) return;
        boolean hasCrystal = mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL
            || mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
        PlayerEntity target = findBestTarget();
        if (target == null) return;

        if (breakSetting.getValue() && breakTimer.passedMs((long) breakDelay.getValue())) {
            EndCrystalEntity c = findBestCrystal();
            if (c != null) { doBreakCrystal(c); breakTimer.reset(); return; }
        }
        if (place.getValue() && hasCrystal && placeTimer.passedMs((long) placeDelay.getValue())) {
            BlockPos p = findBestPlacePos(target);
            if (p != null) { doPlaceCrystal(p); placeTimer.reset(); }
        }
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (!render.getValue() || currentPlacePos == null) return;
        Box box = new Box(currentPlacePos);
        double alpha = Math.min(1.0, (System.currentTimeMillis() % 2000) / 1000.0);
        Color c = ColorUtil.injectAlpha(boxColor.getValue(), (int) (boxColor.getValue().getAlpha() * alpha));
        Render3DUtil.draw3DBox(matrices, box, c, outlineColor.getValue());
    }

    private PlayerEntity findBestTarget() {
        double sq = targetRange.getValue() * targetRange.getValue();
        return mc.world.getPlayers().stream()
            .filter(p -> p != mc.player && p.isAlive() && mc.player.squaredDistanceTo(p) <= sq)
            .min(Comparator.comparingDouble(p -> mc.player.distanceTo(p))).orElse(null);
    }

    private EndCrystalEntity findBestCrystal() {
        return mc.world.getEntitiesByClass(EndCrystalEntity.class,
            mc.player.getBoundingBox().expand(breakRange.getValue()),
            e -> e.isAlive() && mc.player.distanceTo(e) <= breakRange.getValue())
            .stream().min(Comparator.comparingDouble(e -> mc.player.distanceTo(e))).orElse(null);
    }

    private void doBreakCrystal(EndCrystalEntity crystal) {
        if (rotate.getValue()) faceVector(new Vec3d(crystal.getX(), crystal.getY(), crystal.getZ()));
        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private BlockPos findBestPlacePos(PlayerEntity target) {
        float range = placeRange.getValueFloat();
        BlockPos center = BlockPos.ofFloored(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        int r = (int) Math.ceil(range);
        BlockPos best = null; double bestDist = Double.MAX_VALUE;
        for (int x = -r; x <= r; x++)
            for (int y = -r; y <= r; y++)
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = center.add(x, y, z);
                    if (center.getSquaredDistance(pos) > range*range) continue;
                    if (!canPlaceCrystal(pos)) continue;
                    double d = mc.player.getEyePos().distanceTo(pos.toCenterPos());
                    if (d < bestDist) { bestDist = d; best = pos; }
                }
        return best;
    }

    private boolean canPlaceCrystal(BlockPos pos) {
        BlockPos obsPos = pos.down();
        Block b = mc.world.getBlockState(obsPos).getBlock();
        if (b != Blocks.OBSIDIAN && b != Blocks.BEDROCK) return false;
        if (!mc.world.isAir(pos) || !mc.world.isAir(pos.up())) return false;
        return !BlockUtil.hasEntity(obsPos, true);
    }

    private void doPlaceCrystal(BlockPos pos) {
        if (rotate.getValue()) faceVector(pos.down().toCenterPos());
        Hand hand = mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL ? Hand.MAIN_HAND : Hand.OFF_HAND;
        Direction side = BlockUtil.getClickSide(pos.down());
        if (side == null) side = Direction.UP;
        BlockUtil.clickBlock(pos.down(), side, false, hand);
        currentPlacePos = pos;
    }

    private void faceVector(Vec3d vec) {
        double dx = vec.x - mc.player.getEyePos().x;
        double dy = vec.y - mc.player.getEyePos().y;
        double dz = vec.z - mc.player.getEyePos().z;
        double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
        mc.player.setYaw((float) Math.toDegrees(Math.atan2(dz, dx)) - 90);
        mc.player.setPitch((float) -Math.toDegrees(Math.atan2(dy, dist)));
        mc.player.headYaw = mc.player.getYaw();
    }
}
