package net.geforcemods.securitycraft.platform.transfer;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
public class DelegatingResourceHandler<T extends Resource> implements ResourceHandler<T> {
 protected final ResourceHandler<T> delegate;
 public DelegatingResourceHandler(ResourceHandler<T> delegate){this.delegate=java.util.Objects.requireNonNull(delegate);}
 public int size(){return delegate.size();}
 public T getResource(int index){return delegate.getResource(index);}
 public int getAmount(int index){return delegate.getAmount(index);}
 public int getCapacity(int index,T resource){return delegate.getCapacity(index,resource);}
 public boolean isValid(int index,T resource){return delegate.isValid(index,resource);}
 public int insert(int index,T resource,int amount,TransactionContext tx){return delegate.insert(index,resource,amount,tx);}
 public int extract(int index,T resource,int amount,TransactionContext tx){return delegate.extract(index,resource,amount,tx);}
}
