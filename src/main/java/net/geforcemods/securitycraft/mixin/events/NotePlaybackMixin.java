package net.geforcemods.securitycraft.mixin.events;

import net.geforcemods.securitycraft.platform.GameplayEventBus;
import net.geforcemods.securitycraft.platform.events.SCEvents.NoteBlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoteBlock.class)
public abstract class NotePlaybackMixin {
    @Inject(method="playNote",at=@At("HEAD"))
    private void securitycraft$note(Entity source,BlockState state,Level level,BlockPos pos,CallbackInfo ci) {
        if(!level.isClientSide() && (state.getValue(NoteBlock.INSTRUMENT).worksAboveNoteBlock() || level.getBlockState(pos.above()).isAir())) GameplayEventBus.EVENT_BUS.post(new NoteBlockEvent.Play(level,pos,state.getValue(NoteBlock.NOTE),state.getValue(NoteBlock.INSTRUMENT)));
    }
}
