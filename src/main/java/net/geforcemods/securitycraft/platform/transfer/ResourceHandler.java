package net.geforcemods.securitycraft.platform.transfer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
/** Slot-aware compatibility surface, backed by Fabric transactions rather than a second inventory. */
public interface ResourceHandler<T extends Resource> {
 int size();
 T getResource(int index);
 int getAmount(int index);
 int getCapacity(int index, T resource);
 boolean isValid(int index, T resource);
 int insert(int index, T resource, int amount, TransactionContext transaction);
 int extract(int index, T resource, int amount, TransactionContext transaction);
 default int insert(T resource,int amount,TransactionContext transaction) {
  if(amount<0) throw new IllegalArgumentException("Negative transfer amount");
  int moved=0;
  for(int i=0;i<size()&&moved<amount;i++) if(isValid(i,resource)) moved+=insert(i,resource,amount-moved,transaction);
  return moved;
 }
 default int extract(T resource,int amount,TransactionContext transaction) {
  if(amount<0) throw new IllegalArgumentException("Negative transfer amount");
  int moved=0;
  for(int i=0;i<size()&&moved<amount;i++) moved+=extract(i,resource,amount-moved,transaction);
  return moved;
 }
 /** All automation passes through the handler overrides, preserving insert-only/owner restrictions. */
 @SuppressWarnings("unchecked")
 default SlottedStorage<ItemVariant> asStorage() {
  ResourceHandler<T> handler=this;
  return new SlottedStorage<>() {
   public int getSlotCount(){return handler.size();}
   public SingleSlotStorage<ItemVariant> getSlot(int index){
    if(index<0||index>=getSlotCount()) throw new IndexOutOfBoundsException(index);
    return new SingleSlotStorage<>() {
     public ItemVariant getResource(){return ((ItemResource)handler.getResource(index)).variant();}
     public boolean isResourceBlank(){return getResource().isBlank();}
     public long getAmount(){return handler.getAmount(index);}
     public long getCapacity(){return handler.getCapacity(index,(T)new ItemResource(getResource()));}
     public long insert(ItemVariant resource,long amount,TransactionContext tx){T converted=(T)new ItemResource(resource);return handler.isValid(index,converted)?handler.insert(index,converted,bounded(amount),tx):0;}
     public long extract(ItemVariant resource,long amount,TransactionContext tx){return handler.extract(index,(T)new ItemResource(resource),bounded(amount),tx);}
    };
   }
   public long insert(ItemVariant resource,long amount,TransactionContext tx){return handler.insert((T)new ItemResource(resource),bounded(amount),tx);}
   public long extract(ItemVariant resource,long amount,TransactionContext tx){return handler.extract((T)new ItemResource(resource),bounded(amount),tx);}
   public Iterator<StorageView<ItemVariant>> iterator(){List<StorageView<ItemVariant>> views=new ArrayList<>(getSlotCount());for(int i=0;i<getSlotCount();i++)views.add(getSlot(i));return views.iterator();}
  };
 }
 private static int bounded(long amount){if(amount<0)throw new IllegalArgumentException("Negative transfer amount");return (int)Math.min(Integer.MAX_VALUE,amount);}
}
