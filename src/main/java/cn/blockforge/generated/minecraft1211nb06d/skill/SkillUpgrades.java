package cn.blockforge.generated.minecraft1211nb06d.skill;

import cn.blockforge.generated.minecraft1211nb06d.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** 技能各等级的升级成本、效果参数与服务端升级校验。 */
public final class SkillUpgrades {
    private SkillUpgrades() {
    }

    public static int maxLevel(SkillId id) {
        return switch (id) {
            case BURST -> 4;
            case GRAB, FLY -> 3;
            case DASH -> 1;
        };
    }

    /** targetLevel 是升级后要达到的等级。 */
    public static int costCount(SkillId id, int targetLevel) {
        return switch (id) {
            case BURST -> switch (targetLevel) { case 2 -> 1; case 3 -> 3; case 4 -> 5; default -> 0; };
            case GRAB -> switch (targetLevel) { case 2 -> 3; case 3 -> 5; default -> 0; };
            case FLY -> switch (targetLevel) { case 2 -> 2; case 3 -> 5; default -> 0; };
            case DASH -> 0;
        };
    }

    public static Item costItem(SkillId id) {
        return switch (id) {
            case BURST -> ModItems.BURST_CRYSTAL.get();
            case GRAB -> ModItems.BIND_STONE.get();
            case FLY -> ModItems.HOVER_SHARD.get();
            case DASH -> Items.AIR;
        };
    }

    public static int burstDamage(int level) {
        return switch (level) { case 1 -> 150; case 2, 3 -> 300; default -> 800; };
    }

    public static boolean burstExplodes(int level) {
        return level >= 3;
    }

    /** -1 表示不限时。 */
    public static int grabDurationTicks(int level) {
        return switch (level) { case 1 -> 5 * 20; case 2 -> 30 * 20; default -> -1; };
    }

    /** -1 表示不限时。 */
    public static int flyDurationTicks(int level) {
        return switch (level) { case 1 -> 25 * 20; case 2 -> 60 * 20; default -> -1; };
    }

    /** Ctrl 加速在飞行 3 级解锁。 */
    public static boolean flyBoostUnlocked(int level) {
        return level >= 3;
    }

    public static String materialNameKey(SkillId id) {
        return "item.minecraft_1_21_1_n_b06d." + switch (id) {
            case BURST -> "burst_crystal";
            case GRAB -> "bind_stone";
            case FLY -> "hover_shard";
            case DASH -> "dash_shard";
        };
    }

    /** 服务端校验并执行一次升级请求（材料、解锁、等级上限全部在服务端复核）。 */
    public static void upgrade(ServerPlayer player, byte skillOrdinal) {
        if (skillOrdinal < 0 || skillOrdinal >= SkillId.VALUES.length) {
            return;
        }
        SkillId id = SkillId.VALUES[skillOrdinal];
        SkillData data = SkillUnlocks.data(player);
        if (!data.has(id)) {
            notify(player, "message.minecraft_1_21_1_n_b06d.upgrade.locked", id);
            return;
        }
        int current = data.level(id);
        int max = maxLevel(id);
        if (current >= max) {
            notify(player, "message.minecraft_1_21_1_n_b06d.upgrade.max", id);
            return;
        }
        int target = current + 1;
        int need = costCount(id, target);
        Item item = costItem(id);
        Inventory inventory = player.getInventory();
        if (count(inventory, item) < need) {
            notifyMissing(player, id, item, need);
            return;
        }
        remove(inventory, item, need);
        data.setLevel(id, target);
        if (id == SkillId.FLY) {
            data.flyTicksLeft = flyDurationTicks(target);
            SkillActions.applyFlightAbilities(player);
        } else if (id == SkillId.GRAB && data.grabbedId >= 0) {
            data.grabTicksLeft = grabDurationTicks(target);
        }
        player.setData(ModAttachments.SKILLS, data);
        SkillUnlocks.sync(player);
        player.displayClientMessage(Component.translatable(
                "message.minecraft_1_21_1_n_b06d.upgrade.success",
                skillName(id), target), true);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7F, 1.1F + 0.1F * target);
    }

    public static int count(Inventory inventory, Item item) {
        int total = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    public static void remove(Inventory inventory, Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.is(item)) {
                continue;
            }
            int take = Math.min(stack.getCount(), remaining);
            inventory.removeItem(i, take);
            remaining -= take;
        }
        inventory.setChanged();
    }

    private static void notify(ServerPlayer player, String key, SkillId id) {
        player.displayClientMessage(Component.translatable(key, skillName(id)), true);
    }

    private static void notifyMissing(ServerPlayer player, SkillId id, Item item, int need) {
        player.displayClientMessage(Component.translatable(
                "message.minecraft_1_21_1_n_b06d.upgrade.missing",
                skillName(id), item.getDescription(), need), true);
    }

    public static Component skillName(SkillId id) {
        return Component.translatable("skill.minecraft_1_21_1_n_b06d." + id.key);
    }
}
