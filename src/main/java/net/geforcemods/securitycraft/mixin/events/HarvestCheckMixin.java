package net.geforcemods.securitycraft.mixin.events;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.geforcemods.securitycraft.platform.GameplayEventBus;
import net.geforcemods.securitycraft.platform.events.SCEvents.PlayerEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayerGameMode.class)
public abstract class HarvestCheckMixin {
    @Shadow @Final protected ServerPlayer player;
    @ModifyExpressionValue(method="destroyBlock",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerPlayer;hasCorrectToolForDrops(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean securitycraft$harvest(boolean original,BlockPos pos) {
        return GameplayEventBus.EVENT_BUS.post(new PlayerEvent.HarvestCheck(player,player.level(),pos,original)).canHarvest();
    }
}
