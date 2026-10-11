package cn.blockforge.generated.minecraft1211nb06d.potion;

import cn.blockforge.generated.minecraft1211nb06d.GeneratedMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModPotions {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, GeneratedMod.MOD_ID);
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(Registries.POTION, GeneratedMod.MOD_ID);

    public static final DeferredHolder<MobEffect, SkillUnlockEffect> BURST_EFFECT =
            MOB_EFFECTS.register("burst_power", () -> new SkillUnlockEffect(MobEffectCategory.BENEFICIAL, 0xE23A12));
    public static final DeferredHolder<MobEffect, SkillUnlockEffect> BIND_EFFECT =
            MOB_EFFECTS.register("bind_power", () -> new SkillUnlockEffect(MobEffectCategory.BENEFICIAL, 0x8A3CFF));
    public static final DeferredHolder<MobEffect, SkillUnlockEffect> FLY_EFFECT =
            MOB_EFFECTS.register("fly_power", () -> new SkillUnlockEffect(MobEffectCategory.BENEFICIAL, 0x5AB4FF));
    public static final DeferredHolder<MobEffect, SkillUnlockEffect> DASH_EFFECT =
            MOB_EFFECTS.register("dash_power", () -> new SkillUnlockEffect(MobEffectCategory.BENEFICIAL, 0x2EE0C8));

    public static final DeferredHolder<Potion, Potion> BURST_POTION = POTIONS.register("burst_power",
            () -> new Potion("burst_power", new MobEffectInstance(Holder.direct(BURST_EFFECT.get()), 1, 0, false, true)));
    public static final DeferredHolder<Potion, Potion> BIND_POTION = POTIONS.register("bind_power",
            () -> new Potion("bind_power", new MobEffectInstance(Holder.direct(BIND_EFFECT.get()), 1, 0, false, true)));
    public static final DeferredHolder<Potion, Potion> FLY_POTION = POTIONS.register("fly_power",
            () -> new Potion("fly_power", new MobEffectInstance(Holder.direct(FLY_EFFECT.get()), 1, 0, false, true)));
    public static final DeferredHolder<Potion, Potion> DASH_POTION = POTIONS.register("dash_power",
            () -> new Potion("dash_power", new MobEffectInstance(Holder.direct(DASH_EFFECT.get()), 1, 0, false, true)));

    private ModPotions() {
    }
}
