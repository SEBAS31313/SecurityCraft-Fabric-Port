package net.geforcemods.securitycraft.platform.transfer;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
/** Adapts native Fabric storage, including third-party unslotted storage, without copying its items. */
public class FabricResourceHandler implements ResourceHandler<ItemResource> {
 protected final Storage<ItemVariant> storage;
 public FabricResourceHandler(Storage<ItemVariant> storage){this.storage=java.util.Objects.requireNonNull(storage);}
 public static ResourceHandler<ItemResource> of(Storage<ItemVariant> storage){return storage==null?null:new FabricResourceHandler(storage);}
 protected List<StorageView<ItemVariant>> views(){List<StorageView<ItemVariant>> result=new ArrayList<>();storage.forEach(result::add);return result;}
 protected StorageView<ItemVariant> view(int index){return storage instanceof SlottedStorage<ItemVariant> slotted?slotted.getSlot(index):views().get(index);}
 public int size(){return storage instanceof SlottedStorage<ItemVariant> slotted?slotted.getSlotCount():views().size();}
 public ItemResource getResource(int index){return new ItemResource(view(index).getResource());}
 public int getAmount(int index){return (int)Math.min(Integer.MAX_VALUE,view(index).getAmount());}
 public int getCapacity(int index,ItemResource resource){return (int)Math.min(Integer.MAX_VALUE,view(index).getCapacity());}
 public boolean isValid(int index,ItemResource resource){return !resource.isEmpty()&&storage.supportsInsertion();}
 public int insert(ItemResource resource,int amount,TransactionContext tx){if(amount<0)throw new IllegalArgumentException("Negative transfer amount");return amount==0||resource.isEmpty()?0:(int)storage.insert(resource.variant(),amount,tx);}
 public int extract(ItemResource resource,int amount,TransactionContext tx){if(amount<0)throw new IllegalArgumentException("Negative transfer amount");return amount==0||resource.isEmpty()?0:(int)storage.extract(resource.variant(),amount,tx);}
 public int insert(int index,ItemResource resource,int amount,TransactionContext tx){
  if(amount<0)throw new IllegalArgumentException("Negative transfer amount");
  if(amount==0||resource.isEmpty())return 0;
  // Unslotted storages expose extractable views only. Insert into their storage once, at index zero.
  return (int)(storage instanceof SlottedStorage<ItemVariant> slotted?slotted.getSlot(index).insert(resource.variant(),amount,tx):index==0?storage.insert(resource.variant(),amount,tx):0);
 }
 public int extract(int index,ItemResource resource,int amount,TransactionContext tx){if(amount<0)throw new IllegalArgumentException("Negative transfer amount");return amount==0||resource.isEmpty()?0:(int)view(index).extract(resource.variant(),amount,tx);}
}
