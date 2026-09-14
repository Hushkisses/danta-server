package kr.danta.paper.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * DEV-016 PostgreSQL adapter.
 *
 * All JDBC operations run on a dedicated virtual-thread executor and never on
 * Paper's main thread. Connection failure degrades persistence only; it does
 * not disable the game plugin.
 */
public final class PostgresDatabaseService implements AutoCloseable {
    private final DatabaseConfig config;
    private final Logger logger;
    private final ExecutorService executor;
    private final AtomicReference<DatabaseStatus> status;
    private volatile String lastError;

    public PostgresDatabaseService(DatabaseConfig config, Logger logger) {
        this.config = Objects.requireNonNull(config, "config");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.executor = Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual().name("danta-db-", 0).factory());
        this.status = new AtomicReference<>(config.enabled()
                ? DatabaseStatus.CONNECTING
                : DatabaseStatus.DISABLED);
    }

    public CompletableFuture<Boolean> initializeAsync() {
        if (!config.enabled()) {
            status.set(DatabaseStatus.DISABLED);
            return CompletableFuture.completedFuture(false);
        }
        if (status.get() == DatabaseStatus.CLOSED) {
            return CompletableFuture.failedFuture(new IllegalStateException("database service is closed"));
        }

        status.set(DatabaseStatus.CONNECTING);
        return CompletableFuture.supplyAsync(() -> {
            try {
                Class.forName("org.postgresql.Driver");
                try (Connection connection = openConnection()) {
                    connection.setAutoCommit(true);
                    try (Statement statement = connection.createStatement()) {
                        statement.execute("""
                                CREATE TABLE IF NOT EXISTS danta_schema_meta (
                                    meta_key VARCHAR(100) PRIMARY KEY,
                                    meta_value TEXT NOT NULL,
                                    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                                )
                                """);
                        statement.execute("""
                                CREATE TABLE IF NOT EXISTS danta_kv (
                                    namespace VARCHAR(64) NOT NULL,
                                    state_key VARCHAR(128) NOT NULL,
                                    state_value TEXT NOT NULL,
                                    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                    PRIMARY KEY (namespace, state_key)
                                )
                                """);
                    }
                    try (PreparedStatement ps = connection.prepareStatement("""
                            INSERT INTO danta_schema_meta(meta_key, meta_value, updated_at)
                            VALUES ('schema_version', '1', NOW())
                            ON CONFLICT (meta_key) DO UPDATE
                            SET meta_value = EXCLUDED.meta_value, updated_at = NOW()
                            """)) {
                        ps.executeUpdate();
                    }
                }
                lastError = null;
                status.set(DatabaseStatus.READY);
                logger.info("PostgreSQL ready: " + config.safeDescription());
                return true;
            } catch (Exception ex) {
                markFailure("PostgreSQL initialization failed", ex);
                return false;
            }
        }, executor);
    }

    public CompletableFuture<Boolean> pingAsync() {
        return submit("ping", connection -> {
            try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery("SELECT 1")) {
                return rs.next() && rs.getInt(1) == 1;
            }
        });
    }

    public <T> CompletableFuture<T> submit(String operation, DatabaseWork<T> work) {
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(work, "work");

        if (!config.enabled()) {
            return CompletableFuture.failedFuture(new IllegalStateException("database is disabled in database.properties"));
        }
        if (status.get() == DatabaseStatus.CLOSED) {
            return CompletableFuture.failedFuture(new IllegalStateException("database service is closed"));
        }

        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = openConnection()) {
                T result = work.execute(connection);
                lastError = null;
                status.set(DatabaseStatus.READY);
                return result;
            } catch (Exception ex) {
                markFailure("Database operation failed: " + operation, ex);
                throw new DatabaseOperationException(operation, ex);
            }
        }, executor);
    }

    public DatabaseHealth health() {
        return new DatabaseHealth(status.get(), config.safeDescription(), lastError);
    }

    public DatabaseConfig config() {
        return config;
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(config.jdbcUrl(), config.user(), config.password());
    }

    private void markFailure(String message, Exception ex) {
        lastError = ex.getClass().getSimpleName() + ": " + safeMessage(ex);
        status.set(DatabaseStatus.DEGRADED);
        logger.warning(message + ". Persistence is degraded, game server remains active. Cause: " + lastError);
    }

    private static String safeMessage(Exception ex) {
        String value = ex.getMessage();
        return value == null || value.isBlank() ? "(no message)" : value;
    }

    @Override public void close() {
        status.set(DatabaseStatus.CLOSED);
        executor.shutdown();
    }

    public static final class DatabaseOperationException extends RuntimeException {
        public DatabaseOperationException(String operation, Throwable cause) {
            super("Database operation failed: " + operation, cause);
        }
    }
}
