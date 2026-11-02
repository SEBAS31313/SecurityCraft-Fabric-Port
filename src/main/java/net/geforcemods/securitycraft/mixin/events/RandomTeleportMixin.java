package net.geforcemods.securitycraft.mixin.events;

import com.llamalad7.mixinextras.sugar.Local;
import net.geforcemods.securitycraft.platform.GameplayEventBus;
import net.geforcemods.securitycraft.platform.events.EntityTeleportEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class RandomTeleportMixin {
    @Inject(method="randomTeleport",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;teleportTo(DDD)V",ordinal=0),cancellable=true)
    private void securitycraft$randomTeleport(double x,double requestedY,double z,boolean particles,CallbackInfoReturnable<Boolean> cir,@Local(name="y") double targetY) {
        LivingEntity entity=(LivingEntity)(Object)this;
        EntityTeleportEvent event=entity instanceof EnderMan ? new EntityTeleportEvent.EnderEntity(entity,x,targetY,z) : new EntityTeleportEvent.ItemConsumption(entity,x,targetY,z);
        if(!entity.level().isClientSide() && GameplayEventBus.EVENT_BUS.post(event).isCanceled()) cir.setReturnValue(false);
    }
}
