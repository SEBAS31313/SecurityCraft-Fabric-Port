package net.geforcemods.securitycraft.platform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
/** Repeats the vanilla notification phase after a piston reinstalls a block entity with its original data. */
public final class WorldUpdates {
 private WorldUpdates(){}
 public static void markAndNotify(Level level,BlockPos pos,BlockState oldState,BlockState newState,int flags,int limit){
  if(oldState!=newState)level.setBlocksDirty(pos,oldState,newState);
  if((flags&2)!=0)level.sendBlockUpdated(pos,oldState,newState,flags);
  if((flags&1)!=0){level.updateNeighborsAt(pos,oldState.getBlock());if(!level.isClientSide()&&newState.hasAnalogOutputSignal())level.updateNeighbourForOutputSignal(pos,newState.getBlock());}
  if((flags&16)==0&&limit>0){int neighborFlags=flags&-34;oldState.updateIndirectNeighbourShapes(level,pos,neighborFlags,limit-1);newState.updateNeighbourShapes(level,pos,neighborFlags,limit-1);newState.updateIndirectNeighbourShapes(level,pos,neighborFlags,limit-1);}
  level.updatePOIOnBlockStateChange(pos,oldState,newState);
 }
}
