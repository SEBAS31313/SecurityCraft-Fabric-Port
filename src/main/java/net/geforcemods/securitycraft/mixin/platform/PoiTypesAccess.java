package net.geforcemods.securitycraft.mixin.platform;

import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PoiTypes.class)
public interface PoiTypesAccess {
    @Invoker("registerBlockStates")
    static void securitycraft$registerBlockStates(Holder<PoiType> type, Set<BlockState> states) {
        throw new AssertionError("Mixin invoker was not applied");
    }
}
