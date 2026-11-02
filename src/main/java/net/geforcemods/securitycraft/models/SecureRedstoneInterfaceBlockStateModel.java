package net.geforcemods.securitycraft.models;

import java.util.function.Supplier;

import net.geforcemods.securitycraft.platform.ModelData;
import net.geforcemods.securitycraft.platform.ModelProperty;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;

public class SecureRedstoneInterfaceBlockStateModel extends DisguisableBlockStateModel {
	public static final ModelProperty<Boolean> POWERED = net.geforcemods.securitycraft.platform.RenderProperties.POWERED;
	private final Supplier<BlockStateModel> poweredModel;

	public SecureRedstoneInterfaceBlockStateModel(Supplier<BlockStateModel> poweredModel, BlockStateModel oldModel) {
		super(oldModel);
		this.poweredModel = poweredModel;
	}

	@Override
	protected BlockStateModel undisguisedModel(ModelData data) {
		return Boolean.TRUE.equals(data.get(POWERED)) ? poweredModel.get() : oldModel;
	}
}
