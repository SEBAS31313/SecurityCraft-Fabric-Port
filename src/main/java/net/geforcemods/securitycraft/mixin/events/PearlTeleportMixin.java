package net.geforcemods.securitycraft.mixin.events;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.geforcemods.securitycraft.platform.GameplayEventBus;
import net.geforcemods.securitycraft.platform.events.EntityTeleportEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrownEnderpearl.class)
public abstract class PearlTeleportMixin {
    @WrapOperation(method="onHit",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerPlayer;teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;"))
    private ServerPlayer securitycraft$pearlPlayer(ServerPlayer player,TeleportTransition transition,Operation<ServerPlayer> original) {
        var target=transition.position();
        if(GameplayEventBus.EVENT_BUS.post(new EntityTeleportEvent.EnderPearl(player,target.x,target.y,target.z)).isCanceled()) return null;
        return original.call(player,transition);
    }
    @WrapOperation(method="onHit",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;"))
    private Entity securitycraft$pearlEntity(Entity entity,TeleportTransition transition,Operation<Entity> original) {
        var target=transition.position();
        if(GameplayEventBus.EVENT_BUS.post(new EntityTeleportEvent.EnderPearl(entity,target.x,target.y,target.z)).isCanceled()) return null;
        return original.call(entity,transition);
    }
}
