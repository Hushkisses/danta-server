package kr.danta.core.snapshot;

import kr.danta.core.diplomacy.DiplomaticStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Dev090DiplomacySnapshotTest {
    @Test
    void v16RoundTripPreservesDiplomacy() {
        GameSnapshot current = new GameSnapshot(
                GameSnapshot.CURRENT_SCHEMA, 1, 2, false, 1.0, null, null,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(new DiplomaticRelationSnapshot("blue", "red", DiplomaticStatus.ALLIANCE)),
                List.of(), List.of());

        GameSnapshot decoded = GameSnapshotCodec.decode(GameSnapshotCodec.encode(current));

        assertEquals(1, decoded.diplomaticRelations().size());
        assertEquals(DiplomaticStatus.ALLIANCE, decoded.diplomaticRelations().getFirst().status());
    }

    @Test
    void oldV15StillDecodesWithEmptyDiplomacy() {
        GameSnapshot v15 = new GameSnapshot(
                15, 1, 2, false, 1.0, null, null,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of());

        String currentShape = GameSnapshotCodec.encode(v15);
        int lastSeparator = currentShape.lastIndexOf('|');
        String withoutVassals = currentShape.substring(0, lastSeparator);
        String legacyV15 = withoutVassals.substring(0, withoutVassals.lastIndexOf('|'));

        GameSnapshot decoded = GameSnapshotCodec.decode(legacyV15);

        assertTrue(decoded.diplomaticRelations().isEmpty());
        assertTrue(decoded.vassalRelations().isEmpty());
    }
}
