package net.geforcemods.securitycraft.mixin.events;

import java.util.Set;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.geforcemods.securitycraft.platform.GameplayEventBus;
import net.geforcemods.securitycraft.platform.events.EntityTeleportEvent;
import net.minecraft.server.commands.SpreadPlayersCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpreadPlayersCommand.class)
public abstract class SpreadTeleportMixin {
    @WrapOperation(method="setPlayerPositions",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FFZ)Z"))
    private static boolean securitycraft$spread(Entity entity,ServerLevel level,double x,double y,double z,Set<Relative> relatives,float yaw,float pitch,boolean force,Operation<Boolean> original) {
        if(GameplayEventBus.EVENT_BUS.post(new EntityTeleportEvent.SpreadPlayersCommand(entity,x,y,z)).isCanceled()) return false;
        return original.call(entity,level,x,y,z,relatives,yaw,pitch,force);
    }
}
