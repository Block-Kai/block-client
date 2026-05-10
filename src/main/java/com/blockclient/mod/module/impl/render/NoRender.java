package com.blockclient.mod.module.impl.render;

import com.blockclient.mod.Category;
import com.blockclient.mod.Module;
import com.blockclient.mod.setting.*;

/**
 * NoRender - 禁用渲染
 * 屏蔽各种烦人的渲染效果（粒子、药水效果、火焰、覆盖等）
 */
public class NoRender extends Module {
    public static NoRender INSTANCE;

    public final BooleanSetting particles = add(new BooleanSetting("Particles", true));
    public final BooleanSetting potionEffects = add(new BooleanSetting("PotionEffects", false));
    public final BooleanSetting fireOverlay = add(new BooleanSetting("FireOverlay", true));
    public final BooleanSetting waterOverlay = add(new BooleanSetting("WaterOverlay", true));
    public final BooleanSetting hurtCam = add(new BooleanSetting("HurtCam", true));
    public final BooleanSetting pumpkinOverlay = add(new BooleanSetting("PumpkinOverlay", true));
    public final BooleanSetting bossBar = add(new BooleanSetting("BossBar", false));
    public final BooleanSetting scoreboard = add(new BooleanSetting("Scoreboard", false));

    public NoRender() {
        super("NoRender", "禁用各种渲染效果", Category.Render);
        INSTANCE = this;
    }

    // 以下属性被 mixins 读取
    public boolean shouldRemoveParticles() { return isOn() && particles.getValue(); }
    public boolean shouldRemoveFire() { return isOn() && fireOverlay.getValue(); }
    public boolean shouldRemoveWater() { return isOn() && waterOverlay.getValue(); }
    public boolean shouldRemoveHurtCam() { return isOn() && hurtCam.getValue(); }
    public boolean shouldRemovePumpkin() { return isOn() && pumpkinOverlay.getValue(); }
    public boolean shouldRemoveBossBar() { return isOn() && bossBar.getValue(); }
    public boolean shouldRemoveScoreboard() { return isOn() && scoreboard.getValue(); }
    public boolean shouldRemovePotionEffects() { return isOn() && potionEffects.getValue(); }
}
