package com.blockclient.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;

public class BlockUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static BlockState getBlockState(BlockPos pos) { return mc.world.getBlockState(pos); }
    public static Block getBlock(BlockPos pos) { return getBlockState(pos).getBlock(); }

    public static Direction getClickSide(BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.offset(dir);
            BlockState state = mc.world.getBlockState(neighbor);
            if (!state.isAir() && state.isFullCube(mc.world, neighbor)) return dir.getOpposite();
        }
        return Direction.UP;
    }

    public static List<BlockPos> getSphere(float radius) {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos center = BlockPos.ofFloored(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        int r = (int) Math.ceil(radius);
        for (int x = -r; x <= r; x++)
            for (int y = -r; y <= r; y++)
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = center.add(x, y, z);
                    if (center.getSquaredDistance(pos) <= radius * radius) positions.add(pos);
                }
        return positions;
    }

    public static boolean hasEntity(BlockPos pos, boolean ignoreCrystal) {
        Box box = new Box(pos);
        for (Entity e : mc.world.getEntitiesByClass(Entity.class, box, e -> true)) {
            if (e == mc.player) continue;
            if (ignoreCrystal && e instanceof EndCrystalEntity) continue;
            return true;
        }
        return false;
    }

    public static boolean canPlace(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        if (!state.isAir() && state.getBlock() != Blocks.FIRE) return false;
        if (hasEntity(pos, false)) return false;
        return getClickSide(pos.down()) != null || mc.player.isSneaking();
    }

    public static boolean hasCrystal(BlockPos pos) {
        Box box = new Box(pos);
        return !mc.world.getEntitiesByClass(EndCrystalEntity.class, box, Entity::isAlive).isEmpty();
    }

    public static void clickBlock(BlockPos pos, Direction side, boolean within, Hand hand) {
        Vec3d hitPos = pos.toCenterPos().add(
            side.getOffsetX() * 0.5, side.getOffsetY() * 0.5, side.getOffsetZ() * 0.5);
        mc.interactionManager.interactBlock(mc.player, hand,
            new BlockHitResult(hitPos, side, pos, false));
        mc.player.swingHand(hand);
    }

    public static Hand getItemHand(Block targetBlock) {
        if (mc.player.getMainHandStack().getItem() instanceof BlockItem bi && bi.getBlock() == targetBlock)
            return Hand.MAIN_HAND;
        if (mc.player.getOffHandStack().getItem() instanceof BlockItem bi && bi.getBlock() == targetBlock)
            return Hand.OFF_HAND;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem bi && bi.getBlock() == targetBlock) {
                mc.player.getInventory().setSelectedSlot(i);
                if (mc.getNetworkHandler() != null)
                    mc.getNetworkHandler().sendPacket(new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(i));
                return Hand.MAIN_HAND;
            }
        }
        return null;
    }
}
