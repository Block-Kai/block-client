package com.blockclient.mixin;

import com.blockclient.mod.manager.ModuleManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onEntityVelocityUpdate", at = @At("HEAD"), cancellable = true)
    private void onEntityVelocityUpdate(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        // Velocity 模块使用自己的 mixin 逻辑
    }

    @Inject(method = "onExplosion", at = @At("HEAD"), cancellable = true)
    private void onExplosion(ExplosionS2CPacket packet, CallbackInfo ci) {
        // Velocity 模块使用自己的 mixin 逻辑
    }

    @Inject(method = "onGameStateChange", at = @At("HEAD"), cancellable = true)
    private void onGameState(net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket packet, CallbackInfo ci) {
        if (ModuleManager.onPacketReceive(packet)) ci.cancel();
    }
}
