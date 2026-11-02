package net.geforcemods.securitycraft.mixin.gameplay;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.geforcemods.securitycraft.platform.GameplayBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(Entity.class)
public class EntityStepSoundMixin {
 @WrapOperation(method="playStepSound",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"))
 private SoundType securitycraft$disguisedSound(BlockState state,Operation<SoundType> original,BlockPos pos,BlockState onState){
  Entity entity=(Entity)(Object)this;
  return state.getBlock() instanceof GameplayBlock hooks?hooks.getSoundType(state,entity.level(),pos,entity):original.call(state);
 }
}
