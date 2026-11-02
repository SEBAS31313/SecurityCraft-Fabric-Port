package net.geforcemods.securitycraft.platform;

import net.minecraft.server.level.ServerPlayer;

/** Menus whose entire opening context is the held item and the player's synchronized inventory. */
public interface EmptyMenuProvider extends net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider<byte[]> {
    @Override default byte[] getScreenOpeningData(ServerPlayer player) { return new byte[0]; }
}
