package cn.blockforge.generated.minecraft1211nb06d.network;

import cn.blockforge.generated.minecraft1211nb06d.GeneratedMod;
import cn.blockforge.generated.minecraft1211nb06d.skill.SkillData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SkillSyncPayload(SkillData data) implements CustomPacketPayload {
    public static final Type<SkillSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID, "skill_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillSyncPayload> STREAM_CODEC =
            SkillData.STREAM_CODEC.map(SkillSyncPayload::new, SkillSyncPayload::data);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
