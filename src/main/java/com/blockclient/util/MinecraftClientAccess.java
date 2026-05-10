package com.blockclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;

/**
 * Minecraft 客户端快捷访问
 */
public class MinecraftClientAccess {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static MinecraftClient get() {
        return mc;
    }

    public static ClientPlayerEntity getPlayer() {
        return mc.player;
    }

    public static ClientWorld getWorld() {
        return mc.world;
    }

    public static boolean nullCheck() {
        return mc.player == null || mc.world == null;
    }
}
