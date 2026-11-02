package net.geforcemods.securitycraft.mixin.events;

import net.geforcemods.securitycraft.platform.GameplayEventBus;
import net.geforcemods.securitycraft.platform.events.SCEvents.LivingDestroyBlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelDestructionMixin {
    @Inject(method="destroyBlock",at=@At("HEAD"),cancellable=true)
    private void securitycraft$destroy(BlockPos pos,boolean drops,Entity entity,int recursion,CallbackInfoReturnable<Boolean> cir) {
        if(entity instanceof LivingEntity living && GameplayEventBus.EVENT_BUS.post(new LivingDestroyBlockEvent(living,((Level)(Object)this).getBlockState(pos))).isCanceled()) cir.setReturnValue(false);
    }
}
