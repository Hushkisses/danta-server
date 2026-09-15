package kr.danta.core.snapshot;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Dev050WalletSnapshotCodecTest {
    @Test void schemaEightRoundTripsWallets() {
        GameSnapshot snapshot = new GameSnapshot(GameSnapshot.CURRENT_SCHEMA, 1, 2, false, 1.0,
                null, null, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(new PersonalWalletSnapshot("player-1", 700)));
        GameSnapshot decoded = GameSnapshotCodec.decode(GameSnapshotCodec.encode(snapshot));
        assertEquals(GameSnapshot.CURRENT_SCHEMA, decoded.schemaVersion());
        assertEquals(1, decoded.personalWallets().size());
        assertEquals("player-1", decoded.personalWallets().get(0).playerId());
        assertEquals(700, decoded.personalWallets().get(0).balance());
    }

}
