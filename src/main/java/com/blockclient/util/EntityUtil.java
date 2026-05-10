package com.blockclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;

public class EntityUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean isHoldingWeapon(PlayerEntity player) {
        ItemStack main = player.getMainHandStack();
        Item item = main.getItem();
        return item == Items.DIAMOND_SWORD || item == Items.NETHERITE_SWORD
            || item == Items.IRON_SWORD || item == Items.STONE_SWORD
            || item == Items.WOODEN_SWORD || item == Items.GOLDEN_SWORD
            || item instanceof AxeItem || item instanceof TridentItem
            || item instanceof MaceItem;
    }

    public static void swingHand(Hand hand) { mc.player.swingHand(hand); }

    public static float getHealth(Entity entity) {
        if (entity instanceof LivingEntity living) {
            return living.getHealth() + living.getAbsorptionAmount();
        }
        return 0;
    }

    public static boolean isArmorLow(PlayerEntity player, int threshold) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            ItemStack stack = player.getEquippedStack(slot);
            if (stack.isEmpty()) continue;
            if (stack.getMaxDamage() - stack.getDamage() < threshold) return true;
        }
        return false;
    }

    public static int getDamagePercent(ItemStack stack) {
        if (stack.isEmpty() || stack.getMaxDamage() == 0) return 0;
        return (stack.getMaxDamage() - stack.getDamage()) * 100 / stack.getMaxDamage();
    }

    public static void syncInventory() {
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(
                new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(
                    mc.player.getInventory().getSelectedSlot()
                )
            );
        }
    }
}
