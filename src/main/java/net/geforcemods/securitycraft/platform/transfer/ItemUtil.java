package net.geforcemods.securitycraft.platform.transfer;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
public final class ItemUtil {
 private ItemUtil(){}
 public static ItemStack insertItemReturnRemaining(ResourceHandler<ItemResource> handler,ItemStack stack,boolean simulate,TransactionContext parent){
  if(stack.isEmpty())return ItemStack.EMPTY;
  try(Transaction tx=parent==null?Transaction.openOuter():parent.openNested()){
   int inserted=handler.insert(ItemResource.of(stack),stack.getCount(),tx);
   if(!simulate)tx.commit();
   return stack.copyWithCount(stack.getCount()-inserted);
  }
 }
}
