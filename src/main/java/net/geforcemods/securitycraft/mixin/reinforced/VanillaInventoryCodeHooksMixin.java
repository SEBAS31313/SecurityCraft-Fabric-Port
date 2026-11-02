package net.geforcemods.securitycraft.mixin.reinforced;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.geforcemods.securitycraft.platform.transfer.ContainerOrHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.llamalad7.mixinextras.sugar.Local;
/** Extends Fabric's block-storage hopper bridge to protected container entities such as security boats. */
@Mixin(HopperBlockEntity.class)
public class VanillaInventoryCodeHooksMixin {
 @Inject(method="ejectItems",at=@At(value="INVOKE_ASSIGN",target="Lnet/minecraft/world/level/block/entity/HopperBlockEntity;getAttachedContainer(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/HopperBlockEntity;)Lnet/minecraft/world/Container;"),cancellable=true)
 private static void securitycraft$pushToEntity(Level level,BlockPos pos,HopperBlockEntity hopper,CallbackInfoReturnable<Boolean> cir,@Local(name="container") Container container){
  if(container!=null)return;
  Direction facing=hopper.getBlockState().getValue(HopperBlock.FACING);
  BlockPos target=pos.relative(facing);
  if(ItemStorage.SIDED.find(level,target,facing.getOpposite())!=null)return;
  ContainerOrHandler lookup=ContainerOrHandler.findEntity(level,target,facing.getOpposite());
  if(!lookup.isEmpty())cir.setReturnValue(StorageUtil.move(ContainerStorage.of(hopper,facing),lookup.itemHandler().asStorage(),item->true,1,null)==1);
 }
 @Inject(method="suckInItems(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/entity/Hopper;)Z",at=@At(value="INVOKE_ASSIGN",target="Lnet/minecraft/world/level/block/entity/HopperBlockEntity;getSourceContainer(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/entity/Hopper;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/Container;"),cancellable=true)
 private static void securitycraft$pullFromEntity(Level level,Hopper hopper,CallbackInfoReturnable<Boolean> cir,@Local(name="container") Container container){
  if(container!=null)return;
  BlockPos source=BlockPos.containing(hopper.getLevelX(),hopper.getLevelY()+1.0,hopper.getLevelZ());
  if(ItemStorage.SIDED.find(level,source,Direction.DOWN)!=null)return;
  ContainerOrHandler lookup=ContainerOrHandler.findEntity(level,source,Direction.DOWN);
  if(!lookup.isEmpty())cir.setReturnValue(StorageUtil.move(lookup.itemHandler().asStorage(),ContainerStorage.of(hopper,Direction.UP),item->true,1,null)==1);
 }
}
