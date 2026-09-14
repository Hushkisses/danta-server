package kr.danta.paper.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public final class DatabaseConfigLoader {
    private DatabaseConfigLoader() {}

    public static DatabaseConfig loadOrCreate(Path file) throws IOException {
        Path parent = file.getParent();
        if (parent != null) Files.createDirectories(parent);

        if (Files.notExists(file)) {
            Properties defaults = new Properties();
            defaults.setProperty("enabled", "false");
            defaults.setProperty("host", "127.0.0.1");
            defaults.setProperty("port", "5432");
            defaults.setProperty("database", "danta");
            defaults.setProperty("user", "danta");
            defaults.setProperty("password", "CHANGE_ME");
            defaults.setProperty("connectTimeoutSeconds", "5");
            try (OutputStream out = Files.newOutputStream(file,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                defaults.store(out, "Danta Server PostgreSQL settings (DEV-016)");
            }
        }

        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            p.load(in);
        }

        return new DatabaseConfig(
                Boolean.parseBoolean(p.getProperty("enabled", "false").trim()),
                p.getProperty("host", "127.0.0.1").trim(),
                parseInt(p, "port", 5432),
                p.getProperty("database", "danta").trim(),
                p.getProperty("user", "danta").trim(),
                p.getProperty("password", ""),
                parseInt(p, "connectTimeoutSeconds", 5)
        );
    }

    private static int parseInt(Properties p, String key, int defaultValue) {
        String value = p.getProperty(key);
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid integer for " + key + ": " + value, ex);
        }
    }
}
