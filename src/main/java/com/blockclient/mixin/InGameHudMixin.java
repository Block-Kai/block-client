package com.blockclient.mixin;

import com.blockclient.mod.module.impl.render.NoRender;
import com.blockclient.mod.manager.ModuleManager;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * InGameHud Mixin
 * 处理 NoRender 的 HUD 屏蔽
 */
@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderStatusEffectOverlay(CallbackInfo ci) {
        NoRender nr = ModuleManager.getModule(NoRender.class);
        if (nr != null && nr.shouldRemovePotionEffects()) {
            ci.cancel();
        }
    }
}
