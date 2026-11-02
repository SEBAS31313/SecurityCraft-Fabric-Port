package net.geforcemods.securitycraft.recipe;

import java.util.Map;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.geforcemods.securitycraft.platform.CraftingContext;
import net.geforcemods.securitycraft.platform.PlatformLifecycle;
import net.minecraft.server.level.ServerPlayer;

public abstract class AbstractReinforcerRecipe extends CustomRecipe {
	private final Ingredient reinforcer;

	protected AbstractReinforcerRecipe(Ingredient reinforcer) {
		this.reinforcer = reinforcer;
	}

	public abstract Map<Block, Block> getBlockMap();

	public abstract boolean isCorrectReinforcer(ItemStack reinforcer);

	@Override
	public boolean matches(CraftingInput inv, Level level) {
		boolean hasCorrectBlock = false;
		boolean hasReinforcer = false;
		Map<Block, Block> blockMap = getBlockMap();

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			Item item = stack.getItem();

			if (item instanceof BlockItem blockItem) {
				if (blockMap.containsKey(blockItem.getBlock())) {
					if (hasCorrectBlock)
						return false;

					hasCorrectBlock = true;
				}
				else
					return false;
			}
			else if (reinforcer.test(stack)) {
				if (hasReinforcer || !isCorrectReinforcer(stack))
					return false;

				hasReinforcer = true;
			}
			else if (!stack.isEmpty())
				return false;
		}

		return hasReinforcer && hasCorrectBlock;
	}

	@Override
	public ItemStack assemble(CraftingInput inv) {
		for (int i = 0; i < inv.size(); i++) {
			if (inv.getItem(i).getItem() instanceof BlockItem blockItem)
				return new ItemStack(getBlockMap().get(blockItem.getBlock()));
		}

		return ItemStack.EMPTY;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
		NonNullList<ItemStack> newInv = NonNullList.withSize(inv.size(), ItemStack.EMPTY);

		for (int i = 0; i < newInv.size(); i++) {
			ItemStack stack = inv.getItem(i);

			if (reinforcer.test(stack)) {
				Player player = CraftingContext.currentPlayer();
				Level level = player != null ? player.level() : PlatformLifecycle.getServer() == null ? null : PlatformLifecycle.getServer().overworld();

				if (level != null && !level.isClientSide())
					stack.hurtAndBreak(1, (ServerLevel) level, player instanceof ServerPlayer serverPlayer ? serverPlayer : null, item -> {});

				newInv.set(i, stack.copy());
			}
		}

		return newInv;
	}

	public Ingredient reinforcer() {
		return reinforcer;
	}
}
