package cn.blockforge.generated.minecraft1211nb06d.client;

import cn.blockforge.generated.minecraft1211nb06d.GeneratedMod;
import cn.blockforge.generated.minecraft1211nb06d.network.FlyStatePayload;
import cn.blockforge.generated.minecraft1211nb06d.network.SkillActionPayload;
import cn.blockforge.generated.minecraft1211nb06d.network.SkillFxPayload;
import cn.blockforge.generated.minecraft1211nb06d.skill.SkillActions;
import cn.blockforge.generated.minecraft1211nb06d.skill.SkillData;
import cn.blockforge.generated.minecraft1211nb06d.skill.SkillId;
import cn.blockforge.generated.minecraft1211nb06d.skill.SkillUnlocks;
import cn.blockforge.generated.minecraft1211nb06d.skill.SkillUpgrades;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * 客户端输入与飞行预测：加速进度在客户端算，服务端镜像同一份位移公式，
 * 保证「无惯性 / 松键即停 / 2 秒线性提速到 10 倍」在两边表现一致。
 */
@EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT)
public final class ClientSkillHandler {
    public static final KeyMapping KEY_BURST = new KeyMapping(
            "key.minecraft_1_21_1_n_b06d.burst", GLFW.GLFW_KEY_R, "key.categories.minecraft_1_21_1_n_b06d");
    public static final KeyMapping KEY_GRAB = new KeyMapping(
            "key.minecraft_1_21_1_n_b06d.grab", GLFW.GLFW_KEY_G, "key.categories.minecraft_1_21_1_n_b06d");
    public static final KeyMapping KEY_DASH = new KeyMapping(
            "key.minecraft_1_21_1_n_b06d.dash", GLFW.GLFW_KEY_V, "key.categories.minecraft_1_21_1_n_b06d");
    public static final KeyMapping KEY_UPGRADE = new KeyMapping(
            "key.minecraft_1_21_1_n_b06d.upgrade", GLFW.GLFW_KEY_P, "key.categories.minecraft_1_21_1_n_b06d");

    private static int rampTicks;
    private static int lastSentProgress = -1;
    private static int lastSentFlags = -1;
    private static boolean wasFlying;
    private static int burstFlashTicks;
    private static int dashFlashTicks;
    private static int grabFlashTicks;

    private ClientSkillHandler() {
    }

    public static int burstFlash() {
        return burstFlashTicks;
    }

    public static int dashFlash() {
        return dashFlashTicks;
    }

    public static int grabFlash() {
        return grabFlashTicks;
    }

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID, path);
    }

    /** 特效包属于自己附近时，让 HUD 图标弹一下（kind 用 SkillFxPayload 常量）。 */
    public static void flash(byte kind) {
        if (kind == SkillFxPayload.BURST || kind == SkillFxPayload.BURST_EXPLOSIVE) {
            burstFlashTicks = 6;
        } else if (kind == SkillFxPayload.DASH) {
            dashFlashTicks = 6;
        } else if (kind == SkillFxPayload.GRAB) {
            grabFlashTicks = 6;
        }
    }

    @SubscribeEvent
    public static void onClientTickPre(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        SkillData data = SkillUnlocks.data(player);
        if (data.has(SkillId.FLY) && player.getAbilities().flying && !data.dashing) {
            SkillActions.recordPrePosition(player, data);
        } else {
            data.posRecorded = false;
        }
    }

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }
        SkillData data = SkillUnlocks.data(player);
        if (burstFlashTicks > 0) {
            burstFlashTicks--;
        }
        if (dashFlashTicks > 0) {
            dashFlashTicks--;
        }
        if (grabFlashTicks > 0) {
            grabFlashTicks--;
        }
        if (mc.screen == null) {
            while (KEY_UPGRADE.consumeClick()) {
                mc.setScreen(new SkillUpgradeScreen());
            }
            while (KEY_BURST.consumeClick()) {
                PacketDistributor.sendToServer(new SkillActionPayload(SkillActionPayload.BURST, 0));
                data.burstCooldown = SkillActions.BURST_COOLDOWN;
                burstFlashTicks = 6;
            }
            while (KEY_GRAB.consumeClick()) {
                PacketDistributor.sendToServer(new SkillActionPayload(SkillActionPayload.GRAB, 0));
            }
            while (KEY_DASH.consumeClick()) {
                PacketDistributor.sendToServer(new SkillActionPayload(SkillActionPayload.DASH, 0));
                data.dashCooldown = SkillActions.DASH_COOLDOWN;
                dashFlashTicks = 6;
            }
        } else {
            KEY_UPGRADE.consumeClick();
            KEY_BURST.consumeClick();
            KEY_GRAB.consumeClick();
            KEY_DASH.consumeClick();
        }
        SkillUnlocks.tickCooldowns(data);
        tickLocalTimers(data, player);
        tickFlight(mc, player, data);
    }

    private static void tickLocalTimers(SkillData data, LocalPlayer player) {
        int flyLimit = SkillUpgrades.flyDurationTicks(data.flyLevel);
        if (data.has(SkillId.FLY) && flyLimit >= 0) {
            if (player.getAbilities().flying) {
                data.flyTicksLeft = Math.max(0, data.flyTicksLeft - 1);
            } else if (data.flyTicksLeft >= 0 && data.flyTicksLeft < flyLimit) {
                data.flyTicksLeft++;
            }
        }
        int grabLimit = SkillUpgrades.grabDurationTicks(data.grabLevel);
        if (data.grabbedId >= 0 && grabLimit >= 0 && data.grabTicksLeft > 0) {
            data.grabTicksLeft--;
        }
    }

    private static void tickFlight(Minecraft mc, LocalPlayer player, SkillData data) {
        boolean flying = player.getAbilities().flying;
        boolean unlocked = data.has(SkillId.FLY);
        if (flying != wasFlying && unlocked) {
            playFlightToggle(flying, mc, player);
        }
        if (!unlocked || !flying) {
            if (wasFlying && !flying) {
                sendFlyState(0.0F, false, false, false);
            }
            rampTicks = 0;
            data.flyProgress = 0.0F;
            data.flyHorizontal = false;
            data.flyVertical = false;
            wasFlying = flying;
            return;
        }
        boolean boost = SkillUpgrades.flyBoostUnlocked(data.flyLevel) && mc.options.keySprint.isDown();
        if (boost) {
            rampTicks = Math.min(SkillActions.BOOST_RAMP_TICKS, rampTicks + 1);
        } else {
            rampTicks = 0;
        }
        data.flyProgress = rampTicks / (float) SkillActions.BOOST_RAMP_TICKS;
        data.flyHorizontal = hasHorizontalInput(mc);
        data.flyVertical = mc.options.keyJump.isDown() || mc.options.keyShift.isDown();
        data.sprintHeld = boost;
        if (!data.dashing) {
            SkillActions.applyFlightMotion(player, data);
        }
        sendFlyState(data.flyProgress, data.flyHorizontal, data.flyVertical, boost);
        wasFlying = flying;
    }

    private static void playFlightToggle(boolean start, Minecraft mc, LocalPlayer player) {
        if (mc.level == null) {
            return;
        }
        mc.level.playSound(player, player.getX(), player.getY(), player.getZ(),
                start ? SoundEvents.BAT_TAKEOFF : SoundEvents.BAT_AMBIENT,
                SoundSource.PLAYERS, 0.7F, start ? 1.25F : 0.85F);
    }

    private static void sendFlyState(float progress, boolean horizontal, boolean vertical, boolean boost) {
        FlyStatePayload payload = FlyStatePayload.of(progress, horizontal, vertical, boost);
        int p = payload.progress() & 0xFF;
        int f = payload.flags() & 0xFF;
        if (p != lastSentProgress || f != lastSentFlags) {
            lastSentProgress = p;
            lastSentFlags = f;
            PacketDistributor.sendToServer(payload);
        }
    }

    private static boolean hasHorizontalInput(Minecraft mc) {
        return mc.options.keyUp.isDown() || mc.options.keyDown.isDown()
                || mc.options.keyLeft.isDown() || mc.options.keyRight.isDown();
    }
}
