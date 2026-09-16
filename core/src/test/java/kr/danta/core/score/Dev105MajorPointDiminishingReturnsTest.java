package kr.danta.core.score;

import kr.danta.core.nation.NationState;
import kr.danta.core.nation.NationStatus;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.PointPosition;
import kr.danta.core.territory.StrategicPoint;
import kr.danta.core.territory.StrategicPointType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Dev105MajorPointDiminishingReturnsTest {
    @Test
    void sameKindMajorPointsUse100Then75Then50Percent() {
        GameState state = stateWithNation();
        state.addStrategicPoint(point("m1", "red"));
        state.addStrategicPoint(point("m2", "red"));
        state.addStrategicPoint(point("m3", "red"));

        HegemonyScoreService scores = new HegemonyScoreService(state, type -> type == StrategicPointType.MAJOR ? 100L : 0L);

        assertEquals(225L, scores.score("red"));
    }

    @Test
    void documentedThirdStepActsAsFloorUntilMoreBalanceIsDefined() {
        GameState state = stateWithNation();
        state.addStrategicPoint(point("m1", "red"));
        state.addStrategicPoint(point("m2", "red"));
        state.addStrategicPoint(point("m3", "red"));
        state.addStrategicPoint(point("m4", "red"));

        HegemonyScoreService scores = new HegemonyScoreService(state, type -> type == StrategicPointType.MAJOR ? 100L : 0L);

        assertEquals(275L, scores.score("red"));
    }

    @Test
    void policyCanSeparateFutureMajorKindsWithoutChangingAggregator() {
        GameState state = stateWithNation();
        state.addStrategicPoint(point("fort_a", "red"));
        state.addStrategicPoint(point("market_a", "red"));
        HegemonyScoreService.HegemonyScorePolicy policy = new HegemonyScoreService.HegemonyScorePolicy() {
            public long strategicPointValue(StrategicPointType type) { return type == StrategicPointType.MAJOR ? 100L : 0L; }
            public String majorPointKind(StrategicPoint point) { return point.pointId().startsWith("fort") ? "FORT" : "MARKET"; }
        };

        assertEquals(200L, new HegemonyScoreService(state, policy).score("red"));
    }

    private static GameState stateWithNation() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "적국", null, 0L, NationStatus.ACTIVE));
        return state;
    }

    private static StrategicPoint point(String id, String owner) {
        return new StrategicPoint(id, id, StrategicPointType.MAJOR, owner,
                new PointPosition("world", 0, 64, 0), 1, Map.of());
    }
}
