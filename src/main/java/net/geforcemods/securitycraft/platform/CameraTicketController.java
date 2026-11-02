package net.geforcemods.securitycraft.platform;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.geforcemods.securitycraft.entity.camera.SecurityCamera;
import net.geforcemods.securitycraft.misc.BlockEntityTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.TicketStorage;

/** Owner-counted, nonpersistent tickets for mounted cameras and live frame feeds. */
public final class CameraTicketController {
    private static final TicketType CAMERA = Registry.register(BuiltInRegistries.TICKET_TYPE,
        Identifier.fromNamespaceAndPath("securitycraft", "camera_forced"),
        new TicketType(0, TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION | TicketType.FLAG_KEEP_DIMENSION_ACTIVE));
    private final Map<ServerLevel,Map<Object,Set<Long>>> owners = new HashMap<>();
    private final Map<ServerLevel,Map<Long,Integer>> references = new HashMap<>();

    public CameraTicketController() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var level : new HashSet<>(owners.keySet())) {
                var invalid = new HashSet<Object>();
                for (var owner : owners.get(level).keySet()) {
                    if (owner instanceof UUID id && !(level.getEntity(id) instanceof SecurityCamera)) invalid.add(owner);
                    else if (owner == FrameOwner.INSTANCE && BlockEntityTracker.FRAME_VIEWED_SECURITY_CAMERAS.getTrackedBlockEntities(level).isEmpty()) invalid.add(owner);
                }
                for (var owner : invalid) removeOwner(level, owner);
            }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> { owners.clear(); references.clear(); });
    }

    public boolean forceChunk(ServerLevel level, BlockPos owner, int x, int z, boolean add) {
        // Frame feeds already deduplicate overlapping chunks across all viewed source blocks.
        return force(level, FrameOwner.INSTANCE, x, z, add);
    }

    public boolean forceChunk(ServerLevel level, Entity owner, int x, int z, boolean add) {
        return force(level, owner.getUUID(), x, z, add);
    }

    private boolean force(ServerLevel level, Object owner, int x, int z, boolean add) {
        long chunk = ChunkPos.pack(x, z);
        var levelOwners = owners.computeIfAbsent(level, key -> new HashMap<>());
        var chunks = levelOwners.computeIfAbsent(owner, key -> new HashSet<>());
        if (add) {
            if (!chunks.add(chunk)) return false;
            var refs = references.computeIfAbsent(level, key -> new HashMap<>());
            if (refs.merge(chunk, 1, Integer::sum) == 1) {
                // NeoForge 26.2 uses FORCED_TICKET_LEVEL for these calls even when its last
                // (forceNaturalSpawning) argument is false. Frames need simulation, not only FULL.
                level.getChunkSource().addTicket(new Ticket(CAMERA, ChunkMap.FORCED_TICKET_LEVEL), ChunkPos.unpack(chunk));
            }
        } else {
            if (!chunks.remove(chunk)) return false;
            release(level, chunk);
            if (chunks.isEmpty()) levelOwners.remove(owner);
        }
        if (levelOwners.isEmpty()) owners.remove(level);
        return true;
    }

    private void release(ServerLevel level, long chunk) {
        var refs = references.get(level);
        if (refs == null) return;
        int remaining = refs.getOrDefault(chunk, 1) - 1;
        if (remaining > 0) refs.put(chunk, remaining);
        else {
            refs.remove(chunk);
            level.getDataStorage().computeIfAbsent(TicketStorage.TYPE).removeTicket(chunk, new Ticket(CAMERA, ChunkMap.FORCED_TICKET_LEVEL));
        }
        if (refs.isEmpty()) references.remove(level);
    }

    private void removeOwner(ServerLevel level, Object owner) {
        var levelOwners = owners.get(level);
        var chunks = levelOwners.remove(owner);
        if (chunks != null) for (long chunk : chunks) release(level, chunk);
        if (levelOwners.isEmpty()) owners.remove(level);
    }

    private enum FrameOwner { INSTANCE }
}
