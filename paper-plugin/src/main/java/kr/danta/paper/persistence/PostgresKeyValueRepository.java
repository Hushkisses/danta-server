package kr.danta.paper.persistence;

import kr.danta.core.persistence.AsyncKeyValueRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Minimal repository proving DEV-016 asynchronous persistence infrastructure.
 * It is intentionally generic; Nation/Army/etc. receive dedicated repositories
 * when their domain tickets are implemented.
 */
public final class PostgresKeyValueRepository implements AsyncKeyValueRepository {
    private final PostgresDatabaseService database;

    public PostgresKeyValueRepository(PostgresDatabaseService database) {
        this.database = database;
    }

    @Override public CompletableFuture<Void> save(String namespace, String key, String value) {
        validate(namespace, key);
        if (value == null) throw new IllegalArgumentException("value is null");
        return database.submit("kv.save", connection -> {
            try (PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO danta_kv(namespace, state_key, state_value, updated_at)
                    VALUES (?, ?, ?, NOW())
                    ON CONFLICT (namespace, state_key) DO UPDATE
                    SET state_value = EXCLUDED.state_value, updated_at = NOW()
                    """)) {
                ps.setString(1, namespace);
                ps.setString(2, key);
                ps.setString(3, value);
                ps.executeUpdate();
            }
            return null;
        });
    }

    @Override public CompletableFuture<Optional<String>> find(String namespace, String key) {
        validate(namespace, key);
        return database.submit("kv.find", connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT state_value FROM danta_kv WHERE namespace = ? AND state_key = ?")) {
                ps.setString(1, namespace);
                ps.setString(2, key);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(rs.getString(1)) : Optional.empty();
                }
            }
        });
    }

    @Override public CompletableFuture<Boolean> delete(String namespace, String key) {
        validate(namespace, key);
        return database.submit("kv.delete", connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM danta_kv WHERE namespace = ? AND state_key = ?")) {
                ps.setString(1, namespace);
                ps.setString(2, key);
                return ps.executeUpdate() > 0;
            }
        });
    }

    private static void validate(String namespace, String key) {
        if (namespace == null || namespace.isBlank() || namespace.length() > 64) {
            throw new IllegalArgumentException("namespace must be 1..64 characters");
        }
        if (key == null || key.isBlank() || key.length() > 128) {
            throw new IllegalArgumentException("key must be 1..128 characters");
        }
    }
}
