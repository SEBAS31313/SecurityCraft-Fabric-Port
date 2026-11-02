package net.geforcemods.securitycraft.platform;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
public interface ExtendedMenuProvider extends net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider<byte[]> {
 void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer);
 @Override default byte[] getScreenOpeningData(ServerPlayer player) {
  RegistryFriendlyByteBuf buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),player.registryAccess());
  try {
   writeClientSideData(null,buffer);
   byte[] data=new byte[buffer.readableBytes()];
   buffer.readBytes(data);
   if(data.length>65536)throw new IllegalStateException("SecurityCraft menu opening data exceeds 64KiB");
   return data;
  }finally{buffer.release();}
 }
}
