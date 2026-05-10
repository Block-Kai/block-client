package com.blockclient.mod.module.impl.combat;

import com.blockclient.mod.Category;
import com.blockclient.mod.Module;
import com.blockclient.mod.setting.*;
import com.blockclient.util.*;

import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.AxeItem;
import net.minecraft.item.MaceItem;

public class Offhand extends Module {
    public static Offhand INSTANCE;
    public final EnumSetting<Mode> mode = add(new EnumSetting<>("Mode", Mode.Totem));
    public final BooleanSetting swordGap = add(new BooleanSetting("SwordGap", true));
    private final Timer timer = new Timer();

    public Offhand() {
        super("Offhand", "自动切换副手物品", Category.Combat);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if (nullCheck() || !timer.passedMs(200)) return;

        ItemStack offhand = mc.player.getOffHandStack();
        if (isCorrectOffhand()) return;

        ItemStack target = getTargetItem();
        if (target == null || target.isEmpty()) return;

        for (int i = 9; i < 45; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == target.getItem()) {
                InventoryUtil.inventorySwap(i, 44);
                timer.reset();
                return;
            }
        }
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == target.getItem()) {
                InventoryUtil.inventorySwap(i, 44);
                timer.reset();
                return;
            }
        }
    }

    private boolean isCorrectOffhand() {
        ItemStack off = mc.player.getOffHandStack();
        return switch (mode.getValue()) {
            case Totem -> off.getItem() == Items.TOTEM_OF_UNDYING;
            case Crystal -> off.getItem() == Items.END_CRYSTAL;
            case EXP -> off.getItem() == Items.EXPERIENCE_BOTTLE;
            case Gap -> off.getItem() == Items.ENCHANTED_GOLDEN_APPLE || off.getItem() == Items.GOLDEN_APPLE;
        };
    }

    private ItemStack getTargetItem() {
        if (swordGap.getValue() && isWeaponHeld()) {
            for (int i = 0; i < 45; i++) {
                ItemStack s = mc.player.getInventory().getStack(i);
                if (s.getItem() == Items.ENCHANTED_GOLDEN_APPLE) return new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);
                if (s.getItem() == Items.GOLDEN_APPLE) return new ItemStack(Items.GOLDEN_APPLE);
            }
        }
        return switch (mode.getValue()) {
            case Totem -> new ItemStack(Items.TOTEM_OF_UNDYING);
            case Crystal -> new ItemStack(Items.END_CRYSTAL);
            case EXP -> new ItemStack(Items.EXPERIENCE_BOTTLE);
            case Gap -> new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);
        };
    }

    private boolean isWeaponHeld() {
        ItemStack main = mc.player.getMainHandStack();
        ItemStack off = mc.player.getOffHandStack();
        // Check mainhand and offhand for weapons
        return EntityUtil.isHoldingWeapon(mc.player);
    }

    public enum Mode { Totem, Crystal, EXP, Gap }
}
