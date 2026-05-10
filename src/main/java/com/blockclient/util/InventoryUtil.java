package com.blockclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

/**
 * 背包工具 - 1.21.4 compatible
 */
public class InventoryUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void switchToSlot(int slot) {
        if (slot >= 0 && slot <= 8) {
            mc.player.getInventory().setSelectedSlot(slot);
            syncSlot();
        }
    }

    public static void syncSlot() {
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(
                new UpdateSelectedSlotC2SPacket(mc.player.getInventory().getSelectedSlot())
            );
        }
    }

    public static int findItem(Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) return i;
        }
        return -1;
    }

    public static int findItemInventorySlot(Item item) {
        for (int i = 9; i < 36; i++)
            if (mc.player.getInventory().getStack(i).getItem() == item) return i;
        for (int i = 0; i < 9; i++)
            if (mc.player.getInventory().getStack(i).getItem() == item) return i;
        return -1;
    }

    /** 背包内交换物品 - 简化版 */
    public static void inventorySwap(int slot1, int slot2) {
        if (mc.getNetworkHandler() == null || mc.interactionManager == null) return;
        // 使用 pickblock 方式交换
        mc.player.getInventory().swapSlotWithHotbar(slot1);
    }
}
