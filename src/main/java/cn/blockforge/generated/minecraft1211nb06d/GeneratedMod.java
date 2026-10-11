package cn.blockforge.generated.minecraft1211nb06d;

import cn.blockforge.generated.minecraft1211nb06d.item.ModItems;
import cn.blockforge.generated.minecraft1211nb06d.network.ModNetworking;
import cn.blockforge.generated.minecraft1211nb06d.potion.ModPotions;
import cn.blockforge.generated.minecraft1211nb06d.skill.ModAttachments;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(GeneratedMod.MOD_ID)
public final class GeneratedMod {
    public static final String MOD_ID = "minecraft_1_21_1_n_b06d";

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("skills",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + MOD_ID + ".skills"))
                    .icon(() -> new ItemStack(ModItems.BURST_CORE.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.BURST_CRYSTAL.get());
                        output.accept(ModItems.BURST_CORE.get());
                        output.accept(ModItems.BIND_STONE.get());
                        output.accept(ModItems.BIND_CORE.get());
                        output.accept(ModItems.SHULKER_NUCLEUS.get());
                        output.accept(ModItems.WING_POWDER.get());
                        output.accept(ModItems.HOVER_SHARD.get());
                        output.accept(ModItems.HOVER_CORE.get());
                        output.accept(ModItems.DASH_SHARD.get());
                        output.accept(ModItems.DASH_CORE.get());
                        output.accept(ModItems.BURST_POTION.get());
                        output.accept(ModItems.BIND_POTION.get());
                        output.accept(ModItems.FLY_POTION.get());
                        output.accept(ModItems.DASH_POTION.get());
                    })
                    .build());

    public GeneratedMod(IEventBus modBus) {
        ModItems.ITEMS.register(modBus);
        ModPotions.MOB_EFFECTS.register(modBus);
        ModPotions.POTIONS.register(modBus);
        ModAttachments.ATTACHMENT_TYPES.register(modBus);
        TABS.register(modBus);
        modBus.addListener(ModNetworking::register);
    }
}
