package com.blockclient.ui;

import imgui.ImGui;
import imgui.ImDrawList;
import imgui.ImFont;
import imgui.ImVec2;
import imgui.flag.ImDrawFlags;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;

import net.minecraft.client.MinecraftClient;

import com.blockclient.BlockClientMod;
import com.blockclient.mod.Category;
import com.blockclient.mod.Module;
import com.blockclient.mod.manager.ModuleManager;
import com.blockclient.mod.setting.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

/**
 * Renders the Block-Client GUI overlay with ImGui.
 *
 * 7 panels: Combat / Misc / Render / Movement / Player / Exploit / Client
 * Each panel lists its category's modules as clickable toggle entries.
 * Left-click  → toggle module on/off
 * Right-click → open settings popup with detailed parameters + keybind
 */
public final class PanelRenderer {

    private static final String[] PANEL_TITLES = {
            "Combat", "Misc", "Render", "Movement", "Player", "Exploit", "Client"
    };
    private static final Category[] PANEL_CATEGORIES = {
            Category.Combat, Category.Misc, Category.Render,
            Category.Movement, Category.Player, Category.Exploit, Category.Client
    };
    private static final int PANEL_COUNT = PANEL_TITLES.length;

    // ── Layout ratios ──
    private static final float TOP_MARGIN_RATIO = 0.055f;
    private static final float PANEL_HEIGHT_RATIO = 0.62f;
    private static final float SIDE_MARGIN_RATIO = 0.025f;
    private static final float PANEL_GAP_RATIO = 0.010f;
    private static final float TITLE_BAR_HEIGHT_RATIO = 0.08f;
    private static final float CORNER_RADIUS = 6.0f;

    private static final float BRAND_FONT_BASE = 32.0f;
    private static final float TITLE_FONT_BASE = 18.0f;
    private static final float MODULE_FONT_BASE = 14.0f;

    // ── Mouse state ──
    private static boolean prevLeftDown = false;
    private static boolean leftClicked = false;
    private static boolean prevRightDown = false;
    private static boolean rightClicked = false;
    private static float mouseX = 0, mouseY = 0;
    private static boolean clickConsumed = false;

    // ── Settings popup state ──
    private static Module selectedModule = null;
    private static float rightClickX = 0, rightClickY = 0;

    // ── Keybind capture ──
    private static boolean isBindingKey = false;
    private static boolean[] prevKeyStates = null;

    // ── Reusable buffers to avoid GC pressure ──
    private static final float[] sliderBuffer = new float[1];
    private static final int[] intBuffer = new int[1];
    private static final float[] colorBuffer = new float[4];
    private static final ImString stringBuffer = new ImString("", 256);

    private PanelRenderer() {
    }

    // ──────────────────────────────────────────────
    //  Entry point (called from mixin each frame)
    // ──────────────────────────────────────────────

    public static void render() {
        pollMouse();
        pollKeys();
        draw();
        renderSettingsPopup();
    }

    // ──────────────────────────────────────────────
    //  GLFW input polling
    // ──────────────────────────────────────────────

    private static void pollMouse() {
        long window = MinecraftClient.getInstance().getWindow().getHandle();
        boolean nowLeftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean nowRightDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        leftClicked = nowLeftDown && !prevLeftDown;
        rightClicked = nowRightDown && !prevRightDown;
        prevLeftDown = nowLeftDown;
        prevRightDown = nowRightDown;
        clickConsumed = false;

        if (leftClicked || rightClicked || nowLeftDown || nowRightDown) {
            double[] x = new double[1], y = new double[1];
            GLFW.glfwGetCursorPos(window, x, y);
            mouseX = (float) x[0];
            mouseY = (float) y[0];
        }
    }

    /** Polls GLFW keys for keybind capture (only when isBindingKey is active) */
    private static void pollKeys() {
        if (!isBindingKey || selectedModule == null) return;

        long window = MinecraftClient.getInstance().getWindow().getHandle();
        if (prevKeyStates == null) {
            prevKeyStates = new boolean[GLFW.GLFW_KEY_LAST];
        }

        for (int key = 0; key < GLFW.GLFW_KEY_LAST; key++) {
            boolean nowDown = GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
            boolean wasDown = prevKeyStates[key];
            prevKeyStates[key] = nowDown;

            if (nowDown && !wasDown) {
                // Esc cancels binding
                if (key == GLFW.GLFW_KEY_ESCAPE) {
                    isBindingKey = false;
                    return;
                }
                selectedModule.getBindSetting().setValue(key);
                isBindingKey = false;
                return;
            }
        }
    }

    // ──────────────────────────────────────────────
    //  Main panel drawing (raw ImDrawList)
    // ──────────────────────────────────────────────

    private static void draw() {
        MinecraftClient client = MinecraftClient.getInstance();
        int ww = client.getWindow().getFramebufferWidth();
        int wh = client.getWindow().getFramebufferHeight();

        ImGui.setNextWindowPos(0, 0);
        ImGui.setNextWindowSize(ww, wh);

        int overlayFlags = ImGuiWindowFlags.NoTitleBar
                | ImGuiWindowFlags.NoResize
                | ImGuiWindowFlags.NoMove
                | ImGuiWindowFlags.NoScrollbar
                | ImGuiWindowFlags.NoScrollWithMouse
                | ImGuiWindowFlags.NoCollapse
                | ImGuiWindowFlags.NoNav
                | ImGuiWindowFlags.NoBackground
                | ImGuiWindowFlags.NoBringToFrontOnFocus;

        ImGui.begin("##blockclient-overlay", overlayFlags);

        ImDrawList dl = ImGui.getWindowDrawList();
        ImFont font = ImGui.getFont();

        // ── Layout ──
        float topMargin = wh * TOP_MARGIN_RATIO;
        float sideMargin = ww * SIDE_MARGIN_RATIO;
        float panelGap = ww * PANEL_GAP_RATIO;
        float panelHeight = wh * PANEL_HEIGHT_RATIO;
        float totalGaps = panelGap * (PANEL_COUNT - 1);
        float availableWidth = ww - sideMargin * 2.0f - totalGaps;
        float panelWidth = availableWidth / PANEL_COUNT;
        float titleBarHeight = panelHeight * TITLE_BAR_HEIGHT_RATIO;

        float brandFontSize = BRAND_FONT_BASE * (wh / 1080.0f);
        float titleFontSize = TITLE_FONT_BASE * (wh / 1080.0f);
        float moduleFontSize = MODULE_FONT_BASE * (wh / 1080.0f);

        // ── Branding ──
        float brandX = sideMargin;
        float brandY = topMargin * 0.35f;
        renderOutlinedText(dl, font, brandFontSize, brandX, brandY,
                0xFFFF88B3, 0xFFFFFFFF, BlockClientMod.CLIENT_NAME);

        // ── Panels ──
        for (int i = 0; i < PANEL_COUNT; i++) {
            float px = sideMargin + i * (panelWidth + panelGap);
            float py = topMargin;

            // Panel background
            dl.addRectFilled(px, py, px + panelWidth, py + panelHeight,
                    0xD94E002A, CORNER_RADIUS);
            dl.addRect(px, py, px + panelWidth, py + panelHeight,
                    0xFFDD4E9D, CORNER_RADIUS, 0, 1.0f);

            // Title bar
            float titleBottom = py + titleBarHeight;
            dl.addRectFilled(px, py, px + panelWidth, titleBottom,
                    0xE666003D, CORNER_RADIUS, ImDrawFlags.RoundCornersTop);
            dl.addLine(px + 4.0f, titleBottom,
                    px + panelWidth - 4.0f, titleBottom, 0xCCDD4E9D, 1.0f);

            // Title text
            String title = PANEL_TITLES[i];
            ImVec2 textSize = font.calcTextSizeA(titleFontSize, Float.MAX_VALUE, 0.0f, title);
            float textX = px + (panelWidth - textSize.x) / 2.0f;
            float textY = py + (titleBarHeight - textSize.y) / 2.0f;
            renderOutlinedText(dl, font, titleFontSize, textX, textY,
                    0xFFFF88B3, 0xFFFFFFFF, title);

            // ── Module list ──
            Category cat = PANEL_CATEGORIES[i];
            List<Module> panelModules = getModulesForCategory(cat);

            float itemStartY = titleBottom + 4.0f;
            float itemHeight = moduleFontSize + 6.0f;
            float itemPaddingX = 4.0f;

            int idx = 0;
            for (Module mod : panelModules) {
                float iy = itemStartY + idx * (itemHeight + 2.0f);
                if (iy + itemHeight > py + panelHeight - 4.0f) break;

                float ix = px + itemPaddingX;
                float iw = panelWidth - itemPaddingX * 2.0f;

                boolean isOn = mod.isOn();
                int bgColor = isOn ? 0x809D4EDD : 0x402A004E;
                int hoverBg = isOn ? 0x999D4EDD : 0x603A006A;

                // Check hover
                boolean hovered = mouseX >= ix && mouseX <= ix + iw
                        && mouseY >= iy && mouseY <= iy + itemHeight;

                dl.addRectFilled(ix, iy, ix + iw, iy + itemHeight,
                        hovered ? hoverBg : bgColor, 3.0f);

                // ── Left-click → toggle (only when no popup open) ──
                if (hovered && leftClicked && !clickConsumed && selectedModule == null) {
                    mod.toggle();
                    clickConsumed = true;
                }

                // ── Right-click → open settings popup ──
                if (hovered && rightClicked && !clickConsumed) {
                    selectedModule = mod;
                    rightClickX = mouseX;
                    rightClickY = mouseY;
                    isBindingKey = false;
                    clickConsumed = true;
                }

                // Module name
                String displayName = mod.getName();
                String info = mod.getInfo();
                String label = info != null ? displayName + " [" + info + "]" : displayName;

                int textColor = isOn ? 0xFFFFFFFF : 0xAAFFFFFF;

                ImVec2 modTextSize = font.calcTextSizeA(moduleFontSize, Float.MAX_VALUE, 0.0f, label);
                float mtx = ix + 4.0f;
                float mty = iy + (itemHeight - modTextSize.y) / 2.0f;
                dl.addText(font, moduleFontSize, mtx, mty, textColor, label);

                idx++;
            }
        }

        ImGui.end();
    }

    // ──────────────────────────────────────────────
    //  Settings popup (ImGui widgets)
    // ──────────────────────────────────────────────

    private static void renderSettingsPopup() {
        if (selectedModule == null) return;

        final String popupId = "##module_settings";

        // If the popup is not yet open, this is either:
        //   a) the first frame after right-click → open it now, or
        //   b) the user clicked outside last frame → clear state
        if (!ImGui.isPopupOpen(popupId)) {
            if (rightClicked) {
                // Right-click on a module happened this frame — open the popup
                ImGui.setNextWindowPos(rightClickX, rightClickY, ImGuiCond.Appearing);
                ImGui.setNextWindowSize(240.0f, 0.0f, ImGuiCond.Appearing);
                ImGui.openPopup(popupId);
            } else {
                // Popup was dismissed (click outside) and no new right-click — clean up
                selectedModule = null;
                isBindingKey = false;
                return;
            }
        }

        // ── Styling for popup ──
        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0.12f, 0.00f, 0.22f, 0.95f);
        ImGui.pushStyleColor(ImGuiCol.Border, 0.62f, 0.31f, 0.87f, 1.00f);
        ImGui.pushStyleColor(ImGuiCol.TitleBg, 0.35f, 0.00f, 0.15f, 0.90f);
        ImGui.pushStyleColor(ImGuiCol.TitleBgActive, 0.40f, 0.00f, 0.20f, 0.90f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 6.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 8.0f, 4.0f);

        if (ImGui.beginPopup(popupId)) {
            // ── Title ──
            ImGui.pushStyleColor(ImGuiCol.Text, 1.0f, 0.53f, 0.70f, 1.0f);
            ImGui.text(selectedModule.getName());
            ImGui.popStyleColor();

            String desc = selectedModule.getDescription();
            if (desc != null && !desc.isEmpty()) {
                ImGui.textDisabled(desc);
            }
            ImGui.separator();

            // ── Module on/off toggle ──
            ImBoolean enabled = new ImBoolean(selectedModule.isOn());
            if (ImGui.checkbox("Enabled", enabled)) {
                if (enabled.get()) {
                    selectedModule.enable();
                } else {
                    selectedModule.disable();
                }
            }

            ImGui.separator();

            // ── Settings ──
            List<Setting> settings = selectedModule.getSettings();
            for (Setting setting : settings) {
                if (!setting.isVisible()) continue;
                if (setting instanceof BindSetting) continue; // handled at bottom

                String name = setting.getName();

                if (setting instanceof BooleanSetting boolSetting) {
                    ImBoolean imBool = new ImBoolean(boolSetting.getValue());
                    if (ImGui.checkbox(name, imBool)) {
                        boolSetting.setValue(imBool.get());
                    }

                } else if (setting instanceof SliderSetting slider) {
                    renderSliderSetting(name, slider);

                } else if (setting instanceof EnumSetting<?> enumSetting) {
                    renderEnumSetting(name, enumSetting);

                } else if (setting instanceof ColorSetting colorSetting) {
                    renderColorSetting(name, colorSetting);

                } else if (setting instanceof StringSetting stringSetting) {
                    renderStringSetting(name, stringSetting);
                }
            }

            ImGui.separator();

            // ── Keybind section ──
            renderKeybindSection();

            ImGui.endPopup();
        }

        ImGui.popStyleVar(2);
        ImGui.popStyleColor(4);
    }

    // ──────────────────────────────────────────────
    //  Keybind capture UI
    // ──────────────────────────────────────────────

    private static void renderKeybindSection() {
        ImGui.text("Keybind:");

        BindSetting bind = selectedModule.getBindSetting();
        int keyCode = bind.getValue();

        if (isBindingKey) {
            ImGui.pushStyleColor(ImGuiCol.Button, 0.6f, 0.0f, 0.6f, 0.8f);
            ImGui.button("Press a key... (Esc to cancel)");
            ImGui.popStyleColor();
        } else {
            String keyName = keyCode > 0 ? getKeyName(keyCode) : "None";
            if (ImGui.button("Bind: " + keyName)) {
                isBindingKey = true;
                // Mark all keys as "previously held" so we only capture fresh presses
                if (prevKeyStates != null) {
                    for (int i = 0; i < prevKeyStates.length; i++) {
                        prevKeyStates[i] = true;
                    }
                }
            }
            if (keyCode > 0) {
                ImGui.sameLine();
                if (ImGui.button("Remove")) {
                    bind.setValue(-1);
                }
            }
        }
    }

    // ──────────────────────────────────────────────
    //  Setting-type renderers
    // ──────────────────────────────────────────────

    private static void renderSliderSetting(String name, SliderSetting slider) {
        String suffix = slider.getSuffix();
        String label = suffix.isEmpty() ? name : name + " (" + suffix + ")";

        if (slider.getIncrement() >= 1.0 && slider.getMin() >= -100000 && slider.getMax() <= 100000) {
            // Integer slider
            intBuffer[0] = slider.getValueInt();
            if (ImGui.sliderInt(label, intBuffer, (int) slider.getMin(), (int) slider.getMax())) {
                slider.setValue(intBuffer[0]);
            }
        } else {
            // Float slider
            sliderBuffer[0] = slider.getValueFloat();
            if (ImGui.sliderFloat(label, sliderBuffer, (float) slider.getMin(), (float) slider.getMax())) {
                slider.setValue(sliderBuffer[0]);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Enum<T>> void renderEnumSetting(String name, EnumSetting<?> enumSetting) {
        EnumSetting<T> es = (EnumSetting<T>) enumSetting;
        T current = es.getValue();
        T[] constants = current.getDeclaringClass().getEnumConstants();

        String[] items = new String[constants.length];
        int currentIdx = 0;
        for (int i = 0; i < constants.length; i++) {
            items[i] = constants[i].name();
            if (constants[i] == current) {
                currentIdx = i;
            }
        }

        ImInt selected = new ImInt(currentIdx);
        if (ImGui.combo(name, selected, items)) {
            es.setValue(constants[selected.get()]);
        }
    }

    private static void renderColorSetting(String name, ColorSetting colorSetting) {
        if (colorSetting.rainbow) {
            ImGui.text(name + ": \uD83C\uDF08"); // rainbow emoji
            if (ImGui.button("Disable Rainbow")) {
                colorSetting.rainbow = false;
            }
        } else {
            Color color = colorSetting.getValue();
            colorBuffer[0] = color.getRed() / 255.0f;
            colorBuffer[1] = color.getGreen() / 255.0f;
            colorBuffer[2] = color.getBlue() / 255.0f;
            colorBuffer[3] = color.getAlpha() / 255.0f;

            if (ImGui.colorEdit4(name, colorBuffer,
                    ImGuiColorEditFlags.NoInputs |
                    ImGuiColorEditFlags.PickerHueWheel |
                    ImGuiColorEditFlags.AlphaPreview |
                    ImGuiColorEditFlags.AlphaBar)) {
                int r = (int) (colorBuffer[0] * 255.0f);
                int g = (int) (colorBuffer[1] * 255.0f);
                int b = (int) (colorBuffer[2] * 255.0f);
                int a = (int) (colorBuffer[3] * 255.0f);
                colorSetting.setValue(new Color(r, g, b, a));
            }

            if (ImGui.button("Rainbow")) {
                colorSetting.rainbow = true;
            }
        }
    }

    private static void renderStringSetting(String name, StringSetting setting) {
        stringBuffer.set(setting.getValue());
        if (ImGui.inputText(name, stringBuffer)) {
            setting.setValue(stringBuffer.get());
        }
    }

    // ──────────────────────────────────────────────
    //  Helpers
    // ──────────────────────────────────────────────

    private static List<Module> getModulesForCategory(Category cat) {
        List<Module> result = new ArrayList<>();
        for (Module m : ModuleManager.getModules()) {
            if (m.getCategory() == cat) {
                result.add(m);
            }
        }
        return result;
    }

    /** Converts a GLFW key code to a human-readable name. */
    private static String getKeyName(int key) {
        if (key <= 0) return "None";

        String name = GLFW.glfwGetKeyName(key, 0);
        if (name != null && !name.isEmpty()) return name;

        return switch (key) {
            case GLFW.GLFW_KEY_SPACE -> "Space";
            case GLFW.GLFW_KEY_ENTER -> "Enter";
            case GLFW.GLFW_KEY_TAB -> "Tab";
            case GLFW.GLFW_KEY_BACKSPACE -> "BkSpc";
            case GLFW.GLFW_KEY_DELETE -> "Del";
            case GLFW.GLFW_KEY_INSERT -> "Ins";
            case GLFW.GLFW_KEY_HOME -> "Home";
            case GLFW.GLFW_KEY_END -> "End";
            case GLFW.GLFW_KEY_PAGE_UP -> "PgUp";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "PgDn";
            case GLFW.GLFW_KEY_ESCAPE -> "Esc";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LShift";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RShift";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCtrl";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCtrl";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LAlt";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RAlt";
            case GLFW.GLFW_KEY_LEFT_SUPER -> "LSuper";
            case GLFW.GLFW_KEY_RIGHT_SUPER -> "RSuper";
            case GLFW.GLFW_KEY_CAPS_LOCK -> "Caps";
            case GLFW.GLFW_KEY_NUM_LOCK -> "NumLk";
            case GLFW.GLFW_KEY_SCROLL_LOCK -> "ScrLk";
            case GLFW.GLFW_KEY_PRINT_SCREEN -> "PrtSc";
            case GLFW.GLFW_KEY_PAUSE -> "Pause";
            case GLFW.GLFW_KEY_MENU -> "Menu";
            case GLFW.GLFW_KEY_KP_MULTIPLY -> "Num*";
            case GLFW.GLFW_KEY_KP_DIVIDE -> "Num/";
            case GLFW.GLFW_KEY_KP_ADD -> "Num+";
            case GLFW.GLFW_KEY_KP_SUBTRACT -> "Num-";
            case GLFW.GLFW_KEY_KP_DECIMAL -> "Num.";
            case GLFW.GLFW_KEY_KP_EQUAL -> "Num=";
            case GLFW.GLFW_KEY_KP_ENTER -> "NumEnt";
            case GLFW.GLFW_KEY_UP -> "Up";
            case GLFW.GLFW_KEY_DOWN -> "Down";
            case GLFW.GLFW_KEY_LEFT -> "Left";
            case GLFW.GLFW_KEY_RIGHT -> "Right";
            case GLFW.GLFW_KEY_KP_0 -> "Num0";
            case GLFW.GLFW_KEY_KP_1 -> "Num1";
            case GLFW.GLFW_KEY_KP_2 -> "Num2";
            case GLFW.GLFW_KEY_KP_3 -> "Num3";
            case GLFW.GLFW_KEY_KP_4 -> "Num4";
            case GLFW.GLFW_KEY_KP_5 -> "Num5";
            case GLFW.GLFW_KEY_KP_6 -> "Num6";
            case GLFW.GLFW_KEY_KP_7 -> "Num7";
            case GLFW.GLFW_KEY_KP_8 -> "Num8";
            case GLFW.GLFW_KEY_KP_9 -> "Num9";
            case GLFW.GLFW_KEY_F1  -> "F1";
            case GLFW.GLFW_KEY_F2  -> "F2";
            case GLFW.GLFW_KEY_F3  -> "F3";
            case GLFW.GLFW_KEY_F4  -> "F4";
            case GLFW.GLFW_KEY_F5  -> "F5";
            case GLFW.GLFW_KEY_F6  -> "F6";
            case GLFW.GLFW_KEY_F7  -> "F7";
            case GLFW.GLFW_KEY_F8  -> "F8";
            case GLFW.GLFW_KEY_F9  -> "F9";
            case GLFW.GLFW_KEY_F10 -> "F10";
            case GLFW.GLFW_KEY_F11 -> "F11";
            case GLFW.GLFW_KEY_F12 -> "F12";
            case GLFW.GLFW_KEY_F13 -> "F13";
            case GLFW.GLFW_KEY_F14 -> "F14";
            case GLFW.GLFW_KEY_F15 -> "F15";
            case GLFW.GLFW_KEY_F16 -> "F16";
            case GLFW.GLFW_KEY_F17 -> "F17";
            case GLFW.GLFW_KEY_F18 -> "F18";
            case GLFW.GLFW_KEY_F19 -> "F19";
            case GLFW.GLFW_KEY_F20 -> "F20";
            case GLFW.GLFW_KEY_F21 -> "F21";
            case GLFW.GLFW_KEY_F22 -> "F22";
            case GLFW.GLFW_KEY_F23 -> "F23";
            case GLFW.GLFW_KEY_F24 -> "F24";
            default -> "Key#" + key;
        };
    }

    private static void renderOutlinedText(ImDrawList dl, ImFont font,
                                            float fontSize, float x, float y,
                                            int outlineColor, int textColor,
                                            String text) {
        dl.addText(font, fontSize, x - 1.0f, y, outlineColor, text);
        dl.addText(font, fontSize, x + 1.0f, y, outlineColor, text);
        dl.addText(font, fontSize, x, y - 1.0f, outlineColor, text);
        dl.addText(font, fontSize, x, y + 1.0f, outlineColor, text);
        dl.addText(font, fontSize, x, y, textColor, text);
    }
}
