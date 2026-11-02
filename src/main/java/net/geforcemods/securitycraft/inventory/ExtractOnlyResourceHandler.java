package net.geforcemods.securitycraft.inventory;

import net.geforcemods.securitycraft.platform.transfer.DelegatingResourceHandler;
import net.geforcemods.securitycraft.platform.transfer.ResourceHandler;
import net.geforcemods.securitycraft.platform.transfer.Resource;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class ExtractOnlyResourceHandler<T extends Resource> extends DelegatingResourceHandler<T> {
	public ExtractOnlyResourceHandler(ResourceHandler<T> delegate) {
		super(delegate);
	}

	@Override
	public int insert(T resource, int amount, TransactionContext transaction) {
		return 0;
	}

	@Override
	public int insert(int index, T resource, int amount, TransactionContext transaction) {
		return 0;
	}

	@Override
	public boolean isValid(int index, T resource) {
		return false;
	}
}
