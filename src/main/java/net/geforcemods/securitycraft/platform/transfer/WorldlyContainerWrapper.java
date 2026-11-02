package net.geforcemods.securitycraft.platform.transfer;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
public class WorldlyContainerWrapper extends FabricResourceHandler {
 public WorldlyContainerWrapper(WorldlyContainer container,Direction direction){super(ContainerStorage.of(container,direction));}
}
