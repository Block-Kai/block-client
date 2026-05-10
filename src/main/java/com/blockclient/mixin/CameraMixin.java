package com.blockclient.mixin;

import com.blockclient.mod.module.impl.player.Freecam;
import com.blockclient.mod.module.impl.render.CameraClip;
import com.blockclient.mod.module.impl.render.MotionCamera;
import com.blockclient.mod.manager.ModuleManager;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    protected void setRotation(float yaw, float pitch) {}

    @Shadow
    protected void setPos(double x, double y, double z) {}

    @Inject(method = "update", at = @At("TAIL"))
    private void onUpdate(World world, Entity focused, boolean thirdPerson, boolean inverse, float tickDelta, CallbackInfo ci) {
        Freecam freecam = ModuleManager.getModule(Freecam.class);
        if (freecam != null && freecam.isOn()) {
            setPos(freecam.cameraX, freecam.cameraY, freecam.cameraZ);
            setRotation(freecam.cameraYaw, freecam.cameraPitch);
        }
    }

    @Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
    private void onClipToSpace(float desiredDistance, CallbackInfoReturnable<Double> cir) {
        CameraClip clip = ModuleManager.getModule(CameraClip.class);
        if (clip != null && clip.shouldClip()) {
            cir.setReturnValue(clip.getCameraDistance());
        }
    }

    @Inject(method = "getYaw", at = @At("RETURN"), cancellable = true)
    private void onGetYaw(CallbackInfoReturnable<Double> cir) {
        MotionCamera mc = ModuleManager.getModule(MotionCamera.class);
        if (mc != null && mc.isOn()) cir.setReturnValue(cir.getReturnValue() + mc.getOffsetX());
    }

    @Inject(method = "getPitch", at = @At("RETURN"), cancellable = true)
    private void onGetPitch(CallbackInfoReturnable<Double> cir) {
        MotionCamera mc = ModuleManager.getModule(MotionCamera.class);
        if (mc != null && mc.isOn()) cir.setReturnValue(cir.getReturnValue() + mc.getOffsetY());
    }
}
