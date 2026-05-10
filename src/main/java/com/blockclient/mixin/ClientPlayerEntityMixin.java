package com.blockclient.mixin;

import com.blockclient.mod.module.impl.player.Freecam;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * ClientPlayerEntity Mixin
 * 处理 Freecam 的输入抑制
 */
@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    /**
     * Freecam 启用时阻止玩家移动输入
     */
    @Inject(method = "tickMovement", at = @At("HEAD"), cancellable = true)
    private void onTickMovement(CallbackInfo ci) {
        Freecam freecam = com.blockclient.mod.manager.ModuleManager.getModule(Freecam.class);
        if (freecam != null && freecam.isOn()) {
            // 阻止身体移动
            ClientPlayerEntity self = (ClientPlayerEntity) (Object) this;
            self.setVelocity(0, 0, 0);
        }
    }

    /**
     * Freecam 启用时阻止发送位置更新包
     */
    @Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
    private void onSendMovementPackets(CallbackInfo ci) {
        Freecam freecam = com.blockclient.mod.manager.ModuleManager.getModule(Freecam.class);
        if (freecam != null && freecam.isOn()) {
            ci.cancel();
        }
    }

    /**
     * Freecam 启用时阻止 isCamera 返回 true（使相机使用自由位置）
     */
    @Inject(method = "isCamera", at = @At("RETURN"), cancellable = true)
    private void onIsCamera(CallbackInfoReturnable<Boolean> cir) {
        Freecam freecam = com.blockclient.mod.manager.ModuleManager.getModule(Freecam.class);
        if (freecam != null && freecam.isOn()) {
            cir.setReturnValue(false);
        }
    }
}
