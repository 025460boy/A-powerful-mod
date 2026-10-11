package cn.blockforge.generated.minecraft1211nb06d.network;

import cn.blockforge.generated.minecraft1211nb06d.GeneratedMod;
import cn.blockforge.generated.minecraft1211nb06d.skill.SkillActions;
import cn.blockforge.generated.minecraft1211nb06d.skill.SkillUpgrades;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {
    private ModNetworking() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(GeneratedMod.MOD_ID).versioned("1");
        registrar.playToServer(SkillActionPayload.TYPE, SkillActionPayload.STREAM_CODEC, ModNetworking::handleAction);
        registrar.playToServer(FlyStatePayload.TYPE, FlyStatePayload.STREAM_CODEC, ModNetworking::handleFlyState);
        registrar.playToServer(SkillUpgradePayload.TYPE, SkillUpgradePayload.STREAM_CODEC, ModNetworking::handleUpgrade);
        if (FMLEnvironment.dist.isClient()) {
            registrar.playToClient(SkillSyncPayload.TYPE, SkillSyncPayload.STREAM_CODEC, ClientPayloads::handleSync);
            registrar.playToClient(SkillFxPayload.TYPE, SkillFxPayload.STREAM_CODEC, ClientPayloads::handleFx);
        } else {
            registrar.playToClient(SkillSyncPayload.TYPE, SkillSyncPayload.STREAM_CODEC, (payload, ctx) -> {
            });
            registrar.playToClient(SkillFxPayload.TYPE, SkillFxPayload.STREAM_CODEC, (payload, ctx) -> {
            });
        }
    }

    private static void handleAction(SkillActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                SkillActions.handle(player, payload);
            }
        });
    }

    private static void handleFlyState(FlyStatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                SkillActions.applyFlyState(player, payload);
            }
        });
    }

    private static void handleUpgrade(SkillUpgradePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                SkillUpgrades.upgrade(player, payload.skill());
            }
        });
    }
}
