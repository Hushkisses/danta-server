package kr.danta.core.snapshot;

import kr.danta.core.army.ArmyStatus;
import kr.danta.core.combat.TroopType;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Dev119ArmyCompositionSnapshotTest {

    @Test
    void currentSchemaRoundTripPreservesTroopComposition() {
        ArmySnapshot army = new ArmySnapshot(
                "army_a", "red", "red_capital", ArmyStatus.STATIONED,
                170L, null, 0L,
                Map.of(
                        TroopType.INFANTRY, 100L,
                        TroopType.ARCHERS, 50L,
                        TroopType.CAVALRY, 20L));

        GameSnapshot snapshot = new GameSnapshot(
                GameSnapshot.CURRENT_SCHEMA, 1L, 2L, false, 1.0,
                null, null, List.of(), List.of(), List.of(), List.of(army));

        GameSnapshot decoded = GameSnapshotCodec.decode(GameSnapshotCodec.encode(snapshot));
        ArmySnapshot restored = decoded.armies().getFirst();

        assertEquals(22, GameSnapshot.CURRENT_SCHEMA);
        assertEquals(100L, restored.troopComposition().get(TroopType.INFANTRY));
        assertEquals(50L, restored.troopComposition().get(TroopType.ARCHERS));
        assertEquals(20L, restored.troopComposition().get(TroopType.CAVALRY));
        assertEquals(170L, restored.baseTroops());
    }

    @Test
    void schema20ArmyRowMigratesLegacyBaseTroopsToInfantry() {
        String armyRow = String.join(",",
                enc("army_a"), enc("red"), enc("red_capital"),
                ArmyStatus.STATIONED.name(), "250", "-", "0");
        String[] fields = new String[25];
        java.util.Arrays.fill(fields, "-");
        fields[0] = "20";
        fields[1] = "1";
        fields[2] = "2";
        fields[3] = "false";
        fields[4] = "1.0";
        fields[10] = armyRow;
        // v20 encodeVassalRelations(List.of()) serialized an empty list as an empty field, not "-".
        fields[21] = "";

        GameSnapshot decoded = GameSnapshotCodec.decode(String.join("|", fields));
        ArmySnapshot restored = decoded.armies().getFirst();

        assertEquals(250L, restored.troopComposition().get(TroopType.INFANTRY));
        assertEquals(0L, restored.troopComposition().get(TroopType.ARCHERS));
        assertEquals(250L, restored.baseTroops());
        assertEquals(GameSnapshot.CURRENT_SCHEMA, decoded.schemaVersion());
    }

    private static String enc(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
