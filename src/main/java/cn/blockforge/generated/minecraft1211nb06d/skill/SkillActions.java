package cn.blockforge.generated.minecraft1211nb06d.skill;

import cn.blockforge.generated.minecraft1211nb06d.network.FlyStatePayload;
import cn.blockforge.generated.minecraft1211nb06d.network.SkillActionPayload;
import cn.blockforge.generated.minecraft1211nb06d.network.SkillFxPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * 四个技能的服务器端实现，客户端侧复用同一套飞行位移公式。
 */
public final class SkillActions {
    // 爆裂拳
    public static final double BURST_RANGE = 8.0;
    public static final float BURST_DAMAGE = 150.0F;
    public static final float BURST_EXPLOSION_POWER = 32.0F;
    public static final int BURST_COOLDOWN = 20;
    // 抓取
    public static final double GRAB_RANGE = 8.0;
    public static final int GRAB_COOLDOWN = 10;
    // 冲刺：4.5 个区块 = 72 格，0.25s = 5 tick
    public static final double DASH_DISTANCE = 4.5 * 16.0;
    public static final int DASH_TICKS = 5;
    public static final int DASH_COOLDOWN = 40;
    // 飞行：按住加速键 2s（40 tick）线性提速到 10 倍
    public static final int BOOST_RAMP_TICKS = 40;
    public static final double BOOST_MULTIPLIER = 10.0;
    public static final float VANILLA_FLY_SPEED = 0.05F;

    private SkillActions() {
    }

    public static void handle(ServerPlayer player, SkillActionPayload payload) {
        SkillData data = SkillUnlocks.data(player);
        switch (payload.action()) {
            case SkillActionPayload.BURST -> tryBurst(player, data);
            case SkillActionPayload.GRAB -> tryGrab(player, data);
            case SkillActionPayload.DASH -> tryDash(player, data);
            case SkillActionPayload.TOGGLE_FLY -> toggleFly(player, data);
            case SkillActionPayload.SPRINT_STATE -> data.sprintHeld = payload.extra() != 0;
            case SkillActionPayload.RELEASE_GRAB -> {
                releaseGrab(player, data);
                SkillUnlocks.sync(player);
            }
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------ 爆裂拳

    public static void tryBurst(ServerPlayer player, SkillData data) {
        if (!data.has(SkillId.BURST)) {
            notifyLocked(player, SkillId.BURST);
            return;
        }
        if (data.burstCooldown > 0) {
            return;
        }
        LivingEntity grabbed = resolveGrabbed(player, data);
        if (grabbed != null) {
            // 抓着目标时：先给目标挂上冲刺效果并朝准星方向甩出去，再触发爆裂拳
            flingGrabbed(player, grabbed);
            releaseGrab(player, data);
        }
        Vec3 aimPoint = lookPoint(player, BURST_RANGE);
        LivingEntity target = findLookTarget(player, BURST_RANGE, aimPoint);
        Vec3 center = target != null
                ? target.position().add(0, target.getBbHeight() * 0.5, 0)
                : aimPoint;
        if (target != null) {
            DamageSource source = player.damageSources().playerAttack(player);
            target.hurt(source, SkillUpgrades.burstDamage(data.burstLevel));
            Vec3 knock = player.getLookAngle().normalize().scale(3.6).add(0, 0.6, 0);
            target.setDeltaMovement(knock);
            target.hurtMarked = true;
        }
        boolean explosive = SkillUpgrades.burstExplodes(data.burstLevel);
        if (explosive) {
            explodeAt(player, center);
        }
        data.burstCooldown = BURST_COOLDOWN;
        broadcastFx(player, explosive ? SkillFxPayload.BURST_EXPLOSIVE : SkillFxPayload.BURST, center);
        player.swing(player.getUsedItemHand(), true);
        SkillUnlocks.sync(player);
    }

    private static void explodeAt(ServerPlayer player, Vec3 pos) {
        ServerLevel level = player.serverLevel();
        SelfSafeExplosionCalculator calculator = new SelfSafeExplosionCalculator(player);
        level.explode(player, player.damageSources().explosion(player, player), calculator,
                pos.x, pos.y, pos.z, BURST_EXPLOSION_POWER, false, Level.ExplosionInteraction.TNT,
                (ParticleOptions) ParticleTypes.EXPLOSION, (ParticleOptions) ParticleTypes.EXPLOSION_EMITTER,
                SoundEvents.GENERIC_EXPLODE);
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.1F, 0.55F);
    }

    // ------------------------------------------------------------------ 抓取

    public static void tryGrab(ServerPlayer player, SkillData data) {
        if (!data.has(SkillId.GRAB)) {
            notifyLocked(player, SkillId.GRAB);
            return;
        }
        if (data.grabCooldown > 0) {
            return;
        }
        LivingEntity existing = resolveGrabbed(player, data);
        if (existing != null) {
            releaseGrab(player, data);
            data.grabCooldown = GRAB_COOLDOWN;
            SkillUnlocks.sync(player);
            return;
        }
        Vec3 aimPoint = lookPoint(player, GRAB_RANGE);
        LivingEntity target = findLookTarget(player, GRAB_RANGE, aimPoint);
        if (target == null) {
            player.displayClientMessage(
                    Component.translatable("message.minecraft_1_21_1_n_b06d.grab.miss"), true);
            return;
        }
        data.grabbedId = target.getId();
        data.grabTicksLeft = SkillUpgrades.grabDurationTicks(data.grabLevel);
        data.grabCooldown = GRAB_COOLDOWN;
        target.setDeltaMovement(Vec3.ZERO);
        target.hurtMarked = true;
        target.setNoGravity(true);
        target.setSharedFlagOnFire(false);
        broadcastFx(player, SkillFxPayload.GRAB, target.position().add(0, target.getBbHeight() * 0.5, 0));
        SkillUnlocks.sync(player);
    }

    public static void releaseGrab(Player player, SkillData data) {
        LivingEntity grabbed = resolveGrabbed(player, data);
        if (grabbed != null) {
            grabbed.setNoGravity(false);
        }
        data.grabbedId = -1;
        data.grabTicksLeft = -1;
    }

    private static LivingEntity resolveGrabbed(Player player, SkillData data) {
        if (data.grabbedId < 0) {
            return null;
        }
        Entity entity = player.level().getEntity(data.grabbedId);
        if (entity instanceof LivingEntity living && living.isAlive() && living != player) {
            return living;
        }
        return null;
    }

    private static void tickGrab(ServerPlayer player, SkillData data) {
        LivingEntity grabbed = resolveGrabbed(player, data);
        if (grabbed == null) {
            if (data.grabbedId != -1) {
                data.grabbedId = -1;
                SkillUnlocks.sync(player);
            }
            return;
        }
        int grabLimit = SkillUpgrades.grabDurationTicks(data.grabLevel);
        if (grabLimit >= 0 && data.grabTicksLeft >= 0) {
            if (data.grabTicksLeft <= 0) {
                releaseGrab(player, data);
                player.displayClientMessage(Component.translatable("message.minecraft_1_21_1_n_b06d.grab.expired"), true);
                SkillUnlocks.sync(player);
                return;
            }
            data.grabTicksLeft--;
        }
        Vec3 hold = grabHoldPosition(player);
        grabbed.setNoGravity(true);
        grabbed.teleportTo(hold.x, hold.y, hold.z);
        grabbed.setDeltaMovement(Vec3.ZERO);
        grabbed.hurtMarked = true;
        grabbed.fallDistance = 0.0F;
        grabbed.setYRot(player.getYRot());
        grabbed.setXRot(player.getXRot());
        grabbed.yHeadRot = player.yHeadRot;
    }

    /** 左手前侧：准星方向偏左 */
    private static Vec3 grabHoldPosition(Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 left = new Vec3(look.z, 0.0, -look.x);
        if (left.lengthSqr() < 1.0E-4) {
            left = new Vec3(1.0, 0.0, 0.0);
        }
        left = left.normalize().scale(0.9);
        return player.getEyePosition()
                .add(look.scale(1.5))
                .add(left)
                .add(0.0, -0.35, 0.0);
    }

    private static void flingGrabbed(ServerPlayer player, LivingEntity grabbed) {
        Vec3 knock = player.getLookAngle().normalize().scale(dashStep() * DASH_TICKS * 0.35)
                .add(0, 0.7, 0);
        grabbed.setNoGravity(false);
        grabbed.setDeltaMovement(knock);
        grabbed.hurtMarked = true;
    }

    // ------------------------------------------------------------------ 冲刺

    public static void tryDash(ServerPlayer player, SkillData data) {
        if (!data.has(SkillId.DASH)) {
            notifyLocked(player, SkillId.DASH);
            return;
        }
        if (data.dashCooldown > 0 || data.dashing) {
            return;
        }
        LivingEntity grabbed = resolveGrabbed(player, data);
        if (grabbed != null) {
            flingGrabbed(player, grabbed);
            releaseGrab(player, data);
        }
        Vec3 look = player.getLookAngle();
        if (look.lengthSqr() < 1.0E-6) {
            look = new Vec3(-Mth.sin(player.getYRot() * Mth.DEG_TO_RAD), 0.0,
                    Mth.cos(player.getYRot() * Mth.DEG_TO_RAD));
        }
        look = look.normalize();
        data.dashing = true;
        data.dashTicksLeft = DASH_TICKS;
        data.dashDirX = look.x;
        data.dashDirY = look.y;
        data.dashDirZ = look.z;
        data.dashCooldown = DASH_COOLDOWN;
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
        broadcastFx(player, SkillFxPayload.DASH, player.position().add(0, player.getBbHeight() * 0.5, 0));
        SkillUnlocks.sync(player);
    }

    private static double dashStep() {
        return DASH_DISTANCE / DASH_TICKS;
    }

    private static void tickDash(ServerPlayer player, SkillData data) {
        if (!data.dashing) {
            return;
        }
        if (data.dashTicksLeft <= 0) {
            endDash(player, data);
            return;
        }
        Vec3 dir = new Vec3(data.dashDirX, data.dashDirY, data.dashDirZ);
        if (dir.lengthSqr() < 1.0E-6) {
            endDash(player, data);
            return;
        }
        dir = dir.normalize();
        Vec3 step = dir.scale(dashStep());
        Vec3 from = player.getEyePosition();
        BlockHitResult clip = player.level().clip(new ClipContext(from, from.add(step),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (clip.getType() == HitResult.Type.BLOCK) {
            Vec3 remain = clip.getLocation().subtract(from);
            if (remain.lengthSqr() < 0.35) {
                endDash(player, data);
                return;
            }
            step = remain.scale(0.9);
            data.dashTicksLeft = 1;
        }
        player.setDeltaMovement(Vec3.ZERO);
        Vec3 before = player.position();
        player.move(MoverType.SELF, step);
        if (player.position().distanceToSqr(before) < 1.0E-4) {
            endDash(player, data);
            return;
        }
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
        player.fallDistance = 0.0F;
        // 冲刺位移由服务端驱动并逐 tick 同步位置，避免客户端被反作弊判定「移动过快」
        player.connection.teleport(player.getX(), player.getY(), player.getZ(),
                player.getYRot(), player.getXRot());
        data.dashTicksLeft--;
        if (data.dashTicksLeft <= 0) {
            endDash(player, data);
            return;
        }
        spawnDashTrail(player.serverLevel(), player);
    }

    private static void endDash(ServerPlayer player, SkillData data) {
        data.dashing = false;
        data.dashTicksLeft = 0;
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
    }

    private static void spawnDashTrail(ServerLevel level, Player player) {
        Vec3 pos = player.position().add(0, player.getBbHeight() * 0.5, 0);
        level.sendParticles(ParticleTypes.CLOUD, pos.x, pos.y, pos.z, 10, 0.25, 0.2, 0.25, 0.03);
        level.sendParticles(ParticleTypes.GUST, pos.x, pos.y, pos.z, 2, 0.1, 0.1, 0.1, 0.0);
        level.sendParticles(ParticleTypes.SONIC_BOOM, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
    }

    // ------------------------------------------------------------------ 飞行

    public static void toggleFly(ServerPlayer player, SkillData data) {
        if (!data.has(SkillId.FLY)) {
            notifyLocked(player, SkillId.FLY);
            return;
        }
        Abilities abilities = player.getAbilities();
        if (!abilities.flying && !hasFlyTimeLeft(data)) {
            player.displayClientMessage(
                    Component.translatable("message.minecraft_1_21_1_n_b06d.fly.exhausted"), true);
            return;
        }
        abilities.mayfly = true;
        abilities.flying = !abilities.flying;
        if (!abilities.flying) {
            abilities.setFlyingSpeed(VANILLA_FLY_SPEED);
            data.resetFlight();
            player.setDeltaMovement(player.getDeltaMovement().multiply(0.5, 0.0, 0.5));
        }
        data.skillFlying = abilities.flying;
        player.onUpdateAbilities();
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                abilities.flying ? SoundEvents.BAT_TAKEOFF : SoundEvents.BAT_AMBIENT,
                SoundSource.PLAYERS, 0.6F, abilities.flying ? 1.3F : 0.9F);
        SkillUnlocks.sync(player);
    }

    /** 客户端每 tick 上报加速进度与输入状态，服务端镜像同一份物理。 */
    public static void applyFlyState(ServerPlayer player, FlyStatePayload payload) {
        SkillData data = SkillUnlocks.data(player);
        if (!data.has(SkillId.FLY)) {
            data.resetFlight();
            return;
        }
        boolean boostAllowed = SkillUpgrades.flyBoostUnlocked(data.flyLevel);
        data.flyProgress = boostAllowed ? payload.progressFloat() : 0.0F;
        data.flyHorizontal = payload.horizontal();
        data.flyVertical = payload.vertical();
        data.sprintHeld = boostAllowed && payload.boost();
        if (data.flyProgress >= 1.0F) {
            if (!data.boostedBurstPlayed) {
                data.boostedBurstPlayed = true;
                Vec3 pos = player.position().add(0, player.getBbHeight() * 0.6, 0);
                broadcastFx(player, SkillFxPayload.FLY_BOOST, pos);
                player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.6F, 1.6F);
            }
        } else {
            data.boostedBurstPlayed = false;
        }
    }

    /** tick 开始前记录位置，供位移式加速使用（客户端同样调用）。 */
    public static void recordPrePosition(Player player, SkillData data) {
        data.lastX = player.getX();
        data.lastZ = player.getZ();
        data.posRecorded = true;
    }

    /**
     * 无惯性飞行：水平方向随加速进度提速（最高 10 倍），曲线取二次方，
     * 起速阶段只比正常飞行快一点点；垂直方向保持原版创造手感，松键立刻停住。
     */
    public static void applyFlightMotion(Player player, SkillData data) {
        float progress = Mth.clamp(data.flyProgress, 0.0F, 1.0F);
        Vec3 motion = player.getDeltaMovement();
        if (data.posRecorded) {
            double dx = player.getX() - data.lastX;
            double dz = player.getZ() - data.lastZ;
            double travelled = Math.sqrt(dx * dx + dz * dz);
            double multiplier = boostMultiplier(progress);
            if (data.flyHorizontal && travelled > 1.0E-4 && travelled < 16.0 && multiplier > 1.0005) {
                double extra = Math.min(travelled * (multiplier - 1.0), 8.0);
                Vec3 dir = new Vec3(dx / travelled, 0.0, dz / travelled);
                player.move(MoverType.SELF, dir.scale(extra));
            }
        }
        motion = player.getDeltaMovement();
        if (!data.flyHorizontal) {
            player.setDeltaMovement(0.0, motion.y, 0.0);
            motion = player.getDeltaMovement();
        }
        if (!data.flyVertical) {
            player.setDeltaMovement(motion.x, 0.0, motion.z);
        }
        player.fallDistance = 0.0F;
    }

    /** progress 为 0 时等于原速（1 倍）；二次方曲线让起速更柔和，终点仍是 10 倍。 */
    public static double boostMultiplier(float progress) {
        float p = Mth.clamp(progress, 0.0F, 1.0F);
        return 1.0 + (BOOST_MULTIPLIER - 1.0) * (double) (p * p);
    }

    private static void tickFlight(ServerPlayer player, SkillData data) {
        if (!data.has(SkillId.FLY)) {
            if (data.skillFlying) {
                data.skillFlying = false;
                data.resetFlight();
                restoreVanillaAbilities(player);
            }
            return;
        }
        Abilities abilities = player.getAbilities();
        if (!abilities.mayfly && !player.isSpectator()) {
            // 生存模式下授予 mayfly，起跳开关交由原版双击空格逻辑处理
            abilities.mayfly = true;
            abilities.setFlyingSpeed(VANILLA_FLY_SPEED);
            player.onUpdateAbilities();
        }
        int limit = SkillUpgrades.flyDurationTicks(data.flyLevel);
        boolean flying = abilities.flying;
        if (limit >= 0) {
            if (data.flyTicksLeft < 0 || data.flyTicksLeft > limit) {
                data.flyTicksLeft = limit;
            }
            if (flying) {
                if (data.flyTicksLeft <= 0) {
                    // 时间耗尽：强制结束飞行
                    abilities.flying = false;
                    abilities.setFlyingSpeed(VANILLA_FLY_SPEED);
                    player.onUpdateAbilities();
                    data.skillFlying = false;
                    data.resetFlight();
                    player.displayClientMessage(
                            Component.translatable("message.minecraft_1_21_1_n_b06d.fly.expired"), true);
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.BAT_AMBIENT, SoundSource.PLAYERS, 0.6F, 0.7F);
                    SkillUnlocks.sync(player);
                    return;
                }
                data.flyTicksLeft--;
            } else if (data.flyTicksLeft < limit) {
                // 不飞时按 1:1 回充
                data.flyTicksLeft++;
            }
        } else if (!flying) {
            data.flyTicksLeft = -1;
        }
        data.skillFlying = flying;
        if (!flying) {
            data.flyProgress = 0.0F;
            data.boostedBurstPlayed = false;
            return;
        }
        if (data.dashing) {
            return;
        }
        applyFlightMotion(player, data);
    }

    /** 服务端每秒回传一次计时，HUD 本地逐 tick 预测，避免每 tick 发包。 */
    private static void tickTimers(ServerPlayer player, SkillData data) {
        data.timerSyncTicks++;
        if (data.timerSyncTicks >= 20) {
            data.timerSyncTicks = 0;
            if (SkillUpgrades.flyDurationTicks(data.flyLevel) >= 0
                    || (data.grabbedId >= 0 && SkillUpgrades.grabDurationTicks(data.grabLevel) >= 0)) {
                SkillUnlocks.sync(player);
            }
        }
    }

    public static boolean hasFlyTimeLeft(SkillData data) {
        int limit = SkillUpgrades.flyDurationTicks(data.flyLevel);
        return limit < 0 || data.flyTicksLeft > 0;
    }

    private static void restoreVanillaAbilities(ServerPlayer player) {
        Abilities abilities = player.getAbilities();
        if (!player.isCreative() && !player.isSpectator()) {
            abilities.mayfly = false;
            abilities.flying = false;
        }
        abilities.setFlyingSpeed(VANILLA_FLY_SPEED);
        player.onUpdateAbilities();
    }

    public static void applyFlightAbilities(ServerPlayer player) {
        SkillData data = SkillUnlocks.data(player);
        if (!data.has(SkillId.FLY)) {
            return;
        }
        if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = true;
            player.getAbilities().setFlyingSpeed(VANILLA_FLY_SPEED);
        }
        player.onUpdateAbilities();
    }

    // ------------------------------------------------------------------ 公共 tick

    public static void tickServer(ServerPlayer player) {
        SkillData data = SkillUnlocks.data(player);
        SkillUnlocks.tickCooldowns(data);
        tickGrab(player, data);
        tickDash(player, data);
        tickFlight(player, data);
        tickTimers(player, data);
    }

    public static void onPreTick(ServerPlayer player) {
        SkillData data = SkillUnlocks.data(player);
        if (data.has(SkillId.FLY) && player.getAbilities().flying) {
            recordPrePosition(player, data);
        } else {
            data.posRecorded = false;
        }
    }

    // ------------------------------------------------------------------ 目标选取

    public static LivingEntity findLookTarget(Player player, double range, Vec3 aimPoint) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(range));
        AABB box = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.5);
        List<LivingEntity> list = player.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && e.isAlive() && e.isPickable());
        double wallDistSqr = eye.distanceToSqr(aimPoint);
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity entity : list) {
            AABB hitbox = entity.getBoundingBox().inflate(0.25);
            var hit = hitbox.clip(eye, end);
            double dist;
            if (hit.isPresent()) {
                dist = eye.distanceToSqr(hit.get());
            } else if (hitbox.contains(eye)) {
                dist = 0.0;
            } else {
                continue;
            }
            if (dist > wallDistSqr + 0.5) {
                continue;
            }
            if (dist < bestDist) {
                bestDist = dist;
                best = entity;
            }
        }
        return best;
    }

    private static Vec3 lookPoint(Player player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(range));
        BlockHitResult clip = player.level().clip(new ClipContext(eye, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return clip.getType() != HitResult.Type.MISS ? clip.getLocation() : end;
    }

    // ------------------------------------------------------------------ 其他

    private static void broadcastFx(ServerPlayer player, byte kind, Vec3 pos) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new SkillFxPayload(kind, pos.x, pos.y, pos.z, player.getYRot(), player.getXRot()));
    }

    private static void notifyLocked(ServerPlayer player, SkillId id) {
        player.displayClientMessage(Component.translatable("message.minecraft_1_21_1_n_b06d.locked." + id.key), true);
    }

    public static boolean isBedrock(net.minecraft.world.level.block.state.BlockState state) {
        return state.is(Blocks.BEDROCK) || state.is(Blocks.END_PORTAL_FRAME) || state.is(Blocks.END_PORTAL)
                || state.is(Blocks.END_GATEWAY) || state.is(Blocks.BARRIER) || state.is(Blocks.COMMAND_BLOCK)
                || state.is(Blocks.CHAIN_COMMAND_BLOCK) || state.is(Blocks.REPEATING_COMMAND_BLOCK)
                || state.getBlock().getExplosionResistance() >= 3600000.0F;
    }

    /** 领地/保护插件通常通过 mayInteract 拦截，这里交给服务端规则。 */
    public static boolean isProtected(ServerLevel level, BlockPos pos, ServerPlayer player) {
        return !level.mayInteract(player, pos);
    }
}
