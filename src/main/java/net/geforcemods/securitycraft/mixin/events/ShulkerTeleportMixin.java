package net.geforcemods.securitycraft.mixin.events;

import com.llamalad7.mixinextras.sugar.Local;
import net.geforcemods.securitycraft.platform.GameplayEventBus;
import net.geforcemods.securitycraft.platform.events.EntityTeleportEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.Shulker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Shulker.class)
public abstract class ShulkerTeleportMixin {
    @Inject(method="teleportSomewhere",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/monster/Shulker;unRide()V"),cancellable=true)
    private void securitycraft$shulker(CallbackInfoReturnable<Boolean> cir,@Local(name="target") BlockPos target) {
        if(GameplayEventBus.EVENT_BUS.post(new EntityTeleportEvent.EnderEntity((Shulker)(Object)this,target.getX()+0.5,target.getY(),target.getZ()+0.5)).isCanceled()) cir.setReturnValue(false);
    }
}
