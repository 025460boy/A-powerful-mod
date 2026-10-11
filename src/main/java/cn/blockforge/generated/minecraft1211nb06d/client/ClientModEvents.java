package cn.blockforge.generated.minecraft1211nb06d.client;

import cn.blockforge.generated.minecraft1211nb06d.GeneratedMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent event) {
        event.register(ClientSkillHandler.KEY_BURST);
        event.register(ClientSkillHandler.KEY_GRAB);
        event.register(ClientSkillHandler.KEY_DASH);
        event.register(ClientSkillHandler.KEY_UPGRADE);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(SkillHudOverlay::register);
    }
}
