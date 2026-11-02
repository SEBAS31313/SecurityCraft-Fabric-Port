package net.geforcemods.securitycraft.platform;

import com.mojang.brigadier.arguments.ArgumentType;
import net.geforcemods.securitycraft.mixin.platform.ArgumentTypeInfosAccess;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;

/** Associates custom argument classes with their registered wire serializers. */
public final class PlatformArguments {
    private PlatformArguments() {}
    public static <A extends ArgumentType<?>, I extends ArgumentTypeInfo<A,?>> I mapByClass(Class<? extends A> type, I info) {
        ArgumentTypeInfo<?,?> previous = ArgumentTypeInfosAccess.securitycraft$byClass().putIfAbsent(type, info);
        if (previous != null) throw new IllegalStateException("Argument class already registered: " + type.getName());
        return info;
    }
}
