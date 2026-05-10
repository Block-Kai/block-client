package com.blockclient.mod.manager;

import com.blockclient.mod.Module;
import com.blockclient.mod.module.impl.combat.*;
import com.blockclient.mod.module.impl.movement.*;
import com.blockclient.mod.module.impl.player.*;
import com.blockclient.mod.module.impl.render.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 模块管理器
 * 管理所有模块的注册、按键、渲染分发
 */
public class ModuleManager {

    private static final List<Module> modules = new ArrayList<>();

    public static void init() {
        // Combat (5)
        modules.add(new Aura());
        modules.add(new AutoCrystal());
        modules.add(new AutoMace());
        modules.add(new Offhand());
        modules.add(new GrimSurround());

        // Movement (3)
        modules.add(new Velocity());
        modules.add(new ElytraFly());
        modules.add(new Phase());

        // Player (1)
        modules.add(new Freecam());

        // Render (11)
        modules.add(new ESP());
        modules.add(new NameTags());
        modules.add(new Tracers());
        modules.add(new Crosshair());
        modules.add(new LogoutSpots());
        modules.add(new Trajectories());
        modules.add(new Ambience());
        modules.add(new NoRender());
        modules.add(new PhaseESP());
        modules.add(new CameraClip());
        modules.add(new MotionCamera());
    }

    public static List<Module> getModules() {
        return modules;
    }

    // ── 按键处理 ──

    public static void onKeyPressed(int key) {
        for (Module module : modules) {
            if (module.getBindSetting().getValue() == key) {
                module.toggle();
            }
        }
    }

    // ── 渲染分发 ──

    /** 调用所有已启用模块的 onRender2D */
    public static void onRender2D(MatrixStack matrices, float tickDelta) {
        if (MinecraftClient.getInstance().player == null) return;

        for (Module module : modules) {
            if (module.isOn()) {
                try {
                    module.onRender2D(matrices, tickDelta);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** 调用所有已启用模块的 onRender3D */
    public static void onRender3D(MatrixStack matrices, float tickDelta) {
        if (MinecraftClient.getInstance().player == null) return;

        for (Module module : modules) {
            if (module.isOn()) {
                try {
                    module.onRender3D(matrices, tickDelta);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** 调用所有已启用模块的 onTick */
    public static void onTick() {
        if (MinecraftClient.getInstance().player == null) return;

        for (Module module : modules) {
            if (module.isOn()) {
                try {
                    module.onTick();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** 数据包发送 — 返回 true 表示取消 */
    public static boolean onPacketSend(Object packet) {
        boolean cancel = false;
        for (Module module : modules) {
            if (module.isOn()) {
                try {
                    if (module.onPacketSend(packet)) cancel = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return cancel;
    }

    /** 数据包接收 — 返回 true 表示取消 */
    public static boolean onPacketReceive(Object packet) {
        boolean cancel = false;
        for (Module module : modules) {
            if (module.isOn()) {
                try {
                    if (module.onPacketReceive(packet)) cancel = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return cancel;
    }

    public static Module getModuleByName(String name) {
        for (Module m : modules) {
            if (m.getName().equalsIgnoreCase(name)) {
                return m;
            }
        }
        return null;
    }

    public static <T extends Module> T getModule(Class<T> clazz) {
        for (Module m : modules) {
            if (clazz.isInstance(m)) {
                return clazz.cast(m);
            }
        }
        return null;
    }
}
