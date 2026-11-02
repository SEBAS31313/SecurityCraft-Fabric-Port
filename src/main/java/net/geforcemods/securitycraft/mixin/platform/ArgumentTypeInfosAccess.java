package net.geforcemods.securitycraft.mixin.platform;

import java.util.Map;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ArgumentTypeInfos.class)
public interface ArgumentTypeInfosAccess {
    @Accessor("BY_CLASS") static Map<Class<?>,ArgumentTypeInfo<?,?>> securitycraft$byClass() {
        throw new AssertionError("Mixin accessor was not applied");
    }
}
