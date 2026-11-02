package net.geforcemods.securitycraft.models;

import java.util.List;
import java.util.function.Predicate;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.geforcemods.securitycraft.platform.ModelData;
import net.geforcemods.securitycraft.platform.ModelDataAccess;
import net.geforcemods.securitycraft.platform.ModelProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material.Baked;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/** Dynamic disguise geometry, using Fabric's render-data snapshots during chunk meshing. */
public class DisguisableBlockStateModel implements BlockStateModel {
	public static final ModelProperty<BlockState> DISGUISED_STATE = net.geforcemods.securitycraft.platform.RenderProperties.DISGUISED_STATE;
	protected final BlockStateModel oldModel;

	public DisguisableBlockStateModel(BlockStateModel oldModel) {
		this.oldModel = oldModel;
	}

	protected BlockStateModel undisguisedModel(ModelData data) {
		return oldModel;
	}

	private BlockState disguise(ModelData data) {
		BlockState disguise = data.get(DISGUISED_STATE);
		return disguise == null || disguise.isAir() ? null : disguise;
	}

	protected BlockStateModel modelFor(ModelData data) {
		BlockState disguise = disguise(data);
		if (disguise != null) {
			BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(disguise);
			// Never delegate through another dynamic disguise wrapper: a disguised SC block may otherwise recurse.
			if (model != this && !(model instanceof DisguisableBlockStateModel))
				return model;
		}
		return undisguisedModel(data);
	}

	@Override
	public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<Direction> cullTest) {
		ModelData data = ModelDataAccess.get(level, pos);
		BlockStateModel model = modelFor(data);
		BlockState disguisedState = disguise(data);
		boolean disguising = disguisedState != null && model != undisguisedModel(data);
		if (disguising) {
			var tintSources = Minecraft.getInstance().getBlockColors().getTintSources(disguisedState);
			emitter.pushTransform(quad -> {
				int tint = quad.tintIndex();
				if (tint >= 0) {
					if (tint < tintSources.size())
						quad.multiplyColor(tintSources.get(tint).colorInWorld(disguisedState, level, pos));
					quad.tintIndex(-1);
				}
				return true;
			});
		}
		try {
			model.emitQuads(emitter, level, pos, disguising ? disguisedState : state, random, cullTest);
		} finally {
			if (disguising)
				emitter.popTransform();
		}
	}

	@Override
	public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
		oldModel.collectParts(random, parts);
	}

	@Override
	public Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		ModelData data = ModelDataAccess.get(level, pos);
		BlockState disguise = disguise(data);
		BlockStateModel model = modelFor(data);
		return model.particleMaterial(level, pos, disguise != null && model != undisguisedModel(data) ? disguise : state);
	}

	@Override
	public Baked particleMaterial() {
		return oldModel.particleMaterial();
	}

	@Override
	public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
		ModelData data = ModelDataAccess.get(level, pos);
		BlockState disguise = disguise(data);
		BlockStateModel model = modelFor(data);
		return model.materialFlags(level, pos, disguise != null && model != undisguisedModel(data) ? disguise : state, random);
	}

	@Override
	public int materialFlags() {
		// A disguise can use translucent and animated materials even when the underlying block does not.
		return -1;
	}
}
