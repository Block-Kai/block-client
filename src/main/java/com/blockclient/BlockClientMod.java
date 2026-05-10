package com.blockclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blockclient.ui.ImGuiManager;
import com.blockclient.input.InputManager;
import com.blockclient.mod.manager.ModuleManager;

/**
 * Block-Client — a client-side Fabric mod for anarchy servers like 2b2t.
 * Part 2: Module system with 20 modules ported from SunCat Client.
 */
public class BlockClientMod implements ClientModInitializer {

    public static final String MOD_ID = "blockclient";
    public static final String CLIENT_NAME = "Block-Client";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static volatile boolean uiOpen = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[{}] Initializing client-side framework...", CLIENT_NAME);

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            LOGGER.info("[{}] Client window ready — bootstrapping modules, ImGui, input.", CLIENT_NAME);

            ModuleManager.init();
            LOGGER.info("[{}] {} modules loaded.", CLIENT_NAME, ModuleManager.getModules().size());

            ImGuiManager.initialize();
            InputManager.initialize();

            LOGGER.info("[{}] Ready. Right-Shift to toggle UI.", CLIENT_NAME);
        });

        // Tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ModuleManager.onTick();
        });

        // 3D Rendering is handled by GameRendererMixin (direct mixin into renderWorld)
        // Fabric WorldRenderEvents doesn't reliably fire with Sodium.
        // 2D Rendering via HudElement
        HudElementRegistry.addLast(
            Identifier.of(MOD_ID, "modules_2d"),
            (drawContext, tickCounter) -> {
                float td = tickCounter.getTickProgress(true);
                ModuleManager.onRender2D(new net.minecraft.client.util.math.MatrixStack(), td);
            }
        );

        LOGGER.info("[{}] Waiting for window...", CLIENT_NAME);
    }
}
