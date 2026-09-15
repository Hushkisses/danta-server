package kr.danta.core.army;

import kr.danta.core.nation.NationState;
import kr.danta.core.snapshot.ArmySnapshot;
import kr.danta.core.snapshot.GameSnapshot;
import kr.danta.core.snapshot.GameSnapshotCodec;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.PointPosition;
import kr.danta.core.territory.StrategicPoint;
import kr.danta.core.territory.StrategicPointType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Dev030ArmyTest {
    @Test
    void gameStateAcceptsOnlyArmiesWithExistingOwnerAndLocation() {
        GameState state = gameStateWithNationAndPoint();
        ArmyState army = new ArmyState("red_first", "red", "capital_red", ArmyStatus.STATIONED, 1_200L);

        state.addArmy(army);

        assertEquals(army, state.army("red_first").orElseThrow());
        assertEquals(List.of(army), state.armies());
        assertThrows(IllegalArgumentException.class, () -> state.addArmy(
                new ArmyState("ghost_army", "ghost", "capital_red", ArmyStatus.STATIONED, 10L)));
        assertThrows(IllegalArgumentException.class, () -> state.addArmy(
                new ArmyState("lost_army", "red", "missing", ArmyStatus.STATIONED, 10L)));
        assertThrows(IllegalArgumentException.class, () -> state.addArmy(
                new ArmyState("red_first", "red", "capital_red", ArmyStatus.STATIONED, 10L)));
    }

    @Test
    void armyRejectsInvalidTroopCountsAndTracksDev030State() {
        ArmyState army = new ArmyState("red_first", "red", "capital_red", ArmyStatus.STATIONED, 1_200L);

        army.setBaseTroops(950L);
        army.setStatus(ArmyStatus.MOVING);
        army.setLocationPointId("farm_a");

        assertEquals(950L, army.baseTroops());
        assertEquals(ArmyStatus.MOVING, army.status());
        assertEquals("farm_a", army.locationPointId());
        assertThrows(IllegalArgumentException.class, () -> army.setBaseTroops(-1L));
    }

    @Test
    void snapshotV5RoundTripPreservesArmiesAndV4ReadsWithNoArmies() {
        ArmySnapshot army = new ArmySnapshot("red_first", "red", "capital_red", ArmyStatus.IN_BATTLE, 875L);
        GameSnapshot snapshot = new GameSnapshot(
                GameSnapshot.CURRENT_SCHEMA, 100L, 200L, false, 1.0,
                null, null, List.of(), List.of(), List.of(), List.of(army));

        GameSnapshot decoded = GameSnapshotCodec.decode(GameSnapshotCodec.encode(snapshot));

        assertEquals(List.of(army), decoded.armies());
        String schemaV4 = "4|100|200|false|1.0|-|-|-|-|-";
        assertEquals(List.of(), GameSnapshotCodec.decode(schemaV4).armies());
    }

    private static GameState gameStateWithNationAndPoint() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "Red Nation"));
        state.addStrategicPoint(new StrategicPoint(
                "capital_red", "Red Capital", StrategicPointType.CAPITAL,
                new PointPosition("world", 0, 64, 0), 4));
        return state;
    }
}
