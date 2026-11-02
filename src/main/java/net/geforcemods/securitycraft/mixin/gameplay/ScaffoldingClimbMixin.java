package net.geforcemods.securitycraft.mixin.gameplay;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedScaffoldingBlock;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LivingEntity.class)
public class ScaffoldingClimbMixin {
 @Inject(method="onClimbable",at=@At("RETURN"),cancellable=true)
 private void securitycraft$ownedScaffolding(CallbackInfoReturnable<Boolean> cir){
  LivingEntity entity=(LivingEntity)(Object)this;if(entity.isSpectator())return;var pos=entity.blockPosition();
  if(entity.level().getBlockState(pos).getBlock() instanceof ReinforcedScaffoldingBlock scaffolding)cir.setReturnValue(scaffolding.ownsScaffolding(entity.level(),pos,entity));
 }
}
