package net.geforcemods.securitycraft.mixin.gameplay;
import java.util.function.BiConsumer;
import net.geforcemods.securitycraft.platform.GameplayBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(TrunkPlacer.class)
public class TrunkPlacerMixin {
 @Inject(method="placeBelowTrunkBlock",at=@At("HEAD"),cancellable=true)
 private static void securitycraft$preserveReinforcedSoil(WorldGenLevel level,BiConsumer<BlockPos,BlockState> setter,RandomSource random,BlockPos pos,TreeConfiguration config,CallbackInfo ci){
  BlockState soil=level.getBlockState(pos);
  if(soil.getBlock() instanceof GameplayBlock hooks&&hooks.onTreeGrow(soil,level,setter,random,pos,config))ci.cancel();
 }
}
