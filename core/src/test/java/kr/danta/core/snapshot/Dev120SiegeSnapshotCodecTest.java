package kr.danta.core.snapshot;

import kr.danta.core.siege.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class Dev120SiegeSnapshotCodecTest {

    @Test
    void schema22RoundTripPreservesSiegeRuntimeState() {
        UUID playerId = UUID.randomUUID();
        SiegeRuntimeSnapshot siege = new SiegeRuntimeSnapshot(
                List.of(new SiegeInstanceSnapshot("s1", "red_capital", "red", "blue", SiegePhase.ACTIVE, null)),
                List.of(new SiegeReservationSnapshot("s1", 1000L, 900L)),
                List.of(new SiegeProgressSnapshot("red_capital", SiegeEngagementProfile.CAPITAL_THREE_BATTLE,
                        SiegeStage.PLAZA_BATTLE, true)),
                List.of(new SiegeParticipantSnapshot("red_capital", playerId, SiegeSide.ATTACKER, true, "SURVIVAL")),
                List.of(new SiegeMoraleSnapshot("red_capital", SiegeSide.ATTACKER, -20)));

        GameSnapshot snapshot = new GameSnapshot(
                GameSnapshot.CURRENT_SCHEMA, 1L, 2L, false, 1.0,
                null, null, List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), siege);

        GameSnapshot restored = GameSnapshotCodec.decode(GameSnapshotCodec.encode(snapshot));

        assertEquals(22, GameSnapshot.CURRENT_SCHEMA);
        assertEquals(SiegePhase.ACTIVE, restored.siegeRuntime().instances().getFirst().phase());
        assertEquals(1000L, restored.siegeRuntime().reservations().getFirst().scheduledAtEpochMillis());
        assertEquals(SiegeStage.PLAZA_BATTLE, restored.siegeRuntime().progress().getFirst().stage());
        assertTrue(restored.siegeRuntime().progress().getFirst().resumeRequired());
        assertEquals(playerId, restored.siegeRuntime().participants().getFirst().playerId());
        assertEquals(-20, restored.siegeRuntime().morale().getFirst().moraleDelta());
    }

    @Test
    void schema21DecodeProvidesEmptySiegeRuntime() {
        String[] fields = new String[25];
        java.util.Arrays.fill(fields, "-");
        fields[0] = "21";
        fields[1] = "1";
        fields[2] = "2";
        fields[3] = "false";
        fields[4] = "1.0";
        fields[21] = "";

        GameSnapshot restored = GameSnapshotCodec.decode(String.join("|", fields));

        assertTrue(restored.siegeRuntime().isEmpty());
        assertEquals(GameSnapshot.CURRENT_SCHEMA, restored.schemaVersion());
    }
}
