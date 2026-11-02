package net.geforcemods.securitycraft.mixin.gameplay;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.geforcemods.securitycraft.api.IReinforcedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(ConduitBlockEntity.class)
public class ConduitBlockEntityMixin {
 @WrapOperation(method="updateShape",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"))
 private static boolean securitycraft$reinforcedFrame(BlockState state,Object block,Operation<Boolean> original){return original.call(state,block)||state.getBlock() instanceof IReinforcedBlock reinforced&&reinforced.getVanillaBlock()==block;}
}
