package kr.danta.core.runtime;

import java.io.IOException;
import java.util.Optional;

public interface RuntimeClockRepository {
    Optional<RuntimeClockState> load() throws IOException;
    void save(RuntimeClockState state) throws IOException;
}
