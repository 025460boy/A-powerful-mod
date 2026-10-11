package cn.blockforge.generated.minecraft1211nb06d.skill;

import cn.blockforge.generated.minecraft1211nb06d.GeneratedMod;
import cn.blockforge.generated.minecraft1211nb06d.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.GlowSquid;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class SkillEvents {
    private SkillEvents() {
    }

    // --------------------------------------------------------------- 酿造配方

    @SubscribeEvent
    public static void onBrewing(RegisterBrewingRecipesEvent event) {
        var builder = event.getBuilder();
        builder.addRecipe(Ingredient.of(Items.POTION), Ingredient.of(ModItems.BURST_CORE.get()),
                new ItemStack(ModItems.BURST_POTION.get()));
        builder.addRecipe(Ingredient.of(Items.POTION), Ingredient.of(ModItems.BIND_CORE.get()),
                new ItemStack(ModItems.BIND_POTION.get()));
        builder.addRecipe(Ingredient.of(Items.POTION), Ingredient.of(ModItems.HOVER_CORE.get()),
                new ItemStack(ModItems.FLY_POTION.get()));
        builder.addRecipe(Ingredient.of(Items.POTION), Ingredient.of(ModItems.DASH_CORE.get()),
                new ItemStack(ModItems.DASH_POTION.get()));
    }

    // --------------------------------------------------------------- 罕见掉落

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        var random = entity.getRandom();
        ItemStack extra = null;
        if (entity instanceof Blaze) {
            // 平均每 15 只烈焰人掉落 1 个爆裂晶
            if (random.nextInt(15) == 0) {
                extra = new ItemStack(ModItems.BURST_CRYSTAL.get());
            }
        } else if (entity instanceof Vindicator) {
            // 卫道士（Vindicator）平均每 6 只掉落 1 个禁锢石
            if (random.nextInt(6) == 0) {
                extra = new ItemStack(ModItems.BIND_STONE.get());
            }
        } else if (entity instanceof Shulker) {
            if (random.nextInt(20) == 0) {
                extra = new ItemStack(ModItems.SHULKER_NUCLEUS.get());
            }
        } else if (entity instanceof GlowSquid) {
            if (random.nextInt(10) == 0) {
                extra = new ItemStack(ModItems.DASH_SHARD.get());
            }
        } else if (entity instanceof Squid) {
            if (random.nextInt(20) == 0) {
                extra = new ItemStack(ModItems.DASH_SHARD.get());
            }
        }
        if (extra != null) {
            ItemEntity drop = new ItemEntity(entity.level(),
                    entity.getX(), entity.getY() + 0.2, entity.getZ(), extra);
            drop.setPickUpDelay(30);
            event.getDrops().add(drop);
        }
    }

    // --------------------------------------------------------------- 玩家 tick

    @SubscribeEvent
    public static void onPlayerTickPre(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SkillActions.onPreTick(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTickPost(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SkillActions.tickServer(player);
        }
    }

    // --------------------------------------------------------------- 数据同步

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        onSyncStart(event.getEntity());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        onSyncStart(event.getEntity());
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        onSyncStart(event.getEntity());
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        SkillUnlocks.sync(event.getEntity());
    }

    private static void onSyncStart(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            SkillActions.applyFlightAbilities(serverPlayer);
            SkillUnlocks.sync(serverPlayer);
        }
    }

    // --------------------------------------------------------------- 抓取约束

    @SubscribeEvent
    public static void onEntityTeleport(EntityTeleportEvent event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide || !(entity instanceof LivingEntity)) {
            return;
        }
        for (Player player : entity.level().getEntitiesOfClass(Player.class,
                entity.getBoundingBox().inflate(32.0, 32.0, 32.0))) {
            if (SkillUnlocks.data(player).grabbedId == entity.getId()) {
                event.setCanceled(true);
                return;
            }
        }
    }

    // --------------------------------------------------------------- 伤害处理

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        SkillData data = SkillUnlocks.data(player);
        var source = event.getSource();

        // 玩家无敌：除 /kill（generic_kill）外，任何伤害来源都不生效（含摔落、火焰、溺水、
        // 饥饿、中毒、凋零、雷击、爆炸、玩家攻击、虚空/世界边界等）。
        if (!source.is(DamageTypes.GENERIC_KILL)) {
            event.setCanceled(true);
            // 兜底：若血量已被旁路修改（如直接 setHealth），恢复满血并解除死亡状态。
            if (player.getHealth() < player.getMaxHealth() || player.isDeadOrDying()) {
                player.setHealth(player.getMaxHealth());
            }
            player.displayClientMessage(Component.translatable("message.minecraft_1_21_1_n_b06d.shielded"), true);
            return;
        }
        // /kill 放行后继续执行原有技能相关防护逻辑（对本分支无影响，仅保留兼容）。
        if ((data.skillFlying || data.dashing) && source.is(DamageTypeTags.IS_FALL)) {
            event.setCanceled(true);
        }
        if (data.dashing && source.is(DamageTypeTags.BYPASSES_COOLDOWN) && source.getEntity() == null) {
            event.setCanceled(true);
        }
    }
}
