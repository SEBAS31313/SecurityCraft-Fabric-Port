package net.geforcemods.securitycraft.mixin.reinforced;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.geforcemods.securitycraft.api.IOwnable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
/** Owner-aware Fabric providers must take precedence over vanilla's Container shortcut. */
@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {
 @ModifyReturnValue(method="getBlockContainer",at=@At("RETURN"))
 private static Container securitycraft$skipBlockContainer(Container original,Level level,BlockPos pos,BlockState state){return level.getBlockEntity(pos) instanceof IOwnable?null:original;}
 @ModifyReturnValue(method="getEntityContainer",at=@At("RETURN"))
 private static Container securitycraft$skipEntityContainer(Container original){return original instanceof IOwnable?null:original;}
}
