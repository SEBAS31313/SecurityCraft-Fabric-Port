package net.geforcemods.securitycraft.mixin.camera;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.geforcemods.securitycraft.entity.camera.SecurityCamera;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Measure broadcast distance from a mounted camera while retaining every recipient in vanilla's loop. */
@Mixin(value=PlayerList.class,priority=1100)
public abstract class PlayerListMixin {
    @WrapOperation(method="broadcast",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerPlayer;getX()D"))
    private double securitycraft$broadcastX(ServerPlayer player,Operation<Double> original) {
        return player.getCamera() instanceof SecurityCamera camera ? camera.getX() : original.call(player);
    }
    @WrapOperation(method="broadcast",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerPlayer;getY()D"))
    private double securitycraft$broadcastY(ServerPlayer player,Operation<Double> original) {
        return player.getCamera() instanceof SecurityCamera camera ? camera.getY() : original.call(player);
    }
    @WrapOperation(method="broadcast",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerPlayer;getZ()D"))
    private double securitycraft$broadcastZ(ServerPlayer player,Operation<Double> original) {
        return player.getCamera() instanceof SecurityCamera camera ? camera.getZ() : original.call(player);
    }
}
