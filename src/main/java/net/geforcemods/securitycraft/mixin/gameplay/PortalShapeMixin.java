package net.geforcemods.securitycraft.mixin.gameplay;

import net.geforcemods.securitycraft.platform.GameplayBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Reinforced obsidian remains a valid portal frame through the vanilla frame predicate. */
@Mixin(PortalShape.class)
public class PortalShapeMixin {
    @Inject(method = "lambda$static$0", at = @At("RETURN"), cancellable = true)
    private static void securitycraft$reinforcedPortal(BlockState state, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && state.getBlock() instanceof GameplayBlock hooks && hooks.isPortalFrame(state, level, pos))
            cir.setReturnValue(true);
    }
}
