package kr.danta.core.snapshot;

import kr.danta.core.facility.FacilityTier;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class Dev081FacilitySnapshotCodecTest {
    @Test void schema13RoundTripsInstalledAndPendingFacilities() {
        UUID id = UUID.randomUUID();
        GameSnapshot snapshot = new GameSnapshot(GameSnapshot.CURRENT_SCHEMA, 1L, 2000L, false, 1.0,
                null, null, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(),
                List.of(new FacilitySnapshot("p1", "warehouse", FacilityTier.II)),
                List.of(new FacilityConstructionSnapshot(id, "p1", "warehouse", FacilityTier.III, 9000L)));
        GameSnapshot decoded = GameSnapshotCodec.decode(GameSnapshotCodec.encode(snapshot));
        assertEquals(FacilityTier.II, decoded.facilities().getFirst().tier());
        assertEquals(id, decoded.facilityConstructions().getFirst().constructionId());
        assertEquals(9000L, decoded.facilityConstructions().getFirst().dueRuntimeMillis());
    }

    @Test void legacySchema12StillReadsWithEmptyFacilityState() {
        GameSnapshot v12shape = new GameSnapshot(12, 1L, 2L, false, 1.0, null, null,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
        String encoded = GameSnapshotCodec.encode(v12shape);
        // Encoder writes the supplied schema field, so trim the two v13-only trailing fields to emulate a real v12 payload.
        String[] fields = encoded.split("\\|", -1);
        String legacy = String.join("|", java.util.Arrays.copyOf(fields, 17));
        GameSnapshot decoded = GameSnapshotCodec.decode(legacy);
        assertTrue(decoded.facilities().isEmpty());
        assertTrue(decoded.facilityConstructions().isEmpty());
    }
}
