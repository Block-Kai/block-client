package com.blockclient.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class HoleUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean isHole(BlockPos pos) {
        BlockPos[] surrounds = {pos.north(), pos.south(), pos.east(), pos.west()};
        Block downBlock = mc.world.getBlockState(pos.down()).getBlock();
        boolean validDown = downBlock == Blocks.OBSIDIAN || downBlock == Blocks.BEDROCK || downBlock == Blocks.CRYING_OBSIDIAN;
        if (!validDown) return false;
        for (BlockPos p : surrounds) {
            Block b = mc.world.getBlockState(p).getBlock();
            if (b != Blocks.OBSIDIAN && b != Blocks.BEDROCK && b != Blocks.CRYING_OBSIDIAN) return false;
        }
        return true;
    }

    public static BlockPos getPlayerHole() {
        BlockPos pos = BlockPos.ofFloored(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        if (isHole(pos)) return pos;
        if (isHole(pos.down())) return pos.down();
        return null;
    }
}
