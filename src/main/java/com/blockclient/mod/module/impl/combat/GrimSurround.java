package com.blockclient.mod.module.impl.combat;

import com.blockclient.mod.Category;
import com.blockclient.mod.Module;
import com.blockclient.mod.setting.*;
import com.blockclient.util.*;

import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class GrimSurround extends Module {
    public static GrimSurround INSTANCE;
    public final SliderSetting delay = add(new SliderSetting("Delay", 50, 0, 500).setSuffix("ms"));
    public final SliderSetting bpt = add(new SliderSetting("BPT", 4, 1, 8));
    public final BooleanSetting rotate = add(new BooleanSetting("Rotate", true));
    private final Timer timer = new Timer();

    public GrimSurround() {
        super("GrimSurround", "围脚放黑曜石防钻", Category.Combat);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if (nullCheck() || !timer.passedMs((long) delay.getValue())) return;

        BlockPos center = BlockPos.ofFloored(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        int placed = 0;
        Direction[] dirs = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

        for (Direction dir : dirs) {
            if (placed >= bpt.getValueInt()) break;
            BlockPos target = center.offset(dir);
            BlockPos below = target.down();
            if (!mc.world.getBlockState(target).isAir() && mc.world.getBlockState(target).getBlock() != net.minecraft.block.Blocks.FIRE) continue;
            if (mc.world.getBlockState(below).isAir()) continue;

            int obsidianSlot = findObsidian();
            if (obsidianSlot == -1) return;
            mc.player.getInventory().setSelectedSlot(obsidianSlot);
            InventoryUtil.syncSlot();

            if (rotate.getValue()) faceVector(new Vec3d(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5));

            Direction placeSide = null;
            for (Direction d : Direction.values()) {
                BlockPos neighbor = target.offset(d);
                if (!mc.world.getBlockState(neighbor).isAir() && mc.world.getBlockState(neighbor).isFullCube(mc.world, neighbor)) {
                    placeSide = d.getOpposite(); break;
                }
            }
            if (placeSide == null) placeSide = Direction.UP;
            Vec3d hitPos = target.toCenterPos().add(placeSide.getOffsetX() * 0.5, placeSide.getOffsetY() * 0.5, placeSide.getOffsetZ() * 0.5);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(hitPos, placeSide, target, false));
            mc.player.swingHand(Hand.MAIN_HAND);
            placed++;
        }
        timer.reset();
    }

    private int findObsidian() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.getItem() instanceof BlockItem bi && bi.getBlock() == Blocks.OBSIDIAN) return i;
        }
        return -1;
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
