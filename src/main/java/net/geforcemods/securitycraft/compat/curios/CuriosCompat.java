package net.geforcemods.securitycraft.compat.curios;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.geforcemods.securitycraft.util.InventoryUtils.ItemAccess;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Optional Curios inventory bridge, without linking its NeoForge-only superinterfaces on Fabric. */
public final class CuriosCompat {
    private CuriosCompat() {}
    public static boolean hasCuriosInventory(Player player) {
        return inventory(player)!=null;
    }
    public static ItemAccess[] getCuriosItemAccess(Player player) {
        Object inventory=inventory(player);
        if(inventory==null) return new ItemAccess[0];
        Object result=invoke(Api.GET_CURIOS,inventory);
        if(!(result instanceof Map<?,?> curios)) throw new IllegalStateException("Curios getCurios() returned an unsupported value");
        List<ItemAccess> accesses=new ArrayList<>();
        for(Object handler:curios.values()) {
            addHandler(accesses,invoke(Api.GET_STACKS,handler));
            addHandler(accesses,invoke(Api.GET_COSMETIC_STACKS,handler));
        }
        return accesses.toArray(ItemAccess[]::new);
    }
    private static Object inventory(Player player) {
        Object result=invoke(Api.GET_INVENTORY,null,player);
        if(result instanceof Optional<?> inventory) return inventory.orElse(null);
        throw new IllegalStateException("Curios getCuriosInventory() must return Optional");
    }
    private static void addHandler(List<ItemAccess> accesses,Object handler) {
        if(handler==null) return;
        accesses.add(new ItemAccess() {
            @Override public int size(){return ((Number)invoke(Api.GET_SLOTS,handler)).intValue();}
            @Override public ItemStack getItem(int slot){return (ItemStack)invoke(Api.GET_STACK,handler,slot);}
            @Override public void set(int slot,ItemStack stack){invoke(Api.SET_STACK,handler,slot,stack);}
        });
    }
    private static Object invoke(Method method,Object target,Object...args) {
        try {return method.invoke(target,args);}
        catch(InvocationTargetException exception) {
            Throwable cause=exception.getCause();
            if(cause instanceof RuntimeException runtime) throw runtime;
            if(cause instanceof Error error) throw error;
            throw new IllegalStateException("Curios inventory operation failed: "+method.getName(),cause);
        }
        catch(ReflectiveOperationException exception){throw new IllegalStateException("Could not call Curios inventory API: "+method.getName(),exception);}
    }
    private static final class Api {
        static final Method GET_INVENTORY;
        static final Method GET_CURIOS;
        static final Method GET_STACKS;
        static final Method GET_COSMETIC_STACKS;
        static final Method GET_SLOTS;
        static final Method GET_STACK;
        static final Method SET_STACK;
        static {
            try {
                ClassLoader loader=CuriosCompat.class.getClassLoader();
                Class<?> api=Class.forName("top.theillusivec4.curios.api.CuriosApi",true,loader);
                Class<?> inventory=Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler",true,loader);
                Class<?> slot=Class.forName("top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler",true,loader);
                Class<?> stacks=Class.forName("top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler",true,loader);
                GET_INVENTORY=api.getMethod("getCuriosInventory",LivingEntity.class);
                GET_CURIOS=inventory.getMethod("getCurios");
                GET_STACKS=slot.getMethod("getStacks");
                GET_COSMETIC_STACKS=slot.getMethod("getCosmeticStacks");
                GET_SLOTS=stacks.getMethod("getSlots");
                GET_STACK=stacks.getMethod("getStackInSlot",int.class);
                SET_STACK=stacks.getMethod("setStackInSlot",int.class,ItemStack.class);
            }
            catch(ReflectiveOperationException|LinkageError exception) {
                throw new IllegalStateException("The installed Curios bridge does not provide SecurityCraft's supported inventory API",exception);
            }
        }
    }
}
