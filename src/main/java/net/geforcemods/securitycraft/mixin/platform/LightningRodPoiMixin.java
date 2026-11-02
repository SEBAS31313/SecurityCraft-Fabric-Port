package net.geforcemods.securitycraft.mixin.platform;

import net.geforcemods.securitycraft.SCContent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PoiType.class)
public class LightningRodPoiMixin {
    @Inject(method = "is", at = @At("HEAD"), cancellable = true)
    private void securitycraft$includeReinforcedRods(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this == BuiltInRegistries.POINT_OF_INTEREST_TYPE.getValue(PoiTypes.LIGHTNING_ROD)
                && state.getBlock() instanceof net.geforcemods.securitycraft.blocks.reinforced.ReinforcedLightningRodBlock)
            cir.setReturnValue(true);
    }
}
