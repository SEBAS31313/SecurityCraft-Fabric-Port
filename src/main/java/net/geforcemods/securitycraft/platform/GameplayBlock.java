package net.geforcemods.securitycraft.platform;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
/** Behaviour hooks supplied by NeoForge upstream and called by the Fabric gameplay mixins. */
public interface GameplayBlock {
 default int getLightEmission(BlockState state,BlockGetter level,BlockPos pos){return state.getLightEmission();}
 default boolean canConnectRedstone(BlockState state,BlockGetter level,BlockPos pos,Direction direction){return state.isSignalSource()&&direction!=null;}
 default BubbleColumnDirection getBubbleColumnDirection(BlockState state){return BubbleColumnDirection.NONE;}
 default TriState canSustainPlant(BlockState state,BlockGetter level,BlockPos pos,Direction direction,BlockState plant){
  return TriState.DEFAULT;
 }
 default boolean onTreeGrow(BlockState state,WorldGenLevel level,BiConsumer<BlockPos,BlockState> place,RandomSource random,BlockPos pos,TreeConfiguration config){return false;}
 default boolean isConduitFrame(BlockState state,LevelReader level,BlockPos pos,BlockPos conduit){return false;}

 default net.minecraft.world.level.block.SoundType getSoundType(BlockState state,LevelReader level,BlockPos pos,net.minecraft.world.entity.Entity entity){return state.getSoundType();}
 default net.minecraft.world.item.ItemStack getCloneItemStack(LevelReader level,BlockPos pos,BlockState state,boolean includeData,net.minecraft.world.entity.player.Player player){return state.getCloneItemStack(level,pos,includeData);}
 default boolean hasDynamicLightEmission(BlockState state){return false;}
 default boolean shouldDisplayFluidOverlay(BlockState state,net.minecraft.world.level.BlockAndLightGetter level,BlockPos pos,net.minecraft.world.level.material.FluidState fluid){return false;}
 default boolean shouldCheckWeakPower(BlockState state,net.minecraft.world.level.SignalGetter level,BlockPos pos,Direction side){return state.isRedstoneConductor(level,pos);}
 default void onNeighborChange(BlockState state,LevelReader level,BlockPos pos,BlockPos neighbor){}
 default boolean onDestroyedByPlayer(BlockState state,net.minecraft.world.level.Level level,BlockPos pos,net.minecraft.world.entity.player.Player player,net.minecraft.world.item.ItemStack stack,boolean harvest,net.minecraft.world.level.material.FluidState fluid){return level.setBlock(pos,fluid.createLegacyBlock(),level.isClientSide()?11:3);}
 default boolean isPortalFrame(BlockState state,BlockGetter level,BlockPos pos){return false;}
 default boolean isScaffolding(BlockState state,LevelReader level,BlockPos pos,net.minecraft.world.entity.LivingEntity entity){return state.is(net.minecraft.world.level.block.Blocks.SCAFFOLDING);}
 default boolean isLadder(BlockState state,LevelReader level,BlockPos pos,net.minecraft.world.entity.LivingEntity entity){return state.is(net.minecraft.tags.BlockTags.CLIMBABLE);}
}
