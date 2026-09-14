package kr.danta.core.persistence;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Small Paper-independent asynchronous repository contract used by DEV-016.
 * Domain-specific repositories will follow the same rule: callers never block
 * the Minecraft main thread waiting for database I/O.
 */
public interface AsyncKeyValueRepository {
    CompletableFuture<Void> save(String namespace, String key, String value);

    CompletableFuture<Optional<String>> find(String namespace, String key);

    CompletableFuture<Boolean> delete(String namespace, String key);
}
