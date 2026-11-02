package net.geforcemods.securitycraft.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** In-process hooks shared by SecurityCraft blocks and Fabric vanilla event adapters. */
public final class GameplayEventBus {
    public static final GameplayEventBus EVENT_BUS = new GameplayEventBus();
    private final List<Listener<?>> listeners = new ArrayList<>();
    public <T extends GameplayEvent> void addListener(Class<T> type, Consumer<T> callback) {
        listeners.add(new Listener<>(type, callback));
    }
    public <T extends GameplayEvent> T post(T event) {
        for (Listener<?> listener : List.copyOf(listeners)) {
            listener.accept(event);
            if (event.isCanceled()) break;
        }
        return event;
    }
    private record Listener<T extends GameplayEvent>(Class<T> type, Consumer<T> callback) {
        void accept(GameplayEvent event) { if (type.isInstance(event)) callback.accept(type.cast(event)); }
    }
}
