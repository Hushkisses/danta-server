package kr.danta.core.event;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** Lightweight synchronous in-process domain event bus. */
public final class DomainEventBus {
    private final Map<Class<?>, CopyOnWriteArrayList<Consumer<?>>> subscribers = new ConcurrentHashMap<>();

    public <E extends DomainEvent> AutoCloseable subscribe(Class<E> type, Consumer<E> consumer) {
        Objects.requireNonNull(type); Objects.requireNonNull(consumer);
        subscribers.computeIfAbsent(type, ignored -> new CopyOnWriteArrayList<>()).add(consumer);
        return () -> subscribers.getOrDefault(type, new CopyOnWriteArrayList<>()).remove(consumer);
    }

    public void publish(DomainEvent event) {
        Objects.requireNonNull(event);
        List<Consumer<?>> handlers = subscribers.getOrDefault(event.getClass(), new CopyOnWriteArrayList<>());
        for (Consumer<?> raw : handlers) dispatch(raw, event);
    }

    @SuppressWarnings("unchecked")
    private static <E extends DomainEvent> void dispatch(Consumer<?> raw, E event) {
        ((Consumer<E>) raw).accept(event);
    }
}
