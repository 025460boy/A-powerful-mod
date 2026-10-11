package cn.blockforge.generated.minecraft1211nb06d.item;

import cn.blockforge.generated.minecraft1211nb06d.GeneratedMod;
import cn.blockforge.generated.minecraft1211nb06d.skill.SkillId;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GeneratedMod.MOD_ID);

    private static DeferredItem<Item> mat(String name, Rarity rarity, Item.Properties extra) {
        return ITEMS.register(name, () -> new TooltipItem(
                extra == null ? new Item.Properties().rarity(rarity) : extra.rarity(rarity), name));
    }

    public static final DeferredItem<Item> BURST_CRYSTAL = mat("burst_crystal", Rarity.UNCOMMON, null);
    public static final DeferredItem<Item> BURST_CORE = mat("burst_core", Rarity.RARE,
            new Item.Properties().stacksTo(16).fireResistant());
    public static final DeferredItem<Item> BIND_STONE = mat("bind_stone", Rarity.UNCOMMON, null);
    public static final DeferredItem<Item> BIND_CORE = mat("bind_core", Rarity.RARE,
            new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> SHULKER_NUCLEUS = mat("shulker_nucleus", Rarity.UNCOMMON, null);
    public static final DeferredItem<Item> WING_POWDER = mat("wing_powder", Rarity.COMMON, null);
    public static final DeferredItem<Item> HOVER_SHARD = mat("hover_shard", Rarity.UNCOMMON, null);
    public static final DeferredItem<Item> HOVER_CORE = mat("hover_core", Rarity.RARE,
            new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> DASH_SHARD = mat("dash_shard", Rarity.UNCOMMON, null);
    public static final DeferredItem<Item> DASH_CORE = mat("dash_core", Rarity.RARE,
            new Item.Properties().stacksTo(16));

    public static final DeferredItem<SkillPotionItem> BURST_POTION = ITEMS.register("burst_potion",
            () -> new SkillPotionItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), SkillId.BURST));
    public static final DeferredItem<SkillPotionItem> BIND_POTION = ITEMS.register("bind_potion",
            () -> new SkillPotionItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), SkillId.GRAB));
    public static final DeferredItem<SkillPotionItem> FLY_POTION = ITEMS.register("fly_potion",
            () -> new SkillPotionItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), SkillId.FLY));
    public static final DeferredItem<SkillPotionItem> DASH_POTION = ITEMS.register("dash_potion",
            () -> new SkillPotionItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), SkillId.DASH));

    private ModItems() {
    }
}
