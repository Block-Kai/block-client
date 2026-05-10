package com.blockclient.ui;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

import net.minecraft.client.MinecraftClient;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * Manages the full ImGui lifecycle: context creation, GL3/GLFW backends,
 * per-frame begin/end, style configuration, and cleanup.
 *
 * Uses the exact same rendering stack as Meteor Client, Future, etc.
 */
public final class ImGuiManager {

    private static final ImGuiImplGl3 GL3 = new ImGuiImplGl3();
    private static final ImGuiImplGlfw GLFW_IMPL = new ImGuiImplGlfw();

    private static boolean initialized = false;

    private ImGuiManager() {
    }

    // ──────────────────────────────────────────────
    //  Initialization
    // ──────────────────────────────────────────────

    public static void initialize() {
        if (initialized) return;

        MinecraftClient client = MinecraftClient.getInstance();
        long window = client.getWindow().getHandle();

        // ── Create ImGui context ──
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null); // no ini file — we handle persistence ourselves later
        io.setConfigFlags(ImGuiConfigFlags.NoMouseCursorChange); // we manage cursor manually
        io.setDisplaySize(client.getWindow().getFramebufferWidth(),
                client.getWindow().getFramebufferHeight());

        // ── Init OpenGL renderer (MC 1.21 uses core profile → need "core") ──
        GL3.init("#version 150 core");

        // ── Init GLFW backend WITHOUT installing callbacks ──
        //    We manage input forwarding manually so we can chain with MC.
        GLFW_IMPL.init(window, false);

        // ── Apply visual style ──
        configureStyle();

        // ── Register cleanup on JVM shutdown ──
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            GL3.dispose();
            ImGui.destroyContext();
        }, "ImGui-Cleanup"));

        initialized = true;
    }

    /**
     * Applies the Block-Client dark-purple theme to ImGui.
     * Matches the Alien Pro / Future client aesthetic:
     * - Dark purple base with 85% opacity
     * - Thin #9D4EDD borders
     * - #FFFFFF text
     */
    private static void configureStyle() {
        ImGuiStyle style = ImGui.getStyle();

        // Core colours — deep purple palette
        style.setColor(ImGuiCol.WindowBg, 0.165f, 0.000f, 0.306f, 0.85f); // #2A004E at 85%
        style.setColor(ImGuiCol.Border, 0.616f, 0.306f, 0.867f, 1.00f); // #9D4EDD
        style.setColor(ImGuiCol.BorderShadow, 0.616f, 0.306f, 0.867f, 0.30f); // #9D4EDD glow
        style.setColor(ImGuiCol.TitleBg, 0.165f, 0.000f, 0.306f, 0.90f);
        style.setColor(ImGuiCol.TitleBgActive, 0.200f, 0.000f, 0.369f, 0.90f); // slightly lighter
        style.setColor(ImGuiCol.TitleBgCollapsed, 0.165f, 0.000f, 0.306f, 0.75f);
        style.setColor(ImGuiCol.Text, 1.00f, 1.00f, 1.00f, 1.00f); // #FFFFFF
        style.setColor(ImGuiCol.TextDisabled, 0.70f, 0.53f, 0.93f, 0.50f); // #B388FF muted
        style.setColor(ImGuiCol.Button, 0.165f, 0.000f, 0.306f, 0.60f);
        style.setColor(ImGuiCol.ButtonHovered, 0.200f, 0.000f, 0.369f, 0.80f);
        style.setColor(ImGuiCol.ButtonActive, 0.235f, 0.000f, 0.431f, 0.90f);
        style.setColor(ImGuiCol.FrameBg, 0.100f, 0.000f, 0.200f, 0.54f);
        style.setColor(ImGuiCol.FrameBgHovered, 0.165f, 0.000f, 0.306f, 0.68f);
        style.setColor(ImGuiCol.FrameBgActive, 0.200f, 0.000f, 0.369f, 0.82f);
        style.setColor(ImGuiCol.CheckMark, 0.616f, 0.306f, 0.867f, 1.00f); // #9D4EDD
        style.setColor(ImGuiCol.SliderGrab, 0.616f, 0.306f, 0.867f, 1.00f);
        style.setColor(ImGuiCol.SliderGrabActive, 0.700f, 0.400f, 1.000f, 1.00f);
        style.setColor(ImGuiCol.Header, 0.165f, 0.000f, 0.306f, 0.70f);
        style.setColor(ImGuiCol.HeaderHovered, 0.200f, 0.000f, 0.369f, 0.80f);
        style.setColor(ImGuiCol.HeaderActive, 0.235f, 0.000f, 0.431f, 0.90f);
        style.setColor(ImGuiCol.Separator, 0.616f, 0.306f, 0.867f, 0.50f);
        style.setColor(ImGuiCol.ResizeGrip, 0.616f, 0.306f, 0.867f, 0.25f);
        style.setColor(ImGuiCol.ResizeGripHovered, 0.616f, 0.306f, 0.867f, 0.67f);
        style.setColor(ImGuiCol.ResizeGripActive, 0.700f, 0.400f, 1.000f, 0.95f);
        style.setColor(ImGuiCol.ScrollbarBg, 0.050f, 0.000f, 0.100f, 0.53f);
        style.setColor(ImGuiCol.ScrollbarGrab, 0.165f, 0.000f, 0.306f, 0.70f);
        style.setColor(ImGuiCol.ScrollbarGrabHovered, 0.200f, 0.000f, 0.369f, 0.80f);
        style.setColor(ImGuiCol.ScrollbarGrabActive, 0.235f, 0.000f, 0.431f, 0.90f);
        style.setColor(ImGuiCol.PopupBg, 0.100f, 0.000f, 0.200f, 0.94f);

        // Spacing / sizing
        style.setWindowRounding(6.0f);
        style.setChildRounding(6.0f);
        style.setFrameRounding(4.0f);
        style.setGrabRounding(4.0f);
        style.setPopupRounding(4.0f);
        style.setScrollbarRounding(6.0f);

        // Window padding
        style.setWindowPadding(8.0f, 6.0f);
        style.setFramePadding(6.0f, 3.0f);
        style.setItemSpacing(8.0f, 4.0f);
        style.setItemInnerSpacing(4.0f, 4.0f);

        // Anti-aliasing for smooth lines
        style.setAntiAliasedLines(true);
        style.setAntiAliasedFill(true);
    }

    // ──────────────────────────────────────────────
    //  Per-frame begin / end
    // ──────────────────────────────────────────────

    public static void beginFrame() {
        if (!initialized) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ImGuiIO io = ImGui.getIO();

        // Keep display size in sync with the framebuffer (handles resize)
        io.setDisplaySize(
                client.getWindow().getFramebufferWidth(),
                client.getWindow().getFramebufferHeight());

        GLFW_IMPL.newFrame();
        ImGui.newFrame();
    }

    public static void endFrame() {
        if (!initialized) return;

        ImGui.render();

        // ── Save critical GL state before ImGui draws ──
        int[] prevViewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, prevViewport);
        boolean prevScissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        boolean prevBlend = GL11.glIsEnabled(GL11.GL_BLEND);
        int[] prevBlendSrc = new int[1];
        int[] prevBlendDst = new int[1];
        GL11.glGetIntegerv(GL14.GL_BLEND_SRC_RGB, prevBlendSrc);
        GL11.glGetIntegerv(GL14.GL_BLEND_DST_RGB, prevBlendDst);
        boolean prevDepth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean prevCull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        int[] prevProgram = new int[1];
        GL11.glGetIntegerv(GL20.GL_CURRENT_PROGRAM, prevProgram);
        int[] prevVAO = new int[1];
        GL11.glGetIntegerv(GL30.GL_VERTEX_ARRAY_BINDING, prevVAO);
        int[] prevTex2D = new int[1];
        GL11.glGetIntegerv(GL11.GL_TEXTURE_BINDING_2D, prevTex2D);

        // ── Render ImGui draw data ──
        GL3.renderDrawData(ImGui.getDrawData());

        // ── Restore critical GL state ──
        GL11.glViewport(prevViewport[0], prevViewport[1], prevViewport[2], prevViewport[3]);
        if (prevScissor) GL11.glEnable(GL11.GL_SCISSOR_TEST);
        else             GL11.glDisable(GL11.GL_SCISSOR_TEST);
        if (prevBlend) {
            GL11.glEnable(GL11.GL_BLEND);
            GL14.glBlendFuncSeparate(prevBlendSrc[0], prevBlendDst[0], GL11.GL_ONE, GL11.GL_ZERO);
        } else GL11.glDisable(GL11.GL_BLEND);
        if (prevDepth) GL11.glEnable(GL11.GL_DEPTH_TEST);
        else           GL11.glDisable(GL11.GL_DEPTH_TEST);
        if (prevCull) GL11.glEnable(GL11.GL_CULL_FACE);
        else          GL11.glDisable(GL11.GL_CULL_FACE);
        GL20.glUseProgram(prevProgram[0]);
        GL30.glBindVertexArray(prevVAO[0]);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, prevTex2D[0]);
    }

    // ──────────────────────────────────────────────
    //  Expose for external input forwarding
    // ──────────────────────────────────────────────

    public static boolean isInitialized() {
        return initialized;
    }

    public static long getWindowHandle() {
        return MinecraftClient.getInstance().getWindow().getHandle();
    }
}
