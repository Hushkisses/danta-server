package kr.danta.core.runtime;

/** Result of attempting one due runtime task. */
public record RuntimeTaskExecution(
        RuntimeScheduledTask task,
        boolean success,
        Exception error
) {
    public static RuntimeTaskExecution success(RuntimeScheduledTask task) {
        return new RuntimeTaskExecution(task, true, null);
    }

    public static RuntimeTaskExecution failure(RuntimeScheduledTask task, Exception error) {
        return new RuntimeTaskExecution(task, false, error);
    }
}
