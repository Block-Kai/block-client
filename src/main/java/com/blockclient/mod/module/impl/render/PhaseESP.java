package com.blockclient.mod.module.impl.render;

import com.blockclient.mod.Category;
import com.blockclient.mod.Module;
import com.blockclient.mod.setting.*;
import com.blockclient.util.*;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.awt.Color;

public class PhaseESP extends Module {
    public static PhaseESP INSTANCE;
    public final SliderSetting range = add(new SliderSetting("Range", 5, 1, 16, 1));
    public final SliderSetting height = add(new SliderSetting("Height", 3, 1, 8, 1));
    public final ColorSetting fillColor = add(new ColorSetting("Fill", new Color(255, 200, 0, 40)));
    public final ColorSetting outlineColor = add(new ColorSetting("Outline", new Color(255, 200, 0, 150)));

    public PhaseESP() {
        super("PhaseESP", "穿墙块高亮", Category.Render);
        INSTANCE = this;
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (nullCheck()) return;
        BlockPos center = BlockPos.ofFloored(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        int r = range.getValueInt(), h = height.getValueInt();
        Block[] targets = {Blocks.GLASS, Blocks.GLASS_PANE, Blocks.STONE, Blocks.COBBLESTONE,
            Blocks.STONE_BRICKS, Blocks.OAK_PLANKS, Blocks.DIRT, Blocks.GRASS_BLOCK};

        for (int x = -r; x <= r; x++)
            for (int y = -h; y <= h; y++)
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = center.add(x, y, z);
                    Block b = mc.world.getBlockState(pos).getBlock();
                    for (Block t : targets) {
                        if (b == t) {
                            Box box = new Box(pos);
                            Render3DUtil.draw3DBox(matrices, box, fillColor.getValue(), outlineColor.getValue());
                            break;
                        }
                    }
                }
    }
}
