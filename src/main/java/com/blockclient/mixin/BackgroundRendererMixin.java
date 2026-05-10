package com.blockclient.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * BackgroundRenderer Mixin (1.21.4 compatible)
 * Fog handling has moved, keeping this as a placeholder.
 */
@Mixin(targets = "net.minecraft.client.render.BackgroundRenderer")
public class BackgroundRendererMixin {
}
