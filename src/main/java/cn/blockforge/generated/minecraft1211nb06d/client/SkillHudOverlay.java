package cn.blockforge.generated.minecraft1211nb06d.client;

import cn.blockforge.generated.minecraft1211nb06d.GeneratedMod;
import cn.blockforge.generated.minecraft1211nb06d.skill.SkillActions;
import cn.blockforge.generated.minecraft1211nb06d.skill.SkillData;
import cn.blockforge.generated.minecraft1211nb06d.skill.SkillId;
import cn.blockforge.generated.minecraft1211nb06d.skill.SkillUnlocks;
import cn.blockforge.generated.minecraft1211nb06d.skill.SkillUpgrades;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.hud.Hud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** 技能图标 HUD，通过 owo-lib Hud 绘制等级、冷却、计时和加速进度。 */
public final class SkillHudOverlay extends BaseComponent {
    private static final ResourceLocation HUD_ID =
            ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID, "skill_hud");
    private static final ResourceLocation FRAME = ClientSkillHandler.location("textures/gui/icon_frame.png");
    private static final ResourceLocation ICON_BURST = ClientSkillHandler.location("textures/gui/icon_burst.png");
    private static final ResourceLocation ICON_GRAB = ClientSkillHandler.location("textures/gui/icon_grab.png");
    private static final ResourceLocation ICON_FLY = ClientSkillHandler.location("textures/gui/icon_fly.png");
    private static final ResourceLocation ICON_DASH = ClientSkillHandler.location("textures/gui/icon_dash.png");

    private static final int SLOT = 22;
    private static final int ICON = 16;
    private static final int PAD = 4;
    private static final int HUD_WIDTH = PAD + SLOT * 4 + 48;
    private static final int HUD_HEIGHT = PAD + 48;

    private SkillHudOverlay() {
        this.horizontalSizing(Sizing.fixed(HUD_WIDTH));
        this.verticalSizing(Sizing.fixed(HUD_HEIGHT));
        this.positioning(Positioning.relative(0, 100));
        this.margins(io.wispforest.owo.ui.core.Insets.of(0, 26, 8, 0));
    }

    public static void register() {
        if (!Hud.hasComponent(HUD_ID)) {
            Hud.add(HUD_ID, SkillHudOverlay::new);
        }
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) return;

        SkillData data = SkillUnlocks.data(player);
        float pulse = 0.5F + 0.5F * Mth.sin(System.currentTimeMillis() / 400.0F);
        int originX = this.x() + PAD;
        int originY = this.y() + PAD;
        drawSlot(context, originX, originY, ICON_BURST, data.has(SkillId.BURST),
                ClientSkillHandler.burstFlash() > 0, data.burstCooldown, SkillActions.BURST_COOLDOWN,
                ClientSkillHandler.KEY_BURST.getDisplayName(), data.burstLevel, pulse);
        drawSlot(context, originX + SLOT, originY, ICON_GRAB, data.has(SkillId.GRAB),
                data.grabbedId >= 0 || ClientSkillHandler.grabFlash() > 0,
                data.grabCooldown, SkillActions.GRAB_COOLDOWN,
                ClientSkillHandler.KEY_GRAB.getDisplayName(), data.grabLevel, pulse);
        drawSlot(context, originX + SLOT * 2, originY, ICON_FLY, data.has(SkillId.FLY),
                player.getAbilities().flying, 0, 1,
                Component.translatable("hud." + GeneratedMod.MOD_ID + ".fly_hint"), data.flyLevel, pulse);
        drawSlot(context, originX + SLOT * 3, originY, ICON_DASH, data.has(SkillId.DASH),
                ClientSkillHandler.dashFlash() > 0, data.dashCooldown, SkillActions.DASH_COOLDOWN,
                ClientSkillHandler.KEY_DASH.getDisplayName(), data.dashLevel, pulse);

        if (player.getAbilities().flying && data.flyLevel >= 3 && data.flyProgress > 0.0F) {
            int barWidth = SLOT * 4 - 6;
            int filled = Mth.clamp(Math.round(barWidth * data.flyProgress), 0, barWidth);
            int barY = originY + 21;
            context.fill(originX, barY, originX + barWidth, barY + 3, 0x80000000);
            int color = data.flyProgress >= 1.0F ? 0xFF7CF0FF : 0xFF4FA8FF;
            context.fill(originX, barY, originX + filled, barY + 3, color);
            context.drawString(mc.font, mc.options.keySprint.getDisplayName(),
                    originX + barWidth + 4, barY - 2, 0xFFB4B4B4);
        }

        int labelY = originY + 29;
        if (data.has(SkillId.FLY) && SkillUpgrades.flyDurationTicks(data.flyLevel) >= 0) {
            int seconds = Math.max(0, data.flyTicksLeft) / 20;
            context.drawString(mc.font, Component.translatable("hud." + GeneratedMod.MOD_ID + ".fly_time", seconds),
                    originX, labelY, 0xFF8FE7FF);
        }
        if (data.grabbedId >= 0 && SkillUpgrades.grabDurationTicks(data.grabLevel) >= 0) {
            int seconds = Math.max(0, data.grabTicksLeft) / 20;
            context.drawString(mc.font, Component.translatable("hud." + GeneratedMod.MOD_ID + ".grab_time", seconds),
                    originX + SLOT * 2, labelY, 0xFFCDAAFF);
        } else {
            context.drawString(mc.font, Component.translatable("hud." + GeneratedMod.MOD_ID + ".upgrade_hint"),
                    originX + SLOT * 2, labelY, 0xFFB4B4B4);
        }
    }

    private static void drawSlot(OwoUIDrawContext graphics, int x, int y, ResourceLocation icon,
                                 boolean unlocked, boolean active, int cooldown, int maxCooldown,
                                 Component label, int level, float pulse) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int lift = active ? 1 : 0;
        int top = y - lift;
        graphics.blit(FRAME, x - 3, top - 3, 0, 0, 22, 22, 22, 22);
        if (unlocked) {
            if (active) graphics.setColor(1.35F, 1.35F, 1.35F, 1.0F);
            graphics.blit(icon, x, top, 0, 0, ICON, ICON, ICON, ICON);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            graphics.setColor(0.22F, 0.22F, 0.25F, 0.9F);
            graphics.blit(icon, x, top, 0, 0, ICON, ICON, ICON, ICON);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        if (unlocked && cooldown > 0 && maxCooldown > 1) {
            int covered = Mth.clamp(ICON * cooldown / maxCooldown, 0, ICON);
            graphics.fill(x, top + (ICON - covered), x + ICON, top + ICON, 0x99101010);
        }
        if (unlocked) {
            graphics.fill(x + 11, top + 10, x + 18, top + 18, 0xCC101010);
            graphics.drawString(font, Integer.toString(level), x + 12, top + 10, 0xFFFFFFFF, true);
        }
        if (active) {
            graphics.drawRectOutline(x - 3, top - 3, 22, 22, 0xFF7CF0FF);
        } else if (unlocked && cooldown <= 0) {
            int alpha = 0x55 + (int) (0x55 * pulse);
            graphics.drawRectOutline(x - 3, top - 3, 22, 22, (alpha << 24) | 0x00D8FF);
        } else if (unlocked) {
            graphics.drawRectOutline(x - 3, top - 3, 22, 22, 0x44FFFFFF);
        }
        graphics.drawString(font, label, x - 2, top + ICON + 1, unlocked ? 0xFFE6E6E6 : 0xFF777777);
    }
}
