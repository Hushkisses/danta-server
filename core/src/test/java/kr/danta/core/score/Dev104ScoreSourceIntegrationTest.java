package kr.danta.core.score;

import kr.danta.core.event.DomainEventBus;
import kr.danta.core.nation.*;
import kr.danta.core.npc.*;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.*;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Dev104ScoreSourceIntegrationTest {
    @Test
    void currentScoreRecomputesTerritoryVassalAndNpcSubjugationSources() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "적국", "red_cap", 0L, NationStatus.ACTIVE));
        state.addNation(new NationState("blue", "청국", "blue_cap", 0L, NationStatus.ACTIVE));
        state.addNation(new NationState("minor", "소국", null, 0L, NationStatus.ACTIVE));
        state.addStrategicPoint(point("red_cap", StrategicPointType.CAPITAL, "red"));
        state.addStrategicPoint(point("blue_cap", StrategicPointType.CAPITAL, "red"));
        state.addStrategicPoint(point("major", StrategicPointType.MAJOR, "red"));

        VassalService vassals = new VassalService(state);
        vassals.onCapitalFallen("blue", "red", 1L);

        NpcNationService npcs = new NpcNationService(state);
        npcs.register("minor");
        new NpcPoliticalService(state, npcs, new kr.danta.core.diplomacy.DiplomacyService(state),
                new TerritoryService(state, new DomainEventBus())).subjugate("minor", "red");

        HegemonyScoreService.HegemonyScorePolicy provisional = new HegemonyScoreService.HegemonyScorePolicy() {
            public long strategicPointValue(StrategicPointType type) { return type == StrategicPointType.MAJOR ? 3L : 1L; }
            public long vassalValue() { return 5L; }
            public long subjugatedNpcValue() { return 7L; }
        };
        HegemonyScoreService scores = new HegemonyScoreService(state, provisional, vassals, npcs);
        assertEquals(17L, scores.score("red"));

        vassals.release("blue");
        npcs.state("minor").orElseThrow().restoreIndependent();
        assertEquals(5L, scores.score("red"));
    }

    private static StrategicPoint point(String id, StrategicPointType type, String owner) {
        return new StrategicPoint(id, id, type, owner, new PointPosition("world", 0, 64, 0), 1, Map.of());
    }
}
