package net.geforcemods.securitycraft.mixin.gameplay;
import net.geforcemods.securitycraft.platform.GameplayBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.NeighborUpdater;
import net.minecraft.world.level.redstone.Orientation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(NeighborUpdater.class)
public interface NeighborUpdaterMixin {
 @Inject(method="executeUpdate",at=@At("TAIL"))
 private static void securitycraft$neighborData(Level level,BlockState state,BlockPos pos,Block changed,Orientation orientation,boolean moved,CallbackInfo ci){
  if(state.getBlock() instanceof GameplayBlock hooks)hooks.onNeighborChange(state,level,pos,pos);
 }
}
