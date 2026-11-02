package net.geforcemods.securitycraft.platform.transfer;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
public final class CombinedResourceHandler<T extends Resource> implements ResourceHandler<T> {
 private final List<ResourceHandler<T>> handlers;
 @SafeVarargs public CombinedResourceHandler(ResourceHandler<T>...handlers){this.handlers=List.of(handlers);}
 private ResourceHandler<T> handler(int index){for(ResourceHandler<T> h:handlers){if(index<h.size())return h;index-=h.size();}throw new IndexOutOfBoundsException(index);}
 private int local(int index){for(ResourceHandler<T> h:handlers){if(index<h.size())return index;index-=h.size();}throw new IndexOutOfBoundsException(index);}
 public int size(){return handlers.stream().mapToInt(ResourceHandler::size).sum();}
 public T getResource(int index){return handler(index).getResource(local(index));}
 public int getAmount(int index){return handler(index).getAmount(local(index));}
 public int getCapacity(int index,T resource){return handler(index).getCapacity(local(index),resource);}
 public boolean isValid(int index,T resource){return handler(index).isValid(local(index),resource);}
 public int insert(int index,T resource,int amount,TransactionContext tx){return handler(index).insert(local(index),resource,amount,tx);}
 public int extract(int index,T resource,int amount,TransactionContext tx){return handler(index).extract(local(index),resource,amount,tx);}
}
