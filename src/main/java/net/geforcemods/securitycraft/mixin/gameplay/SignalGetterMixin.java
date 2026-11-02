package net.geforcemods.securitycraft.mixin.gameplay;
import net.geforcemods.securitycraft.platform.GameplayBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.SignalGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(SignalGetter.class)
public interface SignalGetterMixin {
 @Inject(method="getSignal",at=@At("HEAD"),cancellable=true)
 private void securitycraft$weakPower(BlockPos pos,Direction side,CallbackInfoReturnable<Integer> cir){
  SignalGetter level=(SignalGetter)this;var state=level.getBlockState(pos);
  if(state.getBlock() instanceof GameplayBlock hooks&&!hooks.shouldCheckWeakPower(state,level,pos,side))cir.setReturnValue(state.getSignal(level,pos,side));
 }
}
