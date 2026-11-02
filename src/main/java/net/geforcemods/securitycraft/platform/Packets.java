package net.geforcemods.securitycraft.platform;

import java.util.Objects;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

/** Fabric networking destinations. The client transport is installed only by the client initializer. */
public final class Packets {
    private static Consumer<CustomPacketPayload> clientSender;
    private Packets() {}
    public static void installClientSender(Consumer<CustomPacketPayload> sender) {
        clientSender = Objects.requireNonNull(sender);
    }
    public static void sendToServer(CustomPacketPayload payload) {
        if (clientSender == null) throw new IllegalStateException("Client networking is not initialized");
        clientSender.accept(payload);
    }
    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }
    public static void sendToAllPlayers(CustomPacketPayload payload) {
        var server = PlatformLifecycle.getServer();
        if (server != null) for (var player : PlayerLookup.all(server)) sendToPlayer(player, payload);
    }
    public static void sendToPlayersInDimension(ServerLevel level, CustomPacketPayload payload) {
        for (var player : level.players()) sendToPlayer(player, payload);
    }
    public static void sendToPlayersTrackingChunk(ServerLevel level, ChunkPos chunk, CustomPacketPayload payload) {
        for (var player : PlayerLookup.tracking(level, chunk)) sendToPlayer(player, payload);
    }
    public static void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload) {
        for (var player : PlayerLookup.tracking(entity)) sendToPlayer(player, payload);
    }
    public static void sendToPlayersTrackingEntityAndSelf(Entity entity, CustomPacketPayload payload) {
        sendToPlayersTrackingEntity(entity, payload);
        if (entity instanceof ServerPlayer player) sendToPlayer(player, payload);
    }
}
