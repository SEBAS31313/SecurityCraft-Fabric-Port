package net.geforcemods.securitycraft.compat.ium;

import net.minecraft.client.multiplayer.ClientLevel;
import net.fabricmc.loader.api.FabricLoader;

public class IumCompat {
	private static final IumMod NONE = new IumMod() {
		@Override
		public void onChunkStatusAdded(ClientLevel level, int x, int z) {}

		@Override
		public void onChunkStatusRemoved(ClientLevel level, int x, int z) {}
	};
	private static IumMod activeIumMod;

	private IumCompat() {}

	public static IumMod get() {
		if (activeIumMod == null)
			activeIumMod = getInstalledIumMod();

		return activeIumMod;
	}

	public static boolean isActive() {
		return get() != NONE;
	}

	private static IumMod getInstalledIumMod() {
		if (FabricLoader.getInstance().isModLoaded("embeddium"))
			return new Embeddium();
		else if (FabricLoader.getInstance().isModLoaded("sodium"))
			return new Sodium();
		else
			return NONE;
	}

	public interface IumMod {
		int FLAG_HAS_BLOCK_DATA = 1;

		void onChunkStatusAdded(ClientLevel level, int x, int z);

		void onChunkStatusRemoved(ClientLevel level, int x, int z);
	}
}
