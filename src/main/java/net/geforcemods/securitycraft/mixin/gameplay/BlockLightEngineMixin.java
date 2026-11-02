package net.geforcemods.securitycraft.mixin.gameplay;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.geforcemods.securitycraft.api.IDisguisable;
import net.geforcemods.securitycraft.platform.AuxiliaryLightManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.BlockLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/** Replaces only the emission value, keeping vanilla's enabled-section and propagation checks. */
@Mixin(BlockLightEngine.class)
public class BlockLightEngineMixin {
    @WrapOperation(method = "getEmission", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"))
    private int securitycraft$disguiseLight(BlockState state, Operation<Integer> original, long packedPos, BlockState sourceState) {
        return securitycraft$emission(state, BlockPos.of(packedPos), original.call(state));
    }

    @WrapOperation(method = "lambda$propagateLightSources$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"))
    private int securitycraft$disguiseSourceLight(BlockState state, Operation<Integer> original, BlockPos pos, BlockState sourceState) {
        return securitycraft$emission(state, pos, original.call(state));
    }

    @Unique
    private int securitycraft$emission(BlockState state, BlockPos pos, int original) {
        // Air and unloaded borders must never cause block entity lookups or additional chunk loads.
        if (!(state.getBlock() instanceof IDisguisable))
            return original;
        var level = ((LightEngineAccess) this).securitycraft$getChunkSource().getLevel();
        int light = AuxiliaryLightManager.get(level).getLightAt(pos);
        return light > 0 ? light : original;
    }
}
