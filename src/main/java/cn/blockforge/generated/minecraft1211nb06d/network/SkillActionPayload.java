package cn.blockforge.generated.minecraft1211nb06d.network;

import cn.blockforge.generated.minecraft1211nb06d.GeneratedMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SkillActionPayload(byte action, int extra) implements CustomPacketPayload {
    public static final byte BURST = 1;
    public static final byte GRAB = 2;
    public static final byte DASH = 3;
    public static final byte TOGGLE_FLY = 4;
    public static final byte SPRINT_STATE = 5;
    public static final byte RELEASE_GRAB = 6;

    public static final Type<SkillActionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID, "skill_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, SkillActionPayload::action,
            ByteBufCodecs.VAR_INT, SkillActionPayload::extra,
            SkillActionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
