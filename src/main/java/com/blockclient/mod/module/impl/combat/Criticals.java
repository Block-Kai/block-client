package com.blockclient.mod.module.impl.combat;

import com.blockclient.mod.Category;
import com.blockclient.mod.Module;
import com.blockclient.mod.setting.*;
import com.blockclient.util.*;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class Criticals extends Module {
    public static Criticals INSTANCE;
    public final EnumSetting<Mode> mode = add(new EnumSetting<>("Mode", Mode.OldNCP));
    public final BooleanSetting onlyGround = add(new BooleanSetting("OnlyGround", true));
    private final BooleanSetting setOnGround = add(new BooleanSetting("SetNoGround", false));
    private final BooleanSetting blockCheck = add(new BooleanSetting("BlockCheck", true));
    private final BooleanSetting autoJump = add(new BooleanSetting("AutoJump", true));
    private final BooleanSetting mini = add(new BooleanSetting("Mini", true));
    private final SliderSetting y = add(new SliderSetting("MotionY", 0.05, 0.0, 1.0));
    private final BooleanSetting autoDisable = add(new BooleanSetting("AutoDisable", true));
    private final BooleanSetting crawlingDisable = add(new BooleanSetting("CrawlingDisable", true));
    private final BooleanSetting flight = add(new BooleanSetting("Flight", false));

    private final Timer attackTimer = new Timer();
    private boolean requireJump = true;

    public Criticals() {
        super("Criticals", "刀刀暴击 - 发送跳跃数据包实现每次攻击均为暴击", Category.Combat);
        INSTANCE = this;
        attackTimer.setMs(999999);
    }

    @Override
    public String getInfo() {
        return mode.getValue().name();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        requireJump = true;
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            if (autoDisable.getValue()) disable();
            return;
        }
        if (mode.is(Mode.Ground)) {
            if (MovementUtil.isMoving() && autoDisable.getValue()) {
                disable();
                return;
            }
            if (crawlingDisable.getValue() && mc.player.isCrawling()) {
                disable();
                return;
            }
            requestJump();
        }
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        if (mode.is(Mode.Ground)) {
            if (crawlingDisable.getValue() && mc.player.isCrawling()) {
                disable();
                return;
            }
            if (MovementUtil.isMoving() && autoDisable.getValue()) {
                disable();
                return;
            }
            if (flight.getValue() && mc.player.fallDistance > 0.0f) {
                mc.player.setVelocity(0, 0, 0);
                requireJump = false;
                return;
            }
            if (blockCheck.getValue() && !canCollideAt(BlockPos.ofFloored(mc.player.getX(), mc.player.getY(), mc.player.getZ()).up(2))) {
                requireJump = true;
                return;
            }
            if (mc.player.isOnGround() && autoJump.getValue() && (flight.getValue() || requireJump)) {
                doJump();
                requireJump = false;
            }
        }
    }

    @Override
    public boolean onPacketSend(Object packet) {
        if (nullCheck()) return false;

        if (mode.is(Mode.Ground) && packet instanceof PlayerMoveC2SPacket && setOnGround.getValue()) {
            var movePkt = (PlayerMoveC2SPacket) packet;
            return false; // Can't modify packets easily without mixin, just return false
        }

        if (!(packet instanceof PlayerInteractEntityC2SPacket interactPkt)) return false;

        PlayerInteractEntityC2SPacket.InteractType[] typeHolder = new PlayerInteractEntityC2SPacket.InteractType[1];
        interactPkt.handle(new PlayerInteractEntityC2SPacket.Handler() {
            @Override public void attack() { typeHolder[0] = PlayerInteractEntityC2SPacket.InteractType.ATTACK; }
            @Override public void interact(net.minecraft.util.Hand hand) { typeHolder[0] = PlayerInteractEntityC2SPacket.InteractType.INTERACT; }
            @Override public void interactAt(net.minecraft.util.Hand hand, net.minecraft.util.math.Vec3d pos) { typeHolder[0] = PlayerInteractEntityC2SPacket.InteractType.INTERACT_AT; }
        });

        if (typeHolder[0] != PlayerInteractEntityC2SPacket.InteractType.ATTACK) return false;

        Entity target = mc.world.getEntityById(interactPkt.getEntityId());
        if (target instanceof EndCrystalEntity || target == null) return false;

        if (onlyGround.getValue() && !mc.player.isOnGround() && !mc.player.getAbilities().flying) return false;
        if (mc.player.isInLava() || mc.player.isTouchingWater()) return false;

        doCrit(target);
        return false;
    }

    private void doCrit(Entity entity) {
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        var netHandler = mc.getNetworkHandler();
        if (netHandler == null) return;

        switch (mode.getValue()) {
            case UpdatedNCP -> {
                mc.player.addCritParticles(entity);
                netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 2.71875E-7, z, false));
                netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
            }
            case Strict -> {
                mc.player.addCritParticles(entity);
                netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.062600301692775, z, false));
                netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.07260029960661, z, false));
                netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
                netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
            }
            case NCP -> {
                mc.player.addCritParticles(entity);
                netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.0625, z, false));
                netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
            }
            case OldNCP -> {
                mc.player.addCritParticles(entity);
                netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 1.058293536E-5, z, false));
                netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 9.16580235E-6, z, false));
                netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 1.0371854E-7, z, false));
            }
            case Hypixel2K22 -> {
                mc.player.addCritParticles(entity);
                netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.0045, z, true));
                netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 1.52121E-4, z, false));
                netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.3, z, false));
                netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.025, z, false));
            }
            case Packet -> {
                mc.player.addCritParticles(entity);
                netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 5.0E-4, z, false));
                netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 1.0E-4, z, false));
            }
            case BBTT -> {
                if (MovementUtil.isMoving() || !MovementUtil.isStatic()) return;
                netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true));
                netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.0625, z, false));
                netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.045, z, false));
            }
            case Ground -> {
                // Handled in onTick
            }
            case Grim, GrimV3 -> {
                if (mc.player.isCrawling()) return;
                if (!mc.player.isOnGround()) return;

                float yaw = mc.player.getYaw();
                float pitch = mc.player.getPitch();

                if (mode.is(Mode.Grim)) {
                    netHandler.sendPacket(new PlayerMoveC2SPacket.Full(x, y + 0.0625, z, yaw, pitch, false));
                    netHandler.sendPacket(new PlayerMoveC2SPacket.Full(x, y, z, yaw, pitch, false));
                    netHandler.sendPacket(new PlayerMoveC2SPacket.Full(x, y + 1.0E-7, z, yaw, pitch, false));
                    netHandler.sendPacket(new PlayerMoveC2SPacket.Full(x, y, z, yaw, pitch, false));
                    mc.player.addCritParticles(entity);
                } else {
                    netHandler.sendPacket(new PlayerMoveC2SPacket.Full(x, y, z, yaw, pitch, true));
                    netHandler.sendPacket(new PlayerMoveC2SPacket.Full(x, y + 0.0625f, z, yaw, pitch, false));
                    netHandler.sendPacket(new PlayerMoveC2SPacket.Full(x, y + 0.04535f, z, yaw, pitch, false));
                    mc.player.addCritParticles(entity);
                }
            }
        }
    }

    private void doJump() {
        if (mini.getValue()) {
            mc.player.setVelocity(mc.player.getVelocity().x, y.getValue(), mc.player.getVelocity().z);
        } else {
            mc.player.jump();
        }
    }

    private void requestJump() {
        if (mc.player.isOnGround() && autoJump.getValue()
            && (!blockCheck.getValue() || canCollideAt(BlockPos.ofFloored(mc.player.getX(), mc.player.getY(), mc.player.getZ()).up(2)))) {
            doJump();
        }
    }

    private boolean canCollideAt(BlockPos pos) {
        Box box = new Box(pos);
        for (var e : mc.world.getEntitiesByClass(net.minecraft.entity.Entity.class, box, e -> true)) {
            if (e == mc.player) continue;
            return true;
        }
        return !mc.world.getBlockState(pos).isAir();
    }

    public enum Mode {
        UpdatedNCP, Strict, NCP, OldNCP,
        Hypixel2K22, Packet, Ground, BBTT,
        Grim, GrimV3
    }
}
