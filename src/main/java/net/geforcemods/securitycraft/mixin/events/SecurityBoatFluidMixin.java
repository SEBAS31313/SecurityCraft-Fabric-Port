package net.geforcemods.securitycraft.mixin.events;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.geforcemods.securitycraft.entity.AbstractSecuritySeaBoat;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractBoat.class)
public abstract class SecurityBoatFluidMixin {
    @WrapOperation(method={"checkInWater","isUnderwater","getWaterLevelAbove"},at=@At(value="INVOKE",target="Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean securitycraft$lava(FluidState state,TagKey<Fluid> tag,Operation<Boolean> original) {
        return original.call(state,tag) || ((Object)this instanceof AbstractSecuritySeaBoat && tag.equals(FluidTags.WATER) && state.is(FluidTags.LAVA));
    }
}
