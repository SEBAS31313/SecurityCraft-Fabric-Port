package net.geforcemods.securitycraft.platform;
import net.minecraft.network.Connection;
import net.minecraft.world.level.storage.ValueInput;
/** Hooks dispatched by BlockEntityLifecycleMixin after vanilla establishes level/data. */
public interface BlockEntityLifecycle {
 default void onLoad() {}
 default void onDataPacket(Connection connection,ValueInput input) {}
}
