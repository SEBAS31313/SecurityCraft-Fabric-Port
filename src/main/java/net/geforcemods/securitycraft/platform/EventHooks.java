package net.geforcemods.securitycraft.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/** Extensible Fabric-side hooks for the piston and fake-fluid operations owned by SecurityCraft. */
public final class EventHooks {
    private EventHooks() {}
    public static BlockState fireFluidPlaceBlockEvent(LevelAccessor level,BlockPos pos,BlockPos origin,BlockState state) {
        return GameplayEventBus.EVENT_BUS.post(new FluidPlace(level,pos,origin,state)).state();
    }
    public static boolean onPistonMovePre(Level level,BlockPos pos,Direction direction,boolean extending) {
        return GameplayEventBus.EVENT_BUS.post(new PistonMove(level,pos,direction,extending,true)).isCanceled();
    }
    public static void onPistonMovePost(Level level,BlockPos pos,Direction direction,boolean extending) {
        GameplayEventBus.EVENT_BUS.post(new PistonMove(level,pos,direction,extending,false));
    }
    public static final class FluidPlace extends GameplayEvent {
        public final LevelAccessor level; public final BlockPos pos; public final BlockPos origin; private BlockState state;
        public FluidPlace(LevelAccessor level,BlockPos pos,BlockPos origin,BlockState state){this.level=level;this.pos=pos;this.origin=origin;this.state=state;}
        public BlockState state(){return state;} public void setState(BlockState value){state=java.util.Objects.requireNonNull(value);}
    }
    public static final class PistonMove extends GameplayEvent {
        public final Level level; public final BlockPos pos; public final Direction direction; public final boolean extending; public final boolean before;
        public PistonMove(Level level,BlockPos pos,Direction direction,boolean extending,boolean before){this.level=level;this.pos=pos;this.direction=direction;this.extending=extending;this.before=before;}
    }
}
