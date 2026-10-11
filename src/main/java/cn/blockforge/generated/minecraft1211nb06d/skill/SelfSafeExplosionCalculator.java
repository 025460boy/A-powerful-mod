package cn.blockforge.generated.minecraft1211nb06d.skill;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.Optional;

public class SelfSafeExplosionCalculator extends ExplosionDamageCalculator {
    private final ServerPlayer owner;

    public SelfSafeExplosionCalculator(ServerPlayer owner) {
        this.owner = owner;
    }

    @Override
    public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
        return entity != owner && super.shouldDamageEntity(explosion, entity);
    }

    @Override
    public float getEntityDamageAmount(Explosion explosion, Entity entity) {
        return entity == owner ? 0.0F : super.getEntityDamageAmount(explosion, entity);
    }

    @Override
    public boolean shouldBlockExplode(Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, float power) {
        if (SkillActions.isBedrock(state)) {
            return false;
        }
        if (level instanceof ServerLevel serverLevel && SkillActions.isProtected(serverLevel, pos, owner)) {
            return false;
        }
        return super.shouldBlockExplode(explosion, level, pos, state, power);
    }

    @Override
    public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter level, BlockPos pos,
                                                       BlockState state, FluidState fluid) {
        if (SkillActions.isBedrock(state)) {
            return Optional.of(3600000.0F);
        }
        return super.getBlockExplosionResistance(explosion, level, pos, state, fluid);
    }
}
