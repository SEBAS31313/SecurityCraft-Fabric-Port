package net.geforcemods.securitycraft.platform;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.world.entity.player.Player;

/** Crafting-player context retained only during the result-slot operation on the current thread. */
public final class CraftingContext {
    private static final ThreadLocal<Deque<Player>> PLAYERS=ThreadLocal.withInitial(ArrayDeque::new);
    private CraftingContext() {}
    public static Player currentPlayer(){return PLAYERS.get().peek();}
    public static Scope push(Player player){PLAYERS.get().push(player);return new Scope();}
    public static final class Scope implements AutoCloseable {
        private boolean closed;
        @Override public void close(){if(closed)return;closed=true;var players=PLAYERS.get();players.pop();if(players.isEmpty())PLAYERS.remove();}
    }
}
