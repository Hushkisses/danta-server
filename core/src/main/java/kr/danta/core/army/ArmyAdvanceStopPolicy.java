package kr.danta.core.army;

import kr.danta.core.state.GameState;
import kr.danta.core.territory.StrategicPointType;

public final class ArmyAdvanceStopPolicy {
    private final GameState gameState;

    public ArmyAdvanceStopPolicy(GameState gameState) {
        this.gameState = gameState;
    }

    public ArmyAdvanceDecision evaluateAfterArrival(String armyId) {
        ArmyState army = gameState.army(armyId).orElseThrow();
        StrategicPointType type = gameState.strategicPoint(army.locationPointId()).orElseThrow().type();
        if (type == StrategicPointType.MAJOR || type == StrategicPointType.CAPITAL) {
            return ArmyAdvanceDecision.stop(ArmyAdvanceStopReason.MAJOR_POINT, army.locationPointId());
        }
        return ArmyAdvanceDecision.continueAdvance();
    }

    public ArmyAdvanceDecision combatStop() {
        return ArmyAdvanceDecision.stop(ArmyAdvanceStopReason.COMBAT, "");
    }

    public ArmyAdvanceDecision supplyStop() {
        return ArmyAdvanceDecision.stop(ArmyAdvanceStopReason.SUPPLY, "");
    }
}
