package net.geforcemods.securitycraft.platform;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.world.entity.player.Player;

/** The authenticated sender and main-thread executor supplied by Fabric networking. */
public record PayloadContext(Player player, Executor executor) {
    public CompletableFuture<Void> enqueueWork(Runnable work) {
        return CompletableFuture.runAsync(work, executor);
    }
}
