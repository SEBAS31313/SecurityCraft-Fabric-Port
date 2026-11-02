package net.geforcemods.securitycraft.mixin.events;

import net.geforcemods.securitycraft.platform.GameplayEventBus;
import net.geforcemods.securitycraft.platform.events.SCEvents.LivingChangeTargetEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobTargetMixin {
    @Inject(method="setTarget",at=@At("HEAD"),cancellable=true)
    private void securitycraft$target(LivingEntity target,CallbackInfo ci) {
        if(GameplayEventBus.EVENT_BUS.post(new LivingChangeTargetEvent((Mob)(Object)this,target)).isCanceled()) ci.cancel();
    }
}
