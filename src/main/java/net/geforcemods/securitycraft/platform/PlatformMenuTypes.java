package net.geforcemods.securitycraft.platform;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

/** Transports the original menu opening buffer through Fabric's typed menu payload. */
public final class PlatformMenuTypes {
    private PlatformMenuTypes() {}
    @FunctionalInterface public interface Factory<T extends AbstractContainerMenu> {
        T create(int id, Inventory inventory, RegistryFriendlyByteBuf data);
    }
    public static <T extends AbstractContainerMenu> MenuType<T> create(Factory<T> factory) {
        return new ExtendedMenuType<>((id, inventory, data) -> {
            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data), inventory.player.registryAccess());
            try { return factory.create(id, inventory, buf); }
            finally { buf.release(); }
        }, ByteBufCodecs.byteArray(65536));
    }
}
