package cn.blockforge.generated.minecraft1211nb06d.network;

import cn.blockforge.generated.minecraft1211nb06d.GeneratedMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 客户端申请将指定技能升一级；材料与等级由服务端验证。 */
public record SkillUpgradePayload(byte skill) implements CustomPacketPayload {
    public static final Type<SkillUpgradePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID, "skill_upgrade"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SkillUpgradePayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.BYTE, SkillUpgradePayload::skill, SkillUpgradePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
