package kr.danta.core.snapshot;

import kr.danta.core.general.GeneralGrade;
import kr.danta.core.general.GeneralHealthStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Dev076GeneralSnapshotCodecTest {
    @Test void v12RoundTripsOwnedGeneralRuntimeState() {
        GeneralSnapshot general = new GeneralSnapshot("g1","red",GeneralGrade.S,5,
                95,88,82,76, List.of("combined_arms"), List.of("supreme_command"),
                "army1", null, GeneralHealthStatus.INJURED, 100L, 200L,
                "blue", 120L, 500L);
        GameSnapshot original = new GameSnapshot(GameSnapshot.CURRENT_SCHEMA, 1L, 2L, false, 1.0,
                null,null,List.of(),List.of(),List.of(),List.of(),List.of(),List.of(),
                List.of(),List.of(),List.of(),List.of(general));
        GameSnapshot decoded = GameSnapshotCodec.decode(GameSnapshotCodec.encode(original));
        assertEquals(1, decoded.generals().size());
        assertEquals(general, decoded.generals().getFirst());
    }

    @Test void v11RemainsReadableWithNoGenerals() {
        String v11 = "11|1|2|false|1.0|-|-|-|-|-|-|-|-|-|-|-";
        GameSnapshot decoded = GameSnapshotCodec.decode(v11);
        assertEquals(GameSnapshot.CURRENT_SCHEMA, decoded.schemaVersion());
        assertTrue(decoded.generals().isEmpty());
    }
}
