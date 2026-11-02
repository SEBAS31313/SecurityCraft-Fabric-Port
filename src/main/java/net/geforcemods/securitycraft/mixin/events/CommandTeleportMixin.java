package net.geforcemods.securitycraft.mixin.events;

import java.util.Set;
import net.geforcemods.securitycraft.platform.GameplayEventBus;
import net.geforcemods.securitycraft.platform.events.EntityTeleportEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.LookAt;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TeleportCommand.class)
public abstract class CommandTeleportMixin {
    @Inject(method="performTeleport",at=@At("HEAD"),cancellable=true)
    private static void securitycraft$teleportCommand(CommandSourceStack source,Entity entity,ServerLevel level,double x,double y,double z,Set<Relative> relatives,float yaw,float pitch,LookAt lookAt,CallbackInfo ci) {
        if(GameplayEventBus.EVENT_BUS.post(new EntityTeleportEvent.TeleportCommand(entity,x,y,z)).isCanceled()) ci.cancel();
    }
}
