package kr.danta.paper.siege;

import kr.danta.core.snapshot.*;
import kr.danta.core.siege.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class Dev120PaperSiegeRecoveryTest {

    @Test
    void progressRestoresExactStageAndMarksResumeRequired() {
        PaperSiegeProgressRuntime runtime = new PaperSiegeProgressRuntime();
        runtime.restore(new SiegeProgressSnapshot(
                "red_capital",
                SiegeEngagementProfile.CAPITAL_THREE_BATTLE,
                SiegeStage.PLAZA_BATTLE,
                true));

        assertTrue(runtime.active("red_capital"));
        assertTrue(runtime.status("red_capital").contains("광장 전투"));
        assertTrue(runtime.resumeRequired("red_capital"));
    }

    @Test
    void participantRegistryRestoresEliminationAndMorale() {
        SiegeParticipantRegistry registry =
                new SiegeParticipantRegistry(SiegeCommanderCasualtyPolicy.developmentDefaults());
        UUID playerId = UUID.randomUUID();

        registry.restoreParticipant("red_capital", playerId, SiegeSide.ATTACKER, true);
        registry.restoreMorale("red_capital", SiegeSide.ATTACKER, -20);

        assertTrue(registry.eliminated("red_capital", playerId));
        assertEquals(SiegeSide.ATTACKER, registry.side("red_capital", playerId));
        assertEquals(-20, registry.moraleDelta("red_capital", SiegeSide.ATTACKER));
        assertThrows(IllegalStateException.class,
                () -> registry.join("red_capital", playerId, SiegeSide.ATTACKER));
    }

    @Test
    void runtimeStateRoundTripPreservesPaperRecoveryData() {
        UUID playerId = UUID.randomUUID();
        SiegeRuntimeSnapshot state = new SiegeRuntimeSnapshot(
                List.of(), List.of(),
                List.of(new SiegeProgressSnapshot(
                        "red_capital",
                        SiegeEngagementProfile.CAPITAL_THREE_BATTLE,
                        SiegeStage.CORE_BATTLE,
                        true)),
                List.of(new SiegeParticipantSnapshot(
                        "red_capital", playerId, SiegeSide.DEFENDER, true, "SURVIVAL")),
                List.of(new SiegeMoraleSnapshot("red_capital", SiegeSide.DEFENDER, -20)));

        PaperSiegeProgressRuntime progress = new PaperSiegeProgressRuntime();
        SiegeParticipantRegistry participants =
                new SiegeParticipantRegistry(SiegeCommanderCasualtyPolicy.developmentDefaults());

        progress.restore(state.progress().getFirst());
        participants.restoreParticipant("red_capital", playerId, SiegeSide.DEFENDER, true);
        participants.restoreMorale("red_capital", SiegeSide.DEFENDER, -20);

        assertTrue(progress.resumeRequired("red_capital"));
        assertTrue(participants.eliminated("red_capital", playerId));
        assertEquals(-20, participants.moraleDelta("red_capital", SiegeSide.DEFENDER));
    }
}
