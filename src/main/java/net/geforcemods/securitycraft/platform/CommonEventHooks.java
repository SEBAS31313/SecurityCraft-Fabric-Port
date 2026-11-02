package net.geforcemods.securitycraft.platform;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.player.PlayerPickItemEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.registry.FuelValueEvents;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SCEventHandler;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedCarpetBlock;
import net.geforcemods.securitycraft.misc.OwnershipEvent;
import net.geforcemods.securitycraft.platform.events.EntityTeleportEvent;
import net.geforcemods.securitycraft.platform.events.SCEvents.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;

/** Fabric callbacks for the upstream gameplay listeners; vanilla-only hooks are supplied by event mixins. */
public final class CommonEventHooks {
    private static boolean registered;
    private CommonEventHooks() {}
    public static void register() {
        if (registered) return;
        registered=true;
        var bus=GameplayEventBus.EVENT_BUS;
        bus.addListener(ServerTickEvent.Pre.class, SCEventHandler::onServerTickPre);
        bus.addListener(OnDatapackSyncEvent.class, SCEventHandler::onDatapackSync);
        bus.addListener(EntityLeaveLevelEvent.class, SCEventHandler::onEntityLeaveLevel);
        bus.addListener(ServerAboutToStartEvent.class, SCEventHandler::onServerAboutToStart);
        bus.addListener(ServerStoppedEvent.class, SCEventHandler::onServerStop);
        bus.addListener(EntityInvulnerabilityCheckEvent.class, SCEventHandler::onEntityInvulnerabilityCheck);
        bus.addListener(LivingIncomingDamageEvent.class, SCEventHandler::onLivingAttacked);
        bus.addListener(LivingDamageEvent.Post.class, SCEventHandler::onDamageTaken);
        bus.addListener(EntityMountEvent.class, SCEventHandler::onDismount);
        bus.addListener(UseItemOnBlockEvent.class, SCEventHandler::onUseItemOnBlock);
        bus.addListener(PlayerInteractEvent.RightClickBlock.class, SCEventHandler::highestPriorityOnRightClickBlock);
        bus.addListener(PlayerInteractEvent.RightClickBlock.class, SCEventHandler::onRightClickBlock);
        bus.addListener(PlayerInteractEvent.LeftClickBlock.class, SCEventHandler::onLeftClickBlock);
        bus.addListener(BreakBlockEvent.class, SCEventHandler::onBlockEventBreak);
        bus.addListener(BlockEvent.EntityPlaceEvent.class, SCEventHandler::onBlockEventPlace);
        bus.addListener(PlayerEvent.HarvestCheck.class, SCEventHandler::onPlayerHarvestCheck);
        bus.addListener(OwnershipEvent.class, SCEventHandler::onOwnership);
        bus.addListener(LivingChangeTargetEvent.class, SCEventHandler::onLivingSetAttackTarget);
        bus.addListener(LivingDestroyBlockEvent.class, SCEventHandler::onLivingDestroyEvent);
        bus.addListener(PlayerInteractEvent.RightClickItem.class, SCEventHandler::onRightClickItem);
        bus.addListener(FurnaceFuelBurnTimeEvent.class, SCEventHandler::onFurnaceFuelBurnTime);
        bus.addListener(AnvilUpdateEvent.class, SCEventHandler::onAnvilCraftPre);
        bus.addListener(EntityTeleportEvent.class, SCEventHandler::onEntityTeleport);
        bus.addListener(NoteBlockEvent.Play.class, SCEventHandler::onNoteBlockPlayed);
        ServerTickEvents.START_SERVER_TICK.register(server -> bus.post(new ServerTickEvent.Pre(server)));
        ServerLifecycleEvents.SERVER_STARTING.register(server -> bus.post(new ServerAboutToStartEvent(server)));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> bus.post(new ServerStoppedEvent(server)));
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player,joined) -> bus.post(new OnDatapackSyncEvent(player)));
        ServerEntityEvents.ENTITY_UNLOAD.register((entity,level) -> bus.post(new EntityLeaveLevelEvent(entity,level)));
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity,source,amount) -> !bus.post(new LivingIncomingDamageEvent(entity,source)).isCanceled());
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity,source,baseDamage,damageTaken,blocked) -> bus.post(new LivingDamageEvent.Post(entity,source)));
        AttackBlockCallback.EVENT.register((player,level,hand,pos,direction) -> bus.post(new PlayerInteractEvent.LeftClickBlock(player,level,hand,pos)).isCanceled() ? InteractionResult.FAIL : InteractionResult.PASS);
        UseItemCallback.EVENT.register((player,level,hand) -> bus.post(new PlayerInteractEvent.RightClickItem(player,level,hand)).isCanceled() ? InteractionResult.FAIL : InteractionResult.PASS);
        UseBlockCallback.EVENT.register((player,level,hand,hit) -> {
            var event=bus.post(new PlayerInteractEvent.RightClickBlock(player,level,hand,hit.getBlockPos()));
            if(event.isCanceled()) return event.getCancellationResult();
            var itemEvent=bus.post(new UseItemOnBlockEvent(player,level,hit.getBlockPos(),player.getItemInHand(hand)));
            if(itemEvent.isCanceled()) return itemEvent.getCancellationResult();
            if(event.getUseBlock()==TriState.FALSE && event.getUseItem()==TriState.TRUE)
                return player.getItemInHand(hand).useOn(new UseOnContext(player,hand,hit));
            if(event.getUseItem()==TriState.FALSE) {
                var result=level.getBlockState(hit.getBlockPos()).useWithoutItem(level,player,hit);
                return result.consumesAction() ? result : InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
        PlayerBlockBreakEvents.BEFORE.register((level,player,pos,state,entity) -> !bus.post(new BreakBlockEvent(level,player,pos,state)).isCanceled());
        PlayerPickItemEvents.BLOCK.register((player,pos,state,includeData) -> state.getBlock() instanceof GameplayBlock block ? block.getCloneItemStack(player.level(),pos,state,includeData,player) : null);
        FuelValueEvents.BUILD.register((builder,context) -> {
            for(var block:BuiltInRegistries.BLOCK)
                if(block instanceof ReinforcedCarpetBlock || block==SCContent.ELECTRIFIED_IRON_FENCE_GATE.get()) builder.add(block,0);
        });
    }
}
