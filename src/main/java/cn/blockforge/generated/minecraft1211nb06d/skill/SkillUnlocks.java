package cn.blockforge.generated.minecraft1211nb06d.skill;

import cn.blockforge.generated.minecraft1211nb06d.network.SkillSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public final class SkillUnlocks {
    private SkillUnlocks() {
    }

    public static SkillData data(Player player) {
        return player.getData(ModAttachments.SKILLS);
    }

    public static boolean has(Player player, SkillId id) {
        return data(player).has(id);
    }

    public static void unlock(Player player, SkillId id) {
        SkillData skillData = data(player);
        boolean wasUnlocked = skillData.has(id);
        skillData.unlock(id);
        if (!wasUnlocked && id == SkillId.FLY) {
            skillData.flyTicksLeft = SkillUpgrades.flyDurationTicks(skillData.flyLevel);
        }
        player.setData(ModAttachments.SKILLS, skillData);
        if (id == SkillId.FLY && player instanceof ServerPlayer serverPlayer) {
            SkillActions.applyFlightAbilities(serverPlayer);
        }
        sync(player);
    }

    public static void sync(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SkillSyncPayload(data(player).copyForSync()));
        }
    }

    public static void tickCooldowns(SkillData skillData) {
        if (skillData.dashCooldown > 0) {
            skillData.dashCooldown--;
        }
        if (skillData.burstCooldown > 0) {
            skillData.burstCooldown--;
        }
        if (skillData.grabCooldown > 0) {
            skillData.grabCooldown--;
        }
    }
}
