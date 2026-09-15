package kr.danta.core.combat;

import java.util.Optional;

public record CombatResult(CombatSideResult first, CombatSideResult second, String winnerSideId) {
    public boolean draw() { return winnerSideId == null; }
    public Optional<String> winner() { return Optional.ofNullable(winnerSideId); }
}
