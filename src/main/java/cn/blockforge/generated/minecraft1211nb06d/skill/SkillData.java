package cn.blockforge.generated.minecraft1211nb06d.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 玩家技能解锁、等级、计时与运行状态。 */
public final class SkillData {
    public static final Codec<SkillData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("unlocks").forGetter(d -> d.unlocks),
            Codec.INT.optionalFieldOf("grabbedId", -1).forGetter(d -> d.grabbedId),
            Codec.INT.optionalFieldOf("dashCooldown", 0).forGetter(d -> d.dashCooldown),
            Codec.INT.optionalFieldOf("burstCooldown", 0).forGetter(d -> d.burstCooldown),
            Codec.INT.optionalFieldOf("grabCooldown", 0).forGetter(d -> d.grabCooldown),
            Codec.BOOL.optionalFieldOf("skillFlying", false).forGetter(d -> d.skillFlying),
            Codec.INT.optionalFieldOf("burstLevel", 1).forGetter(d -> d.burstLevel),
            Codec.INT.optionalFieldOf("grabLevel", 1).forGetter(d -> d.grabLevel),
            Codec.INT.optionalFieldOf("flyLevel", 1).forGetter(d -> d.flyLevel),
            Codec.INT.optionalFieldOf("dashLevel", 1).forGetter(d -> d.dashLevel),
            Codec.INT.optionalFieldOf("grabTicksLeft", -1).forGetter(d -> d.grabTicksLeft),
            Codec.INT.optionalFieldOf("flyTicksLeft", -1).forGetter(d -> d.flyTicksLeft)
    ).apply(instance, SkillData::fromSave));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                ByteBufCodecs.VAR_INT.encode(buf, data.unlocks);
                ByteBufCodecs.VAR_INT.encode(buf, data.grabbedId);
                ByteBufCodecs.VAR_INT.encode(buf, data.dashCooldown);
                ByteBufCodecs.VAR_INT.encode(buf, data.burstCooldown);
                ByteBufCodecs.VAR_INT.encode(buf, data.grabCooldown);
                ByteBufCodecs.BOOL.encode(buf, data.skillFlying);
                ByteBufCodecs.VAR_INT.encode(buf, data.burstLevel);
                ByteBufCodecs.VAR_INT.encode(buf, data.grabLevel);
                ByteBufCodecs.VAR_INT.encode(buf, data.flyLevel);
                ByteBufCodecs.VAR_INT.encode(buf, data.dashLevel);
                ByteBufCodecs.VAR_INT.encode(buf, data.grabTicksLeft);
                ByteBufCodecs.VAR_INT.encode(buf, data.flyTicksLeft);
            },
            buf -> fromSave(
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf)));

    public int unlocks;
    public int grabbedId = -1;
    public int dashCooldown;
    public int burstCooldown;
    public int grabCooldown;
    public boolean skillFlying;
    public int burstLevel = 1;
    public int grabLevel = 1;
    public int flyLevel = 1;
    public int dashLevel = 1;
    public int grabTicksLeft = -1;
    public int flyTicksLeft = -1;

    // 不存档/不同步的逐 tick 状态
    public float flyProgress;
    public boolean flyHorizontal;
    public boolean flyVertical;
    public boolean sprintHeld;
    public boolean boostedBurstPlayed;
    public boolean dashing;
    public int dashTicksLeft;
    public double dashDirX;
    public double dashDirY;
    public double dashDirZ;
    public double lastX;
    public double lastZ;
    public boolean posRecorded;
    public int timerSyncTicks;

    public SkillData() {
    }

    public static SkillData fromSave(int unlocks, int grabbedId, int dashCooldown, int burstCooldown,
                                     int grabCooldown, boolean skillFlying, int burstLevel, int grabLevel,
                                     int flyLevel, int dashLevel, int grabTicksLeft, int flyTicksLeft) {
        SkillData data = new SkillData();
        data.unlocks = unlocks;
        data.grabbedId = grabbedId;
        data.dashCooldown = dashCooldown;
        data.burstCooldown = burstCooldown;
        data.grabCooldown = grabCooldown;
        data.skillFlying = skillFlying;
        data.burstLevel = clampLevel(burstLevel, 4);
        data.grabLevel = clampLevel(grabLevel, 3);
        data.flyLevel = clampLevel(flyLevel, 3);
        data.dashLevel = clampLevel(dashLevel, 1);
        data.grabTicksLeft = grabTicksLeft;
        data.flyTicksLeft = flyTicksLeft;
        return data;
    }

    private static int clampLevel(int level, int max) {
        return Math.max(1, Math.min(max, level));
    }

    public int level(SkillId id) {
        return switch (id) {
            case BURST -> burstLevel;
            case GRAB -> grabLevel;
            case FLY -> flyLevel;
            case DASH -> dashLevel;
        };
    }

    public void setLevel(SkillId id, int level) {
        switch (id) {
            case BURST -> burstLevel = clampLevel(level, 4);
            case GRAB -> grabLevel = clampLevel(level, 3);
            case FLY -> flyLevel = clampLevel(level, 3);
            case DASH -> dashLevel = clampLevel(level, 1);
        }
    }

    public boolean has(SkillId id) {
        return (unlocks & id.mask()) != 0;
    }

    public void unlock(SkillId id) {
        unlocks |= id.mask();
    }

    public SkillData copyForSync() {
        return fromSave(unlocks, grabbedId, dashCooldown, burstCooldown, grabCooldown, skillFlying,
                burstLevel, grabLevel, flyLevel, dashLevel, grabTicksLeft, flyTicksLeft);
    }

    public void resetFlight() {
        flyProgress = 0.0F;
        flyHorizontal = false;
        flyVertical = false;
        sprintHeld = false;
        boostedBurstPlayed = false;
    }
}
