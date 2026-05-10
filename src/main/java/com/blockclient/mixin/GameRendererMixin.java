package com.blockclient.mixin;

import com.blockclient.BlockClientMod;
import com.blockclient.mod.manager.ModuleManager;
import com.blockclient.ui.ImGuiManager;
import com.blockclient.ui.PanelRenderer;
import com.blockclient.util.Render3DUtil;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * GameRenderer Mixin
 *
 * - renderWorld() TAIL → 3D 模块渲染（与 SunCat 一致）
 * - render() TAIL → ImGui overlay
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "renderWorld", at = @At("TAIL"))
    private void onRenderWorldTail(RenderTickCounter tickCounter, CallbackInfo ci) {
        if (com.blockclient.mod.Module.nullCheck()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        Camera camera = mc.gameRenderer.getCamera();

        // 构建相机矩阵（与 SunCat Client 一致）
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));

        // 分发 3D 渲染——Render3DUtil 使用 Tessellator + RenderLayer.draw()
        // 不经由 VCP，避免 ImmediatelyFast 拦截
        float td = mc.getRenderTickCounter().getTickProgress(true);
        ModuleManager.onRender3D(matrixStack, td);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderTail(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if (!ImGuiManager.isInitialized()) return;

        ImGuiManager.beginFrame();

        if (BlockClientMod.uiOpen) {
            PanelRenderer.render();
        }

        ImGuiManager.endFrame();
    }
}
