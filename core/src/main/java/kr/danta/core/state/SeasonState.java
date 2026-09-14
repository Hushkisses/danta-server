package kr.danta.core.state;

import java.util.Objects;

public record SeasonState(String seasonId, String displayName) {
    public SeasonState {
        Objects.requireNonNull(seasonId, "seasonId");
        Objects.requireNonNull(displayName, "displayName");
        if (seasonId.isBlank()) throw new IllegalArgumentException("seasonId must not be blank");
    }
}
