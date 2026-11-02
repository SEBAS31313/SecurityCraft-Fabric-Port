package net.geforcemods.securitycraft.mixin.gameplay;
import net.geforcemods.securitycraft.platform.GameplayBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(RedStoneWireBlock.class)
public class RedStoneWireBlockMixin {
 @Inject(method="shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z",at=@At("HEAD"),cancellable=true)
 private static void securitycraft$connectInputs(BlockState state,Direction side,CallbackInfoReturnable<Boolean> cir){if(state.getBlock() instanceof GameplayBlock hooks)cir.setReturnValue(hooks.canConnectRedstone(state,null,BlockPos.ZERO,side));}
}
