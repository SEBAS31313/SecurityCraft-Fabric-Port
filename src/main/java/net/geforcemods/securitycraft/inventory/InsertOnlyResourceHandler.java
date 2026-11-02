package net.geforcemods.securitycraft.inventory;

import net.geforcemods.securitycraft.platform.transfer.DelegatingResourceHandler;
import net.geforcemods.securitycraft.platform.transfer.ResourceHandler;
import net.geforcemods.securitycraft.platform.transfer.Resource;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class InsertOnlyResourceHandler<T extends Resource> extends DelegatingResourceHandler<T> {
	public InsertOnlyResourceHandler(ResourceHandler<T> delegate) {
		super(delegate);
	}

	@Override
	public int extract(T resource, int amount, TransactionContext transaction) {
		return 0;
	}

	@Override
	public int extract(int index, T resource, int amount, TransactionContext transaction) {
		return 0;
	}
}
