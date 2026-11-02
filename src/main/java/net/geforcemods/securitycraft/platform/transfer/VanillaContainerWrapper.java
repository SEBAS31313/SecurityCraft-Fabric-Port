package net.geforcemods.securitycraft.platform.transfer;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.minecraft.world.Container;
public class VanillaContainerWrapper extends FabricResourceHandler {
 protected final Container container;
 protected VanillaContainerWrapper(Container container){super(ContainerStorage.of(container,null));this.container=container;}
 public static VanillaContainerWrapper of(Container container){return new VanillaContainerWrapper(container);}
 @Override public boolean isValid(int index,ItemResource resource){return !resource.isEmpty()&&container.canPlaceItem(index,resource.toStack());}
}
