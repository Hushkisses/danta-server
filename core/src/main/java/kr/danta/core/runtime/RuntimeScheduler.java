package kr.danta.core.runtime;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.UUID;

/**
 * PriorityQueue based scheduler driven by {@link RuntimeClockService}.
 *
 * <p>The scheduler never scans all queued jobs. A pump only inspects the queue
 * head and executes tasks whose due runtime has been reached.</p>
 */
public final class RuntimeScheduler {
    private static final Comparator<RuntimeScheduledTask> TASK_ORDER =
            Comparator.comparingLong(RuntimeScheduledTask::dueRuntimeMillis)
                    .thenComparing(task -> task.id().toString());

    private final RuntimeClockService clock;
    private final PriorityQueue<RuntimeScheduledTask> queue = new PriorityQueue<>(TASK_ORDER);
    private final Map<UUID, RuntimeScheduledTask> tasksById = new HashMap<>();
    private final Map<String, RuntimeTaskHandler> handlers = new HashMap<>();

    public RuntimeScheduler(RuntimeClockService clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public synchronized void registerHandler(String taskType, RuntimeTaskHandler handler) {
        if (taskType == null || taskType.isBlank()) {
            throw new IllegalArgumentException("taskType must not be blank");
        }
        Objects.requireNonNull(handler, "handler");
        if (handlers.putIfAbsent(taskType, handler) != null) {
            throw new IllegalStateException("Handler already registered for task type: " + taskType);
        }
    }

    public synchronized RuntimeScheduledTask scheduleAfter(
            Duration delay,
            String taskType,
            Map<String, String> payload
    ) {
        Objects.requireNonNull(delay, "delay");
        if (delay.isNegative()) {
            throw new IllegalArgumentException("delay must be >= 0");
        }
        long delayMillis;
        try {
            delayMillis = delay.toMillis();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("delay is too large", ex);
        }
        return scheduleAt(safeAdd(clock.elapsedMillis(), delayMillis), taskType, payload);
    }

    public synchronized RuntimeScheduledTask scheduleAt(
            long dueRuntimeMillis,
            String taskType,
            Map<String, String> payload
    ) {
        RuntimeScheduledTask task = new RuntimeScheduledTask(
                UUID.randomUUID(), dueRuntimeMillis, taskType, payload);
        add(task);
        return task;
    }

    /** Adds an already identified task, intended for future persistence restore. */
    public synchronized void restore(RuntimeScheduledTask task) {
        add(Objects.requireNonNull(task, "task"));
    }

    private void add(RuntimeScheduledTask task) {
        if (tasksById.putIfAbsent(task.id(), task) != null) {
            throw new IllegalArgumentException("Duplicate runtime task id: " + task.id());
        }
        queue.add(task);
    }

    public synchronized boolean cancel(UUID taskId) {
        RuntimeScheduledTask task = tasksById.remove(Objects.requireNonNull(taskId, "taskId"));
        return task != null && queue.remove(task);
    }

    public synchronized Optional<RuntimeScheduledTask> find(UUID taskId) {
        return Optional.ofNullable(tasksById.get(taskId));
    }

    public synchronized Optional<RuntimeScheduledTask> nextTask() {
        return Optional.ofNullable(queue.peek());
    }

    public synchronized int size() {
        return queue.size();
    }

    public synchronized List<RuntimeScheduledTask> snapshot() {
        List<RuntimeScheduledTask> copy = new ArrayList<>(queue);
        copy.sort(TASK_ORDER);
        return List.copyOf(copy);
    }

    /**
     * Executes every task due at the current runtime and returns their results.
     * Tasks are removed before handler execution, preventing duplicate execution
     * if the pump is called again immediately.
     */
    public List<RuntimeTaskExecution> executeDueTasks() {
        List<RuntimeTaskExecution> results = new ArrayList<>();

        while (true) {
            RuntimeScheduledTask task;
            RuntimeTaskHandler handler;
            synchronized (this) {
                task = queue.peek();
                if (task == null || task.dueRuntimeMillis() > clock.elapsedMillis()) {
                    break;
                }
                queue.poll();
                tasksById.remove(task.id());
                handler = handlers.get(task.taskType());
            }

            if (handler == null) {
                results.add(RuntimeTaskExecution.failure(
                        task,
                        new IllegalStateException("No handler registered for task type: " + task.taskType())));
                continue;
            }

            try {
                handler.execute(task);
                results.add(RuntimeTaskExecution.success(task));
            } catch (Exception ex) {
                results.add(RuntimeTaskExecution.failure(task, ex));
            }
        }

        return List.copyOf(results);
    }

    private static long safeAdd(long left, long right) {
        try {
            return Math.addExact(left, right);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("runtime deadline overflow", ex);
        }
    }
}
