package net.geforcemods.securitycraft.mixin.events;

import net.geforcemods.securitycraft.platform.GameplayEventBus;
import net.geforcemods.securitycraft.platform.events.SCEvents.EntityInvulnerabilityCheckEvent;
import net.geforcemods.securitycraft.platform.events.SCEvents.EntityMountEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityEventsMixin {
    @Inject(method="isInvulnerableToBase",at=@At("RETURN"),cancellable=true)
    private void securitycraft$invulnerability(DamageSource source,CallbackInfoReturnable<Boolean> cir) {
        var event=new EntityInvulnerabilityCheckEvent((Entity)(Object)this,source);
        event.setInvulnerable(cir.getReturnValue());
        GameplayEventBus.EVENT_BUS.post(event);
        cir.setReturnValue(event.isInvulnerable());
    }
    @Inject(method="stopRiding",at=@At("HEAD"),cancellable=true)
    private void securitycraft$dismount(CallbackInfo ci) {
        Entity entity=(Entity)(Object)this;
        if(entity.getVehicle()!=null && GameplayEventBus.EVENT_BUS.post(new EntityMountEvent(entity,entity.getVehicle(),true)).isCanceled()) ci.cancel();
    }
}
