package cn.blockforge.generated.minecraft1211nb06d.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import cn.blockforge.generated.minecraft1211nb06d.client.ClientSkillHandler;
import cn.blockforge.generated.minecraft1211nb06d.client.SkillUpgradeScreen;
import cn.blockforge.generated.minecraft1211nb06d.skill.SkillData;
import cn.blockforge.generated.minecraft1211nb06d.skill.SkillUnlocks;

public final class ClientPayloads {
    private ClientPayloads() {
    }

    public static void handleSync(SkillSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }
            SkillData current = SkillUnlocks.data(player);
            SkillData incoming = payload.data();
            current.unlocks = incoming.unlocks;
            current.grabbedId = incoming.grabbedId;
            current.dashCooldown = incoming.dashCooldown;
            current.burstCooldown = incoming.burstCooldown;
            current.grabCooldown = incoming.grabCooldown;
            current.skillFlying = incoming.skillFlying;
            current.burstLevel = incoming.burstLevel;
            current.grabLevel = incoming.grabLevel;
            current.flyLevel = incoming.flyLevel;
            current.dashLevel = incoming.dashLevel;
            current.grabTicksLeft = incoming.grabTicksLeft;
            current.flyTicksLeft = incoming.flyTicksLeft;
            SkillUpgradeScreen.syncRevision++;
        });
    }

    public static void handleFx(SkillFxPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> playFx(payload));
    }

    private static void playFx(SkillFxPayload payload) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        Vec3 pos = new Vec3(payload.x(), payload.y(), payload.z());
        ClientSkillHandler.flash(payload.kind());
        switch (payload.kind()) {
            case SkillFxPayload.BURST -> spawnBurst(mc, level, pos, false);
            case SkillFxPayload.BURST_EXPLOSIVE -> spawnBurst(mc, level, pos, true);
            case SkillFxPayload.DASH -> spawnDash(mc, level, pos, payload.yaw(), payload.pitch());
            case SkillFxPayload.FLY_BOOST -> spawnFlyBoost(mc, level, pos);
            case SkillFxPayload.GRAB -> spawnGrab(mc, level, pos);
            default -> {
            }
        }
    }

    private static boolean isMine(Minecraft mc, Vec3 pos) {
        return mc.player != null && mc.player.position().distanceToSqr(pos) < 64.0;
    }

    private static void spawnBurst(Minecraft mc, ClientLevel level, Vec3 pos, boolean explosive) {
        for (int i = 0; i < 40; i++) {
            double a = level.random.nextDouble() * Math.PI * 2;
            double b = level.random.nextDouble() * Math.PI;
            double s = 0.35 + level.random.nextDouble() * 0.7;
            level.addParticle(ParticleTypes.FLAME, pos.x, pos.y, pos.z,
                    Math.cos(a) * Math.sin(b) * s, Math.cos(b) * s, Math.sin(a) * Math.sin(b) * s);
        }
        for (int i = 0; i < 18; i++) {
            level.addParticle(ParticleTypes.LAVA, pos.x, pos.y, pos.z,
                    (level.random.nextDouble() - 0.5) * 0.8,
                    level.random.nextDouble() * 0.5,
                    (level.random.nextDouble() - 0.5) * 0.8);
        }
        if (explosive) {
            level.addParticle(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y, pos.z, 0, 0, 0);
            level.playLocalSound(pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.9F, 0.8F, false);
        }
        level.addParticle(ParticleTypes.SWEEP_ATTACK, pos.x, pos.y, pos.z, 0, 0, 0);
        level.playLocalSound(pos.x, pos.y, pos.z, SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2F, 0.55F, false);
        level.playLocalSound(pos.x, pos.y, pos.z, SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.8F, 0.6F, false);
    }

    private static void spawnDash(Minecraft mc, ClientLevel level, Vec3 pos, float yaw, float pitch) {
        Vec3 look = Vec3.directionFromRotation(pitch, yaw);
        for (int i = 0; i < 24; i++) {
            Vec3 p = pos.add(look.scale(i * 0.35));
            level.addParticle(ParticleTypes.CLOUD, p.x, p.y, p.z, look.x * 0.4, look.y * 0.4, look.z * 0.4);
            if (i % 3 == 0) {
                level.addParticle(ParticleTypes.GUST, p.x, p.y, p.z, 0, 0, 0);
            }
        }
        level.addParticle(ParticleTypes.SONIC_BOOM, pos.x, pos.y, pos.z, 0, 0, 0);
        level.playLocalSound(pos.x, pos.y, pos.z, SoundEvents.BREEZE_SHOOT, SoundSource.PLAYERS, 1.0F, 0.7F, false);
        level.playLocalSound(pos.x, pos.y, pos.z, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.35F, 1.8F, false);
    }

    private static void spawnFlyBoost(Minecraft mc, ClientLevel level, Vec3 pos) {
        for (int i = 0; i < 36; i++) {
            double a = (Math.PI * 2 * i) / 36.0;
            level.addParticle(ParticleTypes.CLOUD, pos.x, pos.y, pos.z, Math.cos(a) * 0.55, 0.05, Math.sin(a) * 0.55);
            level.addParticle(ParticleTypes.END_ROD, pos.x, pos.y + 0.2, pos.z, Math.cos(a) * 0.25, 0.12, Math.sin(a) * 0.25);
        }
        level.addParticle(ParticleTypes.GUST_EMITTER_LARGE, pos.x, pos.y, pos.z, 0, 0, 0);
        level.addParticle(ParticleTypes.SONIC_BOOM, pos.x, pos.y, pos.z, 0, 0, 0);
        level.playLocalSound(pos.x, pos.y, pos.z, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.7F, 1.55F, false);
        level.playLocalSound(pos.x, pos.y, pos.z, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 0.9F, 0.65F, false);
        level.playLocalSound(pos.x, pos.y, pos.z, SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 0.5F, 1.6F, false);
    }

    private static void spawnGrab(Minecraft mc, ClientLevel level, Vec3 pos) {
        for (int i = 0; i < 20; i++) {
            level.addParticle(ParticleTypes.PORTAL, pos.x, pos.y, pos.z,
                    (level.random.nextDouble() - 0.5) * 0.6,
                    (level.random.nextDouble() - 0.5) * 0.6,
                    (level.random.nextDouble() - 0.5) * 0.6);
            level.addParticle(ParticleTypes.WITCH, pos.x, pos.y, pos.z, 0, 0.1, 0);
        }
        level.playLocalSound(pos.x, pos.y, pos.z, SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.9F, 0.7F, false);
        level.playLocalSound(pos.x, pos.y, pos.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5F, 1.4F, false);
    }
}
