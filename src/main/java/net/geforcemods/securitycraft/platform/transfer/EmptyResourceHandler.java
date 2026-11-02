package net.geforcemods.securitycraft.platform.transfer;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
public final class EmptyResourceHandler<T extends Resource> implements ResourceHandler<T> {
 private static final EmptyResourceHandler<?> INSTANCE=new EmptyResourceHandler<>();
 @SuppressWarnings("unchecked") public static <T extends Resource> EmptyResourceHandler<T> instance(){return (EmptyResourceHandler<T>)INSTANCE;}
 public int size(){return 0;}
 public T getResource(int index){throw new IndexOutOfBoundsException(index);}
 public int getAmount(int index){throw new IndexOutOfBoundsException(index);}
 public int getCapacity(int index,T resource){throw new IndexOutOfBoundsException(index);}
 public boolean isValid(int index,T resource){return false;}
 public int insert(int index,T resource,int amount,TransactionContext tx){return 0;}
 public int extract(int index,T resource,int amount,TransactionContext tx){return 0;}
}
