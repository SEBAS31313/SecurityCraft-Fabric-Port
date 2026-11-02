package net.geforcemods.securitycraft.renderers.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;

public class KeypadChestRenderState extends ChestRenderState {
	public BlockEntityRenderState disguiseRenderState;
	public boolean isDisguised;
	public net.minecraft.client.resources.model.sprite.SpriteId customSprite;
}
