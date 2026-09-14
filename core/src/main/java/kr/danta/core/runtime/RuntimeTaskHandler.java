package kr.danta.core.runtime;

/** Handler for one logical runtime task type. */
@FunctionalInterface
public interface RuntimeTaskHandler {
    void execute(RuntimeScheduledTask task) throws Exception;
}
