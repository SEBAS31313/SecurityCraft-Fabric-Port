package net.geforcemods.securitycraft.gametest;

import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.blockentities.KeypadBlockEntity;
import net.geforcemods.securitycraft.blockentities.SecurityCameraBlockEntity;
import net.geforcemods.securitycraft.blocks.SecurityCameraBlock;
import net.geforcemods.securitycraft.entity.camera.SecurityCamera;
import net.geforcemods.securitycraft.entity.sentry.Sentry;
import net.geforcemods.securitycraft.entity.sentry.TargetNearestPlayerOrMobGoal;
import net.geforcemods.securitycraft.misc.BlockEntityTracker;
import net.geforcemods.securitycraft.network.server.DismountCamera;
import net.geforcemods.securitycraft.network.server.MountCamera;
import net.geforcemods.securitycraft.platform.PayloadContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.TicketStorage;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Server-side behavioral checks against the actual Fabric-instrumented world and native players. */
public class CommonGameTests {
    @GameTest(maxTicks=40)
    public void ownershipAndOfflineTransfer(GameTestHelper helper) {
        ServerPlayer owner=PortTestPlayers.create(helper,"sc_owner");
        ServerPlayer intruder=PortTestPlayers.create(helper,"sc_other");
        try {
            BlockPos relative=new BlockPos(2,2,2);
            helper.setBlock(relative.south(),Blocks.STONE);
            helper.setBlock(relative,SCContent.KEYPAD.get());
            BlockPos pos=helper.absolutePos(relative);
            var state=helper.getLevel().getBlockState(pos);
            state.getBlock().setPlacedBy(helper.getLevel(),pos,state,owner,new ItemStack(SCContent.KEYPAD.get()));
            var keypad=(KeypadBlockEntity)helper.getLevel().getBlockEntity(pos);
            helper.assertTrue(keypad.isOwnedBy(owner),"The placing player must own the keypad");
            helper.assertFalse(keypad.isOwnedBy(intruder),"An unrelated player must not own the keypad");
            helper.assertTrue(state.getDestroyProgress(owner,helper.getLevel(),pos)>0,"The owner must be able to mine their keypad");
            helper.assertTrue(state.getDestroyProgress(intruder,helper.getLevel(),pos)==0,"An intruder must not be able to mine another player's keypad");
            ItemStack changer=new ItemStack(SCContent.UNIVERSAL_OWNER_CHANGER.get());
            changer.set(DataComponents.CUSTOM_NAME,Component.literal("PortOffline"));
            owner.setItemInHand(InteractionHand.MAIN_HAND,changer);
            var hit=new BlockHitResult(Vec3.atCenterOf(pos),Direction.NORTH,pos,false);
            intruder.setItemInHand(InteractionHand.MAIN_HAND,changer.copy());
            intruder.gameMode.useItemOn(intruder,helper.getLevel(),intruder.getMainHandItem(),InteractionHand.MAIN_HAND,hit);
            helper.assertTrue(keypad.isOwnedBy(owner),"An intruder must not transfer another player's block through the native interaction route");
            helper.assertTrue(owner.gameMode.useItemOn(owner,helper.getLevel(),changer,InteractionHand.MAIN_HAND,hit).consumesAction(),"Owner changer must transfer through the registered Fabric use callback");
            helper.assertTrue("PortOffline".equals(keypad.getOwner().getName()),"Transferred owner name must persist");
            helper.assertFalse(keypad.isOwnedBy(owner),"Previous owner must lose access after transfer");
            helper.assertTrue(keypad.isOwnedBy(new Owner("PortOffline",UUID.randomUUID().toString())),"An offline transfer must resolve ownership by name when the player returns");
            helper.succeed();
        } finally {PortTestPlayers.remove(owner,intruder);}
    }

    @GameTest(maxTicks=80)
    public void cameraAuthorizationMountAndTicketCleanup(GameTestHelper helper) {
        ServerPlayer owner=PortTestPlayers.create(helper,"sc_camowner");
        ServerPlayer intruder=PortTestPlayers.create(helper,"sc_camother");
        ServerLevel level=helper.getLevel();
        BlockPos relative=new BlockPos(2,2,2);
        helper.setBlock(relative.south(),Blocks.STONE);
        helper.setBlock(relative,SCContent.SECURITY_CAMERA.get());
        BlockPos pos=helper.absolutePos(relative);
        var be=(SecurityCameraBlockEntity)level.getBlockEntity(pos);
        be.setOwner(owner.getUUID().toString(),owner.getGameProfile().name());
        be.tick(level,pos,be.getBlockState());
        Vec3 ownerPosition=owner.position();
        ChunkPos chunk=ChunkPos.containing(pos);
        TicketStorage tickets=level.getDataStorage().computeIfAbsent(TicketStorage.TYPE);
        boolean previouslyForced=tickets.getTickets(chunk.pack()).stream().anyMatch(ticket->ticket.getType()==TicketType.FORCED);
        level.setChunkForced(chunk.x(),chunk.z(),true);
        try {
            new MountCamera(pos).handle(new PayloadContext(intruder,level.getServer()));
            helper.assertTrue(intruder.getCamera()==intruder,"An intruder must not mount an unshared camera");
            new MountCamera(pos).handle(new PayloadContext(owner,level.getServer()));
            helper.assertTrue(owner.getCamera() instanceof SecurityCamera,"Camera owner must mount its camera entity");
            helper.assertTrue(owner.position().equals(ownerPosition),"Mounting a remote camera must not teleport the player body");
            helper.assertTrue(level.getBlockState(pos).getValue(SecurityCameraBlock.BEING_VIEWED),"Mounted camera must signal that it is being viewed");
            helper.assertTrue(hasCameraTicket(tickets,chunk),"Mounted cameras must create their own native chunk tickets");
            new DismountCamera().handle(new PayloadContext(owner,level.getServer()));
            helper.assertTrue(owner.getCamera()==owner,"Dismount must restore the player's own camera");
            helper.assertFalse(level.getBlockState(pos).getValue(SecurityCameraBlock.BEING_VIEWED),"Dismount must clear the camera's viewed state");
            helper.assertFalse(hasCameraTicket(tickets,chunk),"Dismount must release the camera's tickets");
            helper.assertTrue(tickets.getTickets(chunk.pack()).stream().anyMatch(ticket->ticket.getType()==TicketType.FORCED),"Camera cleanup must retain a separate /forceload ticket");
            helper.succeed();
        } finally {
            if(owner.getCamera() instanceof SecurityCamera camera)camera.stopViewing(owner);
            if(!previouslyForced)level.setChunkForced(chunk.x(),chunk.z(),false);
            PortTestPlayers.remove(owner,intruder);
        }
    }

    @GameTest(maxTicks=100)
    public void frameFeedLoadsAndUnlinksChunks(GameTestHelper helper) {
        ServerPlayer owner=PortTestPlayers.create(helper,"sc_frame");
        ServerLevel level=helper.getLevel();
        BlockPos relative=new BlockPos(2,2,2);
        helper.setBlock(relative.south(),Blocks.STONE);
        helper.setBlock(relative,SCContent.SECURITY_CAMERA.get());
        BlockPos pos=helper.absolutePos(relative);
        var camera=(SecurityCameraBlockEntity)level.getBlockEntity(pos);
        camera.setOwner(owner.getUUID().toString(),owner.getGameProfile().name());
        camera.tick(level,pos,camera.getBlockState());
        BlockPos framePos=helper.absolutePos(new BlockPos(5,2,2));
        camera.linkFrameForPlayer(owner,framePos,1);
        helper.runAfterDelay(5,()-> {
            try {
                helper.assertTrue(camera.isFrameLinked(owner,framePos),"A linked frame must retain its viewer and position");
                helper.assertTrue(camera.getCameraFeedChunks(owner)!=null,"A linked frame must have a camera chunk view");
                helper.assertTrue(camera.shouldKeepChunkTracked(owner,ChunkPos.containing(pos).x(),ChunkPos.containing(pos).z()),"Camera source chunk must remain tracked for the frame");
                var tickets=level.getDataStorage().computeIfAbsent(TicketStorage.TYPE);
                helper.assertTrue(hasCameraTicket(tickets,ChunkPos.containing(pos)),"An active frame feed must load camera chunks");
                camera.unlinkFrameForPlayer(owner.getUUID(),framePos);
                camera.clearCameraFeedChunks(owner);
                helper.assertFalse(camera.hasPlayerFrameLink(owner),"Unlinking the last frame must remove its viewer");
                helper.assertFalse(BlockEntityTracker.FRAME_VIEWED_SECURITY_CAMERAS.getTrackedBlockEntities(level).contains(pos),"Unlinked camera must leave the active-feed tracker");
                helper.assertFalse(hasCameraTicket(tickets,ChunkPos.containing(pos)),"The last frame unlink must release its camera ticket");
                helper.succeed();
            } finally {
                camera.unlinkFrameForPlayer(owner.getUUID(),null);
                PortTestPlayers.remove(owner);
            }
        });
    }

    @GameTest(maxTicks=60)
    public void sentryTargetsIntrudersAndHonorsTeamOwnership(GameTestHelper helper) {
        ServerPlayer owner=PortTestPlayers.create(helper,"sc_sentryown");
        ServerPlayer intruder=PortTestPlayers.create(helper,"sc_sentryoth");
        ServerLevel level=helper.getLevel();
        boolean oldTeams=ConfigHandler.SERVER.enableTeamOwnership.get();
        var scoreboard=level.getServer().getScoreboard();
        var team=scoreboard.addPlayerTeam("sc_test_"+UUID.randomUUID().toString().substring(0,7));
        Sentry sentry=null;
        try {
            for(int x=0;x<8;x++)for(int z=0;z<8;z++)helper.setBlock(x,1,z,Blocks.STONE);
            var location=helper.absolutePos(new BlockPos(3,2,3));
            sentry=new Sentry(SCContent.SENTRY_ENTITY.get(),level);
            sentry.setPos(location.getX()+0.5,location.getY(),location.getZ()+0.5);
            sentry.setUpSentry(owner);
            sentry.toggleMode(owner,Sentry.SentryMode.AGGRESSIVE_P.ordinal(),false);
            level.addFreshEntity(sentry);
            owner.snapTo(location.getX()-2,location.getY(),location.getZ()+0.5);
            intruder.snapTo(location.getX()+2,location.getY(),location.getZ()+0.5);
            ConfigHandler.SERVER.enableTeamOwnership.set(false);
            var goal=new TargetNearestPlayerOrMobGoal(sentry);
            helper.assertTrue(goal.canUse(),"An active player-targeting sentry must find an intruder");
            helper.assertTrue(sentry.getTarget()==intruder,"The owner must be excluded while the intruder is selected");
            scoreboard.addPlayerToTeam(owner.getGameProfile().name(),team);
            scoreboard.addPlayerToTeam(intruder.getGameProfile().name(),team);
            ConfigHandler.SERVER.enableTeamOwnership.set(true);
            sentry.setTarget(null);
            helper.assertFalse(new TargetNearestPlayerOrMobGoal(sentry).canUse(),"Shared team ownership must prevent targeting a teammate");
            helper.succeed();
        } finally {
            ConfigHandler.SERVER.enableTeamOwnership.set(oldTeams);
            scoreboard.removePlayerTeam(team);
            if(sentry!=null)sentry.discard();
            PortTestPlayers.remove(owner,intruder);
        }
    }

    @GameTest(maxTicks=100)
    public void remoteFrameSourceContinuesTickingWithoutNearbyPlayers(GameTestHelper helper) {
        ServerPlayer viewer=PortTestPlayers.create(helper,"sc_farframe");
        ServerLevel level=helper.getLevel();
        helper.setBlock(1,1,1,Blocks.STONE);
        BlockPos body=helper.absolutePos(new BlockPos(1,2,1));
        viewer.snapTo(body.getX()+0.5,body.getY(),body.getZ()+0.5);
        // Deliberately outside every GameTest structure ticket and every player's simulation radius.
        BlockPos source=new BlockPos(4096,96,4096);
        level.getChunkAt(source);
        level.setBlockAndUpdate(source.south(),Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(source,SCContent.SECURITY_CAMERA.get().defaultBlockState());
        var camera=(SecurityCameraBlockEntity)level.getBlockEntity(source);
        camera.setOwner(viewer.getUUID().toString(),viewer.getGameProfile().name());
        double initialRotation=camera.getCameraRotation();
        camera.linkFrameForPlayer(viewer,helper.absolutePos(new BlockPos(2,2,2)),1);
        helper.runAfterDelay(25,()-> {
            try {
                helper.assertTrue(viewer.distanceToSqr(Vec3.atCenterOf(source))>1024*1024,"Fixture must keep the body far outside the source simulation distance");
                helper.assertTrue(level.getChunkAt(source).getFullStatus().isOrAfter(net.minecraft.server.level.FullChunkStatus.BLOCK_TICKING),"Remote frame source must retain block ticking, not only a loaded chunk");
                helper.assertTrue(camera.getCameraRotation()!=initialRotation,"Remote camera must continue ticking and rotating without a nearby player");
                // The 3x3 frame view cannot finish its queue unless the remote camera block entity is ticking.
                var tickets=level.getDataStorage().computeIfAbsent(TicketStorage.TYPE);
                ChunkPos center=ChunkPos.containing(source);
                helper.assertTrue(hasCameraTicket(tickets,new ChunkPos(center.x()+1,center.z()+1)),"Remote source ticker must drain its frame chunk-loading queue");
                helper.succeed();
            } finally {
                camera.unlinkFrameForPlayer(viewer.getUUID(),null);
                camera.clearCameraFeedChunks(viewer);
                level.removeBlock(source,false);
                level.removeBlock(source.south(),false);
                PortTestPlayers.remove(viewer);
            }
        });
    }

    private static boolean hasCameraTicket(TicketStorage tickets,ChunkPos chunk) {
        return tickets.getTickets(chunk.pack()).stream().anyMatch(ticket->{
            var id=BuiltInRegistries.TICKET_TYPE.getKey(ticket.getType());
            return id!=null && id.getNamespace().equals("securitycraft") && id.getPath().startsWith("camera_");
        });
    }
}
