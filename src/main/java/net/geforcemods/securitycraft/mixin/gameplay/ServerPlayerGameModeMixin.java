package net.geforcemods.securitycraft.mixin.gameplay;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.geforcemods.securitycraft.platform.GameplayBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
 @Shadow @Final protected ServerPlayer player;
 @WrapOperation(method="destroyBlock",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerLevel;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
 private boolean securitycraft$mineAndCageBreak(ServerLevel level,BlockPos pos,boolean moving,Operation<Boolean> original){
  var state=level.getBlockState(pos);
  if(state.getBlock() instanceof GameplayBlock hooks)return hooks.onDestroyedByPlayer(state,level,pos,player,player.getMainHandItem(),player.hasCorrectToolForDrops(state),level.getFluidState(pos));
  return original.call(level,pos,moving);
 }
}
