package kr.danta.paper.combat.live;

import kr.danta.core.combat.TroopType;
import kr.danta.core.combat.ai.CombatAiDecision;
import kr.danta.paper.combat.ai.PaperCombatAiRuntime;

import java.util.Objects;

/** DEV-115 connects DEV-114 tactical decisions to authored movement intent. */
public final class LiveCombatController {
    private final PaperCombatAiRuntime aiRuntime = new PaperCombatAiRuntime();
    private final LiveCombatObservationBuilder observationBuilder = new LiveCombatObservationBuilder();
    private final CombatMovementPlanner movementPlanner = new CombatMovementPlanner();

    public LiveCombatExecution decide(
            LiveCombatUnit unit,
            BattlefieldView battlefieldView,
            DemoBattlefieldLayout layout
    ) {
        Objects.requireNonNull(unit, "unit");
        Objects.requireNonNull(battlefieldView, "battlefieldView");
        Objects.requireNonNull(layout, "layout");

        CombatAiDecision decision = aiRuntime.decide(
                unit.troopType(),
                observationBuilder.build(battlefieldView));
        CombatMovementIntent movement = movementPlanner.plan(
                decision.action(),
                layout.routes(unit.side()));
        return new LiveCombatExecution(decision, movement, battlefieldView.selectedHostileUnitId());
    }

    /**
     * Paper-independent tactical facts for one live unit decision cycle.
     * Convenience fixtures exist only for deterministic DEV-115 tests/demo wiring.
     */
    public record BattlefieldView(
            double nearestHostileDistance,
            TroopType nearestHostileType,
            boolean frontlineSupportPresent,
            boolean backlineThreatened,
            boolean exposedEnemyBackline,
            boolean hostileRetreating,
            boolean spearScreenPresent,
            boolean survivalThreatened,
            boolean alliedCombatGroupPresent,
            java.util.UUID selectedHostileUnitId
    ) {
        public static BattlefieldView cavalryThreat() {
            return new BattlefieldView(6.0, TroopType.CAVALRY, false, false, false, false, false, false, false, null);
        }

        public static BattlefieldView closeThreat() {
            return new BattlefieldView(3.0, TroopType.INFANTRY, false, true, false, false, false, false, false, null);
        }

        public static BattlefieldView exposedBackline() {
            return new BattlefieldView(8.0, TroopType.ARCHERS, false, false, true, false, false, false, false, null);
        }

        public static BattlefieldView supportedRear() {
            return new BattlefieldView(8.0, TroopType.INFANTRY, false, false, false, false, false, false, true, null);
        }
    }
}
