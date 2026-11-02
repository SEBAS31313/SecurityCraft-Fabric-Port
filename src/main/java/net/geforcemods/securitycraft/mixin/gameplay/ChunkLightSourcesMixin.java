package net.geforcemods.securitycraft.mixin.gameplay;

import java.util.function.BiConsumer;
import net.geforcemods.securitycraft.api.IDisguisable;
import net.geforcemods.securitycraft.platform.AuxiliaryLightManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkAccess.class)
public class ChunkLightSourcesMixin {
    @Inject(method = "findBlockLightSources", at = @At("TAIL"))
    private void securitycraft$disguisedSources(BiConsumer<BlockPos,BlockState> consumer, CallbackInfo ci) {
        if ((Object)this instanceof LevelChunk chunk) {
            AuxiliaryLightManager.get(chunk.getLevel()).forEachLightSource(chunk.getPos(), (pos, emission) -> {
                BlockState state = chunk.getBlockState(pos);
                if (state.getBlock() instanceof IDisguisable) consumer.accept(pos, state);
            });
        }
    }
}
