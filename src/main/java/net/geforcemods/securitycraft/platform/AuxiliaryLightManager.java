package net.geforcemods.securitycraft.platform;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/** Thread-safe positional emissions: the lighting worker must never query live block entities. */
public final class AuxiliaryLightManager {
    private static final Map<BlockGetter,AuxiliaryLightManager> LEVELS = new WeakHashMap<>();
    private final WeakReference<BlockGetter> level;
    private final ConcurrentHashMap<Long,ConcurrentHashMap<Long,Integer>> chunks = new ConcurrentHashMap<>();
    private AuxiliaryLightManager(BlockGetter level) { this.level = new WeakReference<>(level); }
    public static synchronized AuxiliaryLightManager get(BlockGetter level) { return LEVELS.computeIfAbsent(level, AuxiliaryLightManager::new); }

    public int getLightAt(BlockPos pos) {
        var values = chunks.get(ChunkPos.containing(pos).pack());
        return values == null ? 0 : values.getOrDefault(pos.asLong(), 0);
    }

    public void setLightAt(BlockPos pos, int value) {
        if (value < 0 || value > 15) throw new IllegalArgumentException("Light must be between 0 and 15");
        if (value == 0) { removeLightAt(pos); return; }
        var values = chunks.computeIfAbsent(ChunkPos.containing(pos).pack(), key -> new ConcurrentHashMap<>());
        Integer previous = values.put(pos.asLong(), value);
        if (previous == null || previous != value) recheck(pos);
    }

    public void removeLightAt(BlockPos pos) {
        long chunk = ChunkPos.containing(pos).pack();
        var values = chunks.get(chunk);
        if (values == null || values.remove(pos.asLong()) == null) return;
        if (values.isEmpty()) chunks.remove(chunk, values);
        recheck(pos);
    }

    public void forEachLightSource(ChunkPos chunk, BiConsumer<BlockPos,Integer> consumer) {
        var values = chunks.get(chunk.pack());
        if (values != null) values.forEach((pos, value) -> consumer.accept(BlockPos.of(pos), value));
    }

    private void recheck(BlockPos pos) {
        if (level.get() instanceof Level world) world.getChunkSource().getLightEngine().checkBlock(pos);
    }
}
