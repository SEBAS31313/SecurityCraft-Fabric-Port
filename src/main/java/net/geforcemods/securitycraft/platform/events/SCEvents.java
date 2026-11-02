package net.geforcemods.securitycraft.platform.events;

import net.geforcemods.securitycraft.platform.GameplayEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.util.TriState;

/** Typed events emitted by Fabric callbacks and SecurityCraft's vanilla mixins. */
public final class SCEvents {
    private SCEvents() {}
    public static class OnDatapackSyncEvent extends GameplayEvent {
        private final ServerPlayer player;
        public OnDatapackSyncEvent(ServerPlayer player) { this.player=player; }
        public ServerPlayer getPlayer() { return player; }
    }
    public static class EntityLeaveLevelEvent extends GameplayEvent {
        private final Entity entity;
        private final Level level;
        public EntityLeaveLevelEvent(Entity entity, Level level) { this.entity=entity;this.level=level; }
        public Entity getEntity() { return entity; }
        public Level getLevel() { return level; }
    }
    public static class ServerAboutToStartEvent extends GameplayEvent {
        private final MinecraftServer server;
        public ServerAboutToStartEvent(MinecraftServer server) { this.server=server; }
        public MinecraftServer getServer() { return server; }
    }
    public static class ServerStoppedEvent extends GameplayEvent {
        private final MinecraftServer server;
        public ServerStoppedEvent(MinecraftServer server) { this.server=server; }
        public MinecraftServer getServer() { return server; }
    }
    public static class EntityInvulnerabilityCheckEvent extends GameplayEvent {
        private final Entity entity;
        private final DamageSource source;
        public EntityInvulnerabilityCheckEvent(Entity entity, DamageSource source) { this.entity=entity;this.source=source; }
        public Entity getEntity() { return entity; }
        public DamageSource getSource() { return source; }
        private boolean invulnerable; public boolean isInvulnerable(){return invulnerable;} public void setInvulnerable(boolean value){invulnerable=value;}
    }
    public static class LivingIncomingDamageEvent extends GameplayEvent {
        private final LivingEntity entity;
        private final DamageSource source;
        public LivingIncomingDamageEvent(LivingEntity entity, DamageSource source) { this.entity=entity;this.source=source; }
        public LivingEntity getEntity() { return entity; }
        public DamageSource getSource() { return source; }
    }
    public static class LivingChangeTargetEvent extends GameplayEvent {
        private final LivingEntity entity;
        private final LivingEntity newAboutToBeSetTarget;
        public LivingChangeTargetEvent(LivingEntity entity, LivingEntity newAboutToBeSetTarget) { this.entity=entity;this.newAboutToBeSetTarget=newAboutToBeSetTarget; }
        public LivingEntity getEntity() { return entity; }
        public LivingEntity getNewAboutToBeSetTarget() { return newAboutToBeSetTarget; }
    }
    public static class LivingDestroyBlockEvent extends GameplayEvent {
        private final LivingEntity entity;
        private final BlockState state;
        public LivingDestroyBlockEvent(LivingEntity entity, BlockState state) { this.entity=entity;this.state=state; }
        public LivingEntity getEntity() { return entity; }
        public BlockState getState() { return state; }
    }
    public static class FurnaceFuelBurnTimeEvent extends GameplayEvent {
        private final ItemStack itemStack;
        public FurnaceFuelBurnTimeEvent(ItemStack itemStack) { this.itemStack=itemStack; }
        public ItemStack getItemStack() { return itemStack; }
        private int burnTime; public int getBurnTime(){return burnTime;} public void setBurnTime(int value){burnTime=value;}
    }
    public static class AnvilUpdateEvent extends GameplayEvent {
        private final ItemStack left;
        private final ItemStack right;
        public AnvilUpdateEvent(ItemStack left, ItemStack right) { this.left=left;this.right=right; }
        public ItemStack getLeft() { return left; }
        public ItemStack getRight() { return right; }
    }
    public static class EntityMountEvent extends GameplayEvent {
        private final Entity entityMounting;
        private final Entity entityBeingMounted;
        private final boolean dismounting;
        public EntityMountEvent(Entity entityMounting, Entity entityBeingMounted, boolean dismounting) { this.entityMounting=entityMounting;this.entityBeingMounted=entityBeingMounted;this.dismounting=dismounting; }
        public Entity getEntityMounting() { return entityMounting; }
        public Entity getEntityBeingMounted() { return entityBeingMounted; }
        public boolean isDismounting() { return dismounting; }
    }
    public static class UseItemOnBlockEvent extends GameplayEvent {
        private final Player player;
        private final Level level;
        private final BlockPos pos;
        private final ItemStack itemStack;
        public UseItemOnBlockEvent(Player player, Level level, BlockPos pos, ItemStack itemStack) { this.player=player;this.level=level;this.pos=pos;this.itemStack=itemStack; }
        public Player getPlayer() { return player; }
        public Level getLevel() { return level; }
        public BlockPos getPos() { return pos; }
        public ItemStack getItemStack() { return itemStack; }
        public UsePhase getUsePhase(){return UsePhase.ITEM_AFTER_BLOCK;} private InteractionResult result=InteractionResult.PASS; public void setCancellationResult(InteractionResult value){result=value;} public InteractionResult getCancellationResult(){return result;} public enum UsePhase {ITEM_AFTER_BLOCK}
    }
    public static class BreakBlockEvent extends GameplayEvent {
        private final Level level;
        private final Player player;
        private final BlockPos pos;
        private final BlockState state;
        public BreakBlockEvent(Level level, Player player, BlockPos pos, BlockState state) { this.level=level;this.player=player;this.pos=pos;this.state=state; }
        public Level getLevel() { return level; }
        public Player getPlayer() { return player; }
        public BlockPos getPos() { return pos; }
        public BlockState getState() { return state; }
    }
    public static class ServerTickEvent {
        public static class Pre extends GameplayEvent {
            private final MinecraftServer server;
            public Pre(MinecraftServer server){this.server=server;}
            public MinecraftServer getServer(){return server;}
        }
    }
    public static class LivingDamageEvent {
        public static class Post extends GameplayEvent {
            private final LivingEntity entity; private final DamageSource source;
            public Post(LivingEntity entity,DamageSource source){this.entity=entity;this.source=source;}
            public LivingEntity getEntity(){return entity;} public DamageSource getSource(){return source;}
        }
    }
    public static class PlayerInteractEvent extends GameplayEvent {
        private final Player player; private final Level level; private final InteractionHand hand; private final BlockPos pos;
        private InteractionResult result=InteractionResult.FAIL;
        private TriState useItem=TriState.DEFAULT; private TriState useBlock=TriState.DEFAULT;
        public PlayerInteractEvent(Player player,Level level,InteractionHand hand,BlockPos pos){this.player=player;this.level=level;this.hand=hand;this.pos=pos;}
        public Player getEntity(){return player;} public Player getPlayer(){return player;} public Level getLevel(){return level;} public InteractionHand getHand(){return hand;} public BlockPos getPos(){return pos;} public ItemStack getItemStack(){return player.getItemInHand(hand);}
        public void setCancellationResult(InteractionResult value){result=value;} public InteractionResult getCancellationResult(){return result;}
        public void setUseItem(TriState value){useItem=value;} public TriState getUseItem(){return useItem;}
        public void setUseBlock(TriState value){useBlock=value;} public TriState getUseBlock(){return useBlock;}
        public static class RightClickBlock extends PlayerInteractEvent {public RightClickBlock(Player p,Level l,InteractionHand h,BlockPos b){super(p,l,h,b);}}
        public static class LeftClickBlock extends PlayerInteractEvent {public LeftClickBlock(Player p,Level l,InteractionHand h,BlockPos b){super(p,l,h,b);}}
        public static class RightClickItem extends PlayerInteractEvent {public RightClickItem(Player p,Level l,InteractionHand h){super(p,l,h,p.blockPosition());}}
    }
    public static class BlockEvent {
        public static class EntityPlaceEvent extends GameplayEvent {
            private final Entity entity;private final Level level;private final BlockPos pos;private final BlockState state;
            public EntityPlaceEvent(Entity entity,Level level,BlockPos pos,BlockState state){this.entity=entity;this.level=level;this.pos=pos;this.state=state;}
            public Entity getEntity(){return entity;} public Level getLevel(){return level;} public BlockPos getPos(){return pos;} public BlockState getState(){return state;}
        }
    }
    public static class PlayerEvent {
        public static class HarvestCheck extends GameplayEvent {
            private final Player entity;private final Level level;private final BlockPos pos;private boolean harvest;
            public HarvestCheck(Player entity,Level level,BlockPos pos,boolean harvest){this.entity=entity;this.level=level;this.pos=pos;this.harvest=harvest;}
            public Player getEntity(){return entity;} public Level getLevel(){return level;} public BlockPos getPos(){return pos;} public void setCanHarvest(boolean value){harvest=value;} public boolean canHarvest(){return harvest;}
        }
    }
    public static class NoteBlockEvent {
        public static class Play extends GameplayEvent {
            private final Level level;private final BlockPos pos;private final int note;private final NoteBlockInstrument instrument;
            public Play(Level level,BlockPos pos,int note,NoteBlockInstrument instrument){this.level=level;this.pos=pos;this.note=note;this.instrument=instrument;}
            public Level getLevel(){return level;} public BlockPos getPos(){return pos;} public int getVanillaNoteId(){return note;} public NoteBlockInstrument getInstrument(){return instrument;}
        }
    }
}
