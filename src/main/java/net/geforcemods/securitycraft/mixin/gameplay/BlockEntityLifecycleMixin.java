package net.geforcemods.securitycraft.mixin.gameplay;
import net.geforcemods.securitycraft.platform.BlockEntityLifecycle;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(BlockEntity.class)
public class BlockEntityLifecycleMixin {
 @Inject(method="setLevel",at=@At("TAIL"))
 private void securitycraft$loaded(Level level,CallbackInfo ci){if(this instanceof BlockEntityLifecycle hooks)hooks.onLoad();}
 @Inject(method={"loadWithComponents","loadCustomOnly"},at=@At("TAIL"))
 private void securitycraft$updated(ValueInput input,CallbackInfo ci){
  BlockEntity entity=(BlockEntity)(Object)this;
  Level level=entity.getLevel();
  if(level!=null&&entity.getBlockState().getBlock() instanceof net.geforcemods.securitycraft.api.IDisguisable){
   int light=net.geforcemods.securitycraft.api.IDisguisable.getDisguisedBlockState(entity).map(net.minecraft.world.level.block.state.BlockState::getLightEmission).orElse(0);
   net.geforcemods.securitycraft.platform.AuxiliaryLightManager.get(level).setLightAt(entity.getBlockPos(),light);
  }
  if(this instanceof BlockEntityLifecycle hooks&&level!=null&&level.isClientSide())hooks.onDataPacket(null,input);
 }
}
