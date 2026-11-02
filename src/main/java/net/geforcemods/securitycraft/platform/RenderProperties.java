package net.geforcemods.securitycraft.platform;

import net.minecraft.world.level.block.state.BlockState;

/** Common render-data keys; reading a snapshot never loads client rendering classes on a server. */
public final class RenderProperties {
	public static final ModelProperty<BlockState> DISGUISED_STATE = new ModelProperty<>();
	public static final ModelProperty<Boolean> POWERED = new ModelProperty<>();
	private RenderProperties() {}
}
