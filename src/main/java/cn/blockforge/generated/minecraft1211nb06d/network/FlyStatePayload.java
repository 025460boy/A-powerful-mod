package cn.blockforge.generated.minecraft1211nb06d.network;

import cn.blockforge.generated.minecraft1211nb06d.GeneratedMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端每 tick 上报的飞行状态：加速进度（0-100）与按键标志位。
 * bit0 = 有水平移动输入，bit1 = 有垂直输入（上升/下降），bit2 = 按住加速键。
 */
public record FlyStatePayload(byte progress, byte flags) implements CustomPacketPayload {
    public static final int FLAG_HORIZONTAL = 1;
    public static final int FLAG_VERTICAL = 2;
    public static final int FLAG_BOOST = 4;

    public static final Type<FlyStatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID, "fly_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FlyStatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, FlyStatePayload::progress,
            ByteBufCodecs.BYTE, FlyStatePayload::flags,
            FlyStatePayload::new
    );

    public boolean horizontal() {
        return (flags & FLAG_HORIZONTAL) != 0;
    }

    public boolean vertical() {
        return (flags & FLAG_VERTICAL) != 0;
    }

    public boolean boost() {
        return (flags & FLAG_BOOST) != 0;
    }

    public static FlyStatePayload of(float progress, boolean horizontal, boolean vertical, boolean boost) {
        byte p = (byte) Math.max(0, Math.min(100, Math.round(progress * 100.0F)));
        byte f = (byte) ((horizontal ? FLAG_HORIZONTAL : 0) | (vertical ? FLAG_VERTICAL : 0) | (boost ? FLAG_BOOST : 0));
        return new FlyStatePayload(p, f);
    }

    public float progressFloat() {
        return (progress & 0xFF) / 100.0F;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
