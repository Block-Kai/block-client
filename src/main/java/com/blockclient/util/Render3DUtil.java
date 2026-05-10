package com.blockclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.Color;

/**
 * 3D 渲染工具 — 适配 1.21.11 + Sodium + ImmediatelyFast
 *
 * 不经过 VertexConsumerProvider（ImmediatelyFast 会拦截并导致格式不匹配崩溃），
 * 直接使用 Tessellator.begin() + RenderLayer.draw() 路径，
 * ImmediatelyFast 不拦截 RenderLayer.draw()。
 */
public class Render3DUtil {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    // ── 静态缓存的 RenderLayer ──
    private static RenderLayer _translucentLines;
    private static RenderLayer _translucentFill;

    private static RenderLayer LINES() {
        if (_translucentLines == null) {
            _translucentLines = RenderLayer.of("bc-tl-lines",
                RenderSetup.builder(RenderPipelines.LINES)
                    .translucent()
                    .build());
        }
        return _translucentLines;
    }

    private static RenderLayer FILL() {
        if (_translucentFill == null) {
            _translucentFill = RenderLayer.of("bc-tl-fill",
                RenderSetup.builder(RenderPipelines.DEBUG_FILLED_BOX)
                    .translucent()
                    .build());
        }
        return _translucentFill;
    }

    // ── 工具: 相机 → 世界偏移 ──
    private static Vec3d camOffset() {
        Camera cam = mc.gameRenderer.getCamera();
        return cam != null ? cam.getCameraPos() : Vec3d.ZERO;
    }

    // ── 3D 文字 ──
    public static void drawText3D(String text, Vec3d pos, Color color) {
        drawText3D(text, pos.x, pos.y, pos.z, 0.0, 0.0, 1.0, color.getRGB());
    }

    public static void drawText3D(String text, Vec3d pos, int color) {
        drawText3D(text, pos.x, pos.y, pos.z, 0.0, 0.0, 1.0, color);
    }

    public static void drawText3D(String text, double x, double y, double z,
                                   double offX, double offY, double scale, int color) {
        Camera camera = mc.gameRenderer.getCamera();
        if (camera == null) return;

        Vec3d camPos = camera.getCameraPos();
        MatrixStack matrices = new MatrixStack();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));
        matrices.translate(x - camPos.x, y - camPos.y, z - camPos.z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.translate(offX, offY, 0.0);
        matrices.scale(-0.025f * (float) scale, -0.025f * (float) scale, 1.0f);

        int halfWidth = mc.textRenderer.getWidth(text) / 2;
        VertexConsumerProvider.Immediate imm = VertexConsumerProvider.immediate(new BufferAllocator(1536));
        mc.textRenderer.draw(text, (float) (-halfWidth), 0.0f, -1, true,
            matrices.peek().getPositionMatrix(), imm, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
        imm.draw();
        mc.textRenderer.draw(text, (float) (-halfWidth), 0.0f, color, false,
            matrices.peek().getPositionMatrix(), imm, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
        imm.draw();
    }

    // ── 3D 方框 (fill + outline) ──
    public static void draw3DBox(MatrixStack stack, Box bb, Color fillColor, Color outlineColor) {
        draw3DBox(stack, bb, fillColor, outlineColor, true, true);
    }

    public static void draw3DBox(MatrixStack stack, Box bb, Color fillColor,
                                  Color outlineColor, boolean outline, boolean fill) {
        draw3DBox(stack, bb, fillColor, outlineColor, outline, fill, 1.5f);
    }

    public static void draw3DBox(MatrixStack stack, Box bb, Color fillColor,
                                  Color outlineColor, boolean outline, boolean fill, float lineWidth) {
        Vec3d camPos = camOffset();
        bb = bb.offset(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f matrix = stack.peek().getPositionMatrix();

        // ── 线框 ──
        if (outline && outlineColor.getAlpha() > 0) {
            float a = outlineColor.getAlpha() / 255.0f;
            float r = outlineColor.getRed() / 255.0f;
            float g = outlineColor.getGreen() / 255.0f;
            float b = outlineColor.getBlue() / 255.0f;

            BufferBuilder buf = Tessellator.getInstance().begin(
                VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            drawBoxLines(buf, matrix, bb, r, g, b, a);
            LINES().draw(buf.end());
        }

        // ── 填充 ──
        if (fill && fillColor.getAlpha() > 0) {
            float a = fillColor.getAlpha() / 255.0f;
            float r = fillColor.getRed() / 255.0f;
            float g = fillColor.getGreen() / 255.0f;
            float b = fillColor.getBlue() / 255.0f;

            BufferBuilder buf = Tessellator.getInstance().begin(
                VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            drawBoxQuads(buf, matrix, bb, r, g, b, a);
            FILL().draw(buf.end());
        }
    }

    // ── 3D 线条 ──
    public static void drawLine(Vec3d start, Vec3d end, Color color) {
        drawLine(start, end, color, 1.0f);
    }

    public static void drawLine(Vec3d start, Vec3d end, Color color, float width) {
        Camera camera = mc.gameRenderer.getCamera();
        if (camera == null) return;

        MatrixStack matrices = new MatrixStack();
        Vec3d camPos = camera.getCameraPos();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));
        matrices.translate(start.x - camPos.x, start.y - camPos.y, start.z - camPos.z);

        Matrix4f m = matrices.peek().getPositionMatrix();
        float r = color.getRed() / 255.0f, g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f, a = color.getAlpha() / 255.0f;

        BufferBuilder buf = Tessellator.getInstance().begin(
            VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        buf.vertex(m, 0, 0, 0).color(r, g, b, a);
        buf.vertex(m, (float)(end.x - start.x), (float)(end.y - start.y), (float)(end.z - start.z)).color(r, g, b, a);
        LINES().draw(buf.end());
    }

    // ── 线框顶点 ──
    private static void drawBoxLines(BufferBuilder buf, Matrix4f m, Box bb,
                                      float r, float g, float b, float a) {
        float minX = (float) bb.minX, minY = (float) bb.minY, minZ = (float) bb.minZ;
        float maxX = (float) bb.maxX, maxY = (float) bb.maxY, maxZ = (float) bb.maxZ;
        // bottom
        buf.vertex(m, minX, minY, minZ).color(r, g, b, a);
        buf.vertex(m, maxX, minY, minZ).color(r, g, b, a);
        buf.vertex(m, maxX, minY, minZ).color(r, g, b, a);
        buf.vertex(m, maxX, minY, maxZ).color(r, g, b, a);
        buf.vertex(m, maxX, minY, maxZ).color(r, g, b, a);
        buf.vertex(m, minX, minY, maxZ).color(r, g, b, a);
        buf.vertex(m, minX, minY, maxZ).color(r, g, b, a);
        buf.vertex(m, minX, minY, minZ).color(r, g, b, a);
        // vertical
        buf.vertex(m, minX, minY, minZ).color(r, g, b, a);
        buf.vertex(m, minX, maxY, minZ).color(r, g, b, a);
        buf.vertex(m, maxX, minY, minZ).color(r, g, b, a);
        buf.vertex(m, maxX, maxY, minZ).color(r, g, b, a);
        buf.vertex(m, maxX, minY, maxZ).color(r, g, b, a);
        buf.vertex(m, maxX, maxY, maxZ).color(r, g, b, a);
        buf.vertex(m, minX, minY, maxZ).color(r, g, b, a);
        buf.vertex(m, minX, maxY, maxZ).color(r, g, b, a);
        // top
        buf.vertex(m, minX, maxY, minZ).color(r, g, b, a);
        buf.vertex(m, maxX, maxY, minZ).color(r, g, b, a);
        buf.vertex(m, maxX, maxY, minZ).color(r, g, b, a);
        buf.vertex(m, maxX, maxY, maxZ).color(r, g, b, a);
        buf.vertex(m, maxX, maxY, maxZ).color(r, g, b, a);
        buf.vertex(m, minX, maxY, maxZ).color(r, g, b, a);
        buf.vertex(m, minX, maxY, maxZ).color(r, g, b, a);
        buf.vertex(m, minX, maxY, minZ).color(r, g, b, a);
    }

    // ── 填充面顶点 ──
    private static void drawBoxQuads(BufferBuilder buf, Matrix4f m, Box bb,
                                      float r, float g, float b, float a) {
        float minX = (float) bb.minX, minY = (float) bb.minY, minZ = (float) bb.minZ;
        float maxX = (float) bb.maxX, maxY = (float) bb.maxY, maxZ = (float) bb.maxZ;
        // bottom
        buf.vertex(m, minX, minY, minZ).color(r, g, b, a);
        buf.vertex(m, maxX, minY, minZ).color(r, g, b, a);
        buf.vertex(m, maxX, minY, maxZ).color(r, g, b, a);
        buf.vertex(m, minX, minY, maxZ).color(r, g, b, a);
        // top
        buf.vertex(m, minX, maxY, minZ).color(r, g, b, a);
        buf.vertex(m, minX, maxY, maxZ).color(r, g, b, a);
        buf.vertex(m, maxX, maxY, maxZ).color(r, g, b, a);
        buf.vertex(m, maxX, maxY, minZ).color(r, g, b, a);
        // sides
        buf.vertex(m, minX, minY, minZ).color(r, g, b, a);
        buf.vertex(m, minX, maxY, minZ).color(r, g, b, a);
        buf.vertex(m, maxX, maxY, minZ).color(r, g, b, a);
        buf.vertex(m, maxX, minY, minZ).color(r, g, b, a);
        buf.vertex(m, maxX, minY, minZ).color(r, g, b, a);
        buf.vertex(m, maxX, maxY, minZ).color(r, g, b, a);
        buf.vertex(m, maxX, maxY, maxZ).color(r, g, b, a);
        buf.vertex(m, maxX, minY, maxZ).color(r, g, b, a);
        buf.vertex(m, minX, minY, maxZ).color(r, g, b, a);
        buf.vertex(m, maxX, minY, maxZ).color(r, g, b, a);
        buf.vertex(m, maxX, maxY, maxZ).color(r, g, b, a);
        buf.vertex(m, minX, maxY, maxZ).color(r, g, b, a);
        buf.vertex(m, minX, minY, minZ).color(r, g, b, a);
        buf.vertex(m, minX, minY, maxZ).color(r, g, b, a);
        buf.vertex(m, minX, maxY, maxZ).color(r, g, b, a);
        buf.vertex(m, minX, maxY, minZ).color(r, g, b, a);
    }
}
