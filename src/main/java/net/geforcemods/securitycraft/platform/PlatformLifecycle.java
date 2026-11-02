package net.geforcemods.securitycraft.platform;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

/** Session-scoped server reference for helpers shared with the integrated server. */
public final class PlatformLifecycle {
    private static volatile MinecraftServer server;
    private static boolean registered;
    private PlatformLifecycle() {}
    public static synchronized void register() {
        if (registered) return;
        registered = true;
        ServerLifecycleEvents.SERVER_STARTING.register(value -> server = value);
        ServerLifecycleEvents.SERVER_STOPPED.register(value -> { if (server == value) server = null; });
    }
    public static MinecraftServer getServer() { return server; }
}
