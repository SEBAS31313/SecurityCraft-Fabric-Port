package net.geforcemods.securitycraft.entity.sentry;

import net.geforcemods.securitycraft.platform.transfer.ResourceHandler;
import net.geforcemods.securitycraft.platform.transfer.ItemResource;

public interface ISentryBulletContainer {
	public ResourceHandler<ItemResource> getHandlerForSentry(Sentry sentry);
}
