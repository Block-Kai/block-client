package com.blockclient.input;

import com.blockclient.BlockClientMod;

import imgui.ImGui;
import imgui.ImGuiIO;

import net.minecraft.client.MinecraftClient;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;

/**
 * Manages keyboard / mouse input forwarding to ImGui,
 * and the Right-Shift toggle for opening / closing the UI.
 *
 * When the UI is open:
 * - Cursor is unlocked (GLFW_CURSOR_NORMAL) so ImGui can receive mouse events.
 * - Keyboard events go to ImGui first; only unhandled keys reach Minecraft.
 * - The game world keeps running — only player input is suppressed.
 *
 * When the UI is closed:
 * - Cursor is re-locked for normal gameplay.
 * - All input goes to Minecraft.
 */
public final class InputManager {

    // Original Minecraft GLFW callbacks (saved so we can chain)
    private static GLFWKeyCallbackI mcKeyCallback;
    private static GLFWCharCallbackI mcCharCallback;
    private static GLFWMouseButtonCallbackI mcMouseButtonCallback;
    private static GLFWCursorPosCallbackI mcCursorPosCallback;
    private static GLFWScrollCallbackI mcScrollCallback;

    private static boolean installed = false;

    // ── Saved cursor mode before UI was opened (for restore) ──
    private static int prevCursorMode = GLFW.GLFW_CURSOR_DISABLED;

    private InputManager() {
    }

    // ──────────────────────────────────────────────
    //  Initialization
    // ──────────────────────────────────────────────

    public static void initialize() {
        if (installed) return;

        long window = getWindow();

        // Snapshot Minecraft's current GLFW callbacks before we override them.
        mcKeyCallback = GLFW.glfwSetKeyCallback(window, null);
        mcCharCallback = GLFW.glfwSetCharCallback(window, null);
        mcMouseButtonCallback = GLFW.glfwSetMouseButtonCallback(window, null);
        mcCursorPosCallback = GLFW.glfwSetCursorPosCallback(window, null);
        mcScrollCallback = GLFW.glfwSetScrollCallback(window, null);

        // Install our chained callbacks.
        installKeyCallback(window);
        installCharCallback(window);
        installMouseButtonCallback(window);
        installCursorPosCallback(window);
        installScrollCallback(window);

        installed = true;
    }

    // ──────────────────────────────────────────────
    //  GLFW callback installations (chaining)
    // ──────────────────────────────────────────────

    private static void installKeyCallback(long window) {
        GLFW.glfwSetKeyCallback(window, (w, key, scancode, action, mods) -> {
            // Always update ImGuiIO modifier state
            forwardKeyToImGui(key, scancode, action, mods);

            // ── Toggle: Right-Shift (open/close), Escape (close only) ──
            if (action == GLFW.GLFW_PRESS) {
                if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
                    BlockClientMod.uiOpen = !BlockClientMod.uiOpen;
                    updateCursorMode(w, BlockClientMod.uiOpen);
                    return;
                }
                if (key == GLFW.GLFW_KEY_ESCAPE) {
                    if (BlockClientMod.uiOpen) {
                        BlockClientMod.uiOpen = false;
                        updateCursorMode(w, false);
                        return;
                    }
                    // When UI is closed, let Escape pass through to MC (pause menu, etc.)
                    forwardKeyToMC(w, key, scancode, action, mods);
                    return;
                }
            }

            // ── Routing ──
            if (BlockClientMod.uiOpen) {
                // UI open → block ALL gameplay input (no movement, no hotbar switching)
                // (toggle keys are already handled above)
            } else {
                // UI closed → normal Minecraft input
                forwardKeyToMC(w, key, scancode, action, mods);
            }
        });
    }

    private static void installCharCallback(long window) {
        GLFW.glfwSetCharCallback(window, (w, codepoint) -> {
            // Always update ImGui character input
            ImGui.getIO().addInputCharacter(codepoint);

            if (!BlockClientMod.uiOpen || !ImGui.getIO().getWantTextInput()) {
                if (mcCharCallback != null) {
                    mcCharCallback.invoke(w, codepoint);
                }
            }
        });
    }

    private static void installMouseButtonCallback(long window) {
        GLFW.glfwSetMouseButtonCallback(window, (w, button, action, mods) -> {
            if (!BlockClientMod.uiOpen) {
                // UI closed → normal Minecraft input
                forwardMouseButtonToMC(w, button, action, mods);
            }
            // UI open → block all mouse clicks (no block breaking / attacking)
            // newFrame() polls mouse buttons for ImGuiIO each frame.
        });
    }

    private static void installCursorPosCallback(long window) {
        GLFW.glfwSetCursorPosCallback(window, (w, xpos, ypos) -> {
            if (!BlockClientMod.uiOpen) {
                // UI closed → normal camera rotation
                if (mcCursorPosCallback != null) {
                    mcCursorPosCallback.invoke(w, xpos, ypos);
                }
            }
            // UI open → block camera rotation (mouse is unlocked, show cursor only)
            // ImGui gets cursor position via GLFW polling in newFrame().
        });
    }

    private static void installScrollCallback(long window) {
        GLFW.glfwSetScrollCallback(window, (w, xoffset, yoffset) -> {
            // Update ImGui scroll (event-based, not polled by newFrame())
            ImGuiIO io = ImGui.getIO();
            io.setMouseWheelH(io.getMouseWheelH() + (float) xoffset);
            io.setMouseWheel(io.getMouseWheel() + (float) yoffset);

            if (!BlockClientMod.uiOpen || !io.getWantCaptureMouse()) {
                if (mcScrollCallback != null) {
                    mcScrollCallback.invoke(w, xoffset, yoffset);
                }
            }
        });
    }

    // ──────────────────────────────────────────────
    //  Forwarding helpers
    // ──────────────────────────────────────────────

    private static void forwardKeyToMC(long window, int key, int scancode, int action, int mods) {
        if (mcKeyCallback != null) {
            mcKeyCallback.invoke(window, key, scancode, action, mods);
        }
    }

    private static void forwardMouseButtonToMC(long window, int button, int action, int mods) {
        if (mcMouseButtonCallback != null) {
            mcMouseButtonCallback.invoke(window, button, action, mods);
        }
    }

    /**
     * Forwards modifier-key state to ImGuiIO so ImGui knows about Ctrl/Shift/Alt.
     * Full GLFW-to-ImGui key mapping is deferred until interactive elements are added.
     */
    private static void forwardKeyToImGui(int glfwKey, int scancode, int action, int mods) {
        ImGuiIO io = ImGui.getIO();
        io.setKeyCtrl((mods & GLFW.GLFW_MOD_CONTROL) != 0);
        io.setKeyShift((mods & GLFW.GLFW_MOD_SHIFT) != 0);
        io.setKeyAlt((mods & GLFW.GLFW_MOD_ALT) != 0);
        io.setKeySuper((mods & GLFW.GLFW_MOD_SUPER) != 0);
    }
    // ──────────────────────────────────────────────
    //  Cursor mode
    // ──────────────────────────────────────────────

    private static void updateCursorMode(long window, boolean uiOpen) {
        if (uiOpen) {
            // Save the current cursor mode before unlocking
            prevCursorMode = GLFW.glfwGetInputMode(window, GLFW.GLFW_CURSOR);
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        } else {
            // Only recentre cursor when returning to gameplay (cursor was disabled)
            // Skip recentring on title screen where cursor stays normal.
            if (prevCursorMode == GLFW.GLFW_CURSOR_DISABLED) {
                var client = MinecraftClient.getInstance();
                double cx = client.getWindow().getWidth() / 2.0;
                double cy = client.getWindow().getHeight() / 2.0;
                GLFW.glfwSetCursorPos(window, cx, cy);
            }

            // Restore the previous cursor mode
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, prevCursorMode);
        }
    }

    // ──────────────────────────────────────────────
    //  Utility
    // ──────────────────────────────────────────────

    private static long getWindow() {
        return MinecraftClient.getInstance().getWindow().getHandle();
    }

    public static boolean isInstalled() {
        return installed;
    }
}
