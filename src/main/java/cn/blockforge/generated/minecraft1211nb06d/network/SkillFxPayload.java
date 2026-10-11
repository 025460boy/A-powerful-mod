package cn.blockforge.generated.minecraft1211nb06d.network;

import cn.blockforge.generated.minecraft1211nb06d.GeneratedMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SkillFxPayload(byte kind, double x, double y, double z, float yaw, float pitch)
        implements CustomPacketPayload {
    public static final byte BURST = 1;
    public static final byte DASH = 2;
    public static final byte FLY_BOOST = 3;
    public static final byte GRAB = 4;
    public static final byte BURST_EXPLOSIVE = 5;

    public static final Type<SkillFxPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID, "skill_fx"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillFxPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, SkillFxPayload::kind,
            ByteBufCodecs.DOUBLE, SkillFxPayload::x,
            ByteBufCodecs.DOUBLE, SkillFxPayload::y,
            ByteBufCodecs.DOUBLE, SkillFxPayload::z,
            ByteBufCodecs.FLOAT, SkillFxPayload::yaw,
            ByteBufCodecs.FLOAT, SkillFxPayload::pitch,
            SkillFxPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
