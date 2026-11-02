package net.geforcemods.securitycraft.platform;

/** A synchronous gameplay notification with optional cancellation. */
public class GameplayEvent {
    private boolean canceled;
    public boolean isCanceled() { return canceled; }
    public void setCanceled(boolean value) { canceled = value; }
}
