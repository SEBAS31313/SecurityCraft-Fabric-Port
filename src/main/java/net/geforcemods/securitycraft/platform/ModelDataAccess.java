package net.geforcemods.securitycraft.platform;

import net.fabricmc.fabric.api.blockgetter.v2.FabricBlockGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

/** Reads the snapshot of a chunk meshing context rather than a live mutable block entity. */
public final class ModelDataAccess {
	private ModelDataAccess() {}

	public static ModelData get(BlockGetter level, BlockPos pos) {
		Object data = ((FabricBlockGetter) level).getBlockEntityRenderData(pos);
		return data instanceof ModelData modelData ? modelData : ModelData.EMPTY;
	}
}
