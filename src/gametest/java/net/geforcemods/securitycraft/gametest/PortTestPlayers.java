package net.geforcemods.securitycraft.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.level.GameType;

/** Native connected test players, following GameTestHelper's EmbeddedChannel player fixture. */
public final class PortTestPlayers {
    private static final AtomicInteger NEXT=new AtomicInteger();
    private static final Map<ServerPlayer,EmbeddedChannel> CHANNELS=new IdentityHashMap<>();
    private PortTestPlayers() {}
    public static ServerPlayer create(GameTestHelper helper,String prefix) {
        String name=prefix+NEXT.incrementAndGet();
        var cookie=CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(),name),false);
        ServerPlayer player=new ServerPlayer(helper.getLevel().getServer(),helper.getLevel(),cookie.gameProfile(),cookie.clientInformation()) {
            @Override public GameType gameMode(){return GameType.SURVIVAL;}
            @Override public boolean isClientAuthoritative(){return false;}
        };
        GameType.SURVIVAL.updatePlayerAbilities(player.getAbilities());
        Connection connection=new Connection(PacketFlow.SERVERBOUND);
        EmbeddedChannel channel=new EmbeddedChannel(connection);
        CHANNELS.put(player,channel);
        helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection,player,cookie);
        player.setGameMode(GameType.SURVIVAL);
        return player;
    }
    public static void remove(ServerPlayer...players) {
        for(ServerPlayer player:players) {
            if(player==null)continue;
            player.level().getServer().getPlayerList().remove(player);
            EmbeddedChannel channel=CHANNELS.remove(player);
            if(channel!=null)channel.finishAndReleaseAll();
        }
    }
}
