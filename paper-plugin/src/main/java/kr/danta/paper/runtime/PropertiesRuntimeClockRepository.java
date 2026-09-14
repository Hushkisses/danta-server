package kr.danta.paper.runtime;

import kr.danta.core.runtime.RuntimeClockRepository;
import kr.danta.core.runtime.RuntimeClockState;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.Properties;

/** Atomic small-file persistence used until PostgreSQL repositories arrive in DEV-016. */
public final class PropertiesRuntimeClockRepository implements RuntimeClockRepository {
    private final Path file;
    public PropertiesRuntimeClockRepository(Path file) { this.file = file; }

    @Override public Optional<RuntimeClockState> load() throws IOException {
        if (!Files.exists(file)) return Optional.empty();
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(file)) { p.load(in); }
        return Optional.of(new RuntimeClockState(
                Long.parseLong(p.getProperty("elapsedMillis", "0")),
                Boolean.parseBoolean(p.getProperty("paused", "false")),
                Double.parseDouble(p.getProperty("speedMultiplier", "1.0"))));
    }

    @Override public void save(RuntimeClockState state) throws IOException {
        Files.createDirectories(file.getParent());
        Properties p = new Properties();
        p.setProperty("elapsedMillis", Long.toString(state.elapsedMillis()));
        p.setProperty("paused", Boolean.toString(state.paused()));
        p.setProperty("speedMultiplier", Double.toString(state.speedMultiplier()));
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        try (OutputStream out = Files.newOutputStream(temp)) { p.store(out, "Danta runtime clock - DEV-011"); }
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException ex) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
