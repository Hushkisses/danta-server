package kr.danta.core.snapshot;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Dev102FameScoreSnapshotTest {
    @Test
    void currentSchemaRoundTripPreservesFameScores() {
        GameSnapshot snapshot = new GameSnapshot(
                GameSnapshot.CURRENT_SCHEMA, 1L, 2L, false, 1.0, null, null,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(
                        new FameScoreSnapshot("red", 120L),
                        new FameScoreSnapshot("blue", 45L)));

        GameSnapshot decoded = GameSnapshotCodec.decode(GameSnapshotCodec.encode(snapshot));

        assertEquals(GameSnapshot.CURRENT_SCHEMA, decoded.schemaVersion());
        assertEquals(List.of(new FameScoreSnapshot("red", 120L), new FameScoreSnapshot("blue", 45L)),
                decoded.fameScores());
    }

    @Test
    void schema18DecodeDefaultsFameToEmptyForBackwardCompatibility() {
        GameSnapshot current = new GameSnapshot(
                GameSnapshot.CURRENT_SCHEMA, 1L, 2L, false, 1.0, null, null,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(new FameScoreSnapshot("red", 9L)));
        String[] fields = GameSnapshotCodec.encode(current).split("\\|", -1);
        fields[0] = "18";
        // v18 predates both fame (v19) and chronicle (v20).
        String schema18 = String.join("|", java.util.Arrays.copyOf(fields, 23));

        GameSnapshot decoded = GameSnapshotCodec.decode(schema18);

        assertTrue(decoded.fameScores().isEmpty());
    }
}
