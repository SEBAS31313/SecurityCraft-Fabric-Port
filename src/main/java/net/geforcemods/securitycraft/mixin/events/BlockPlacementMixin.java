package net.geforcemods.securitycraft.mixin.events;

import net.geforcemods.securitycraft.platform.GameplayEventBus;
import net.geforcemods.securitycraft.platform.events.SCEvents.BlockEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockPlacementMixin {
    @Inject(method="place",at=@At("RETURN"))
    private void securitycraft$placed(BlockPlaceContext context,CallbackInfoReturnable<InteractionResult> cir) {
        if(cir.getReturnValue().consumesAction() && !context.getLevel().isClientSide()) GameplayEventBus.EVENT_BUS.post(new BlockEvent.EntityPlaceEvent(context.getPlayer(),context.getLevel(),context.getClickedPos(),context.getLevel().getBlockState(context.getClickedPos())));
    }
}
