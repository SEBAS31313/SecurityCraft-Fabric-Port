package net.geforcemods.securitycraft.platform.transfer;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
public record ContainerOrHandler(Container container, ResourceHandler<ItemResource> itemHandler) {
 public static final ContainerOrHandler EMPTY=new ContainerOrHandler(null,null);
 public boolean isEmpty(){return container==null&&itemHandler==null;}
 public static ContainerOrHandler find(Level level,BlockPos pos,Direction direction){
  // Always use the Fabric lookup first so owner-aware providers cannot be bypassed through Container.
  var storage=ItemStorage.SIDED.find(level,pos,direction);
  return storage==null?findEntity(level,pos,direction):new ContainerOrHandler(null,FabricResourceHandler.of(storage));
 }
 public static ContainerOrHandler findEntity(Level level,BlockPos pos,Direction direction){
  var entities=level.getEntities((net.minecraft.world.entity.Entity)null,new net.minecraft.world.phys.AABB(pos),net.minecraft.world.entity.EntitySelector.CONTAINER_ENTITY_SELECTOR);
  if(entities.isEmpty())return EMPTY;
  var entity=entities.get(level.getRandom().nextInt(entities.size()));
  var storage=net.geforcemods.securitycraft.platform.ItemStorageRegistration.ENTITY.find(entity,direction);
  if(storage!=null)return new ContainerOrHandler(null,FabricResourceHandler.of(storage));
  // Never fall through to the raw inventory of an owned entity.
  if(entity instanceof net.geforcemods.securitycraft.api.IOwnable)return EMPTY;
  return entity instanceof Container container?new ContainerOrHandler(null,VanillaContainerWrapper.of(container)):EMPTY;
 }
}
