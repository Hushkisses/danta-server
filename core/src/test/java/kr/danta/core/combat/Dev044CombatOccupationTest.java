package kr.danta.core.combat;

import kr.danta.core.event.DomainEventBus;
import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.*;
import kr.danta.core.territory.event.StrategicPointOwnershipChangedEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Dev044CombatOccupationTest {
    @Test void defeatedButSurvivingGarrisonPreventsOccupation() {
        Fixture f = fixture();
        CombatResolution resolution = resolution(1000, 1000);
        var result = f.service.occupyAfterVictory("market", "red", resolution, true);
        assertFalse(result.ownershipChanged());
        assertEquals("DEFENDER_REMAINS", result.reason());
        assertTrue(f.state.strategicPoint("market").orElseThrow().ownerNationId().isEmpty());
        assertTrue(f.state.hasGarrison("market"));
    }

    @Test void zeroRemainingDefenderTransfersOwnershipThroughTerritoryService() {
        Fixture f = fixture();
        List<StrategicPointOwnershipChangedEvent> events = new ArrayList<>();
        f.bus.subscribe(StrategicPointOwnershipChangedEvent.class, events::add);
        CombatResolution resolution = resolution(1000, 3);
        // Override the minimum-policy result with a fully defeated defender; DEV-044 consumes
        // combat outcome, it does not invent an annihilation rule.
        CombatResult power = resolution.powerResult();
        CombatResolution cleared = new CombatResolution(power, resolution.first(),
                new CombatLossResult("npc", CombatOutcome.DEFEAT, 3, 3, 0, true));

        var result = f.service.occupyAfterVictory("market", "red", cleared, true);
        assertTrue(result.ownershipChanged());
        assertEquals("red", f.state.strategicPoint("market").orElseThrow().ownerNationId().orElseThrow());
        assertFalse(f.state.hasGarrison("market"));
        assertEquals(1, events.size());
        assertEquals("combat-occupation", events.getFirst().reason());
    }

    @Test void drawCannotOccupy() {
        Fixture f = fixture();
        CombatResult power = new CombatResolver().resolve(
                CombatSideInput.neutral("red", TroopType.INFANTRY, 100),
                CombatSideInput.neutral("npc", TroopType.INFANTRY, 100));
        var result = f.service.occupyAfterVictory("market", "red", new CombatLossPolicy().apply(power), true);
        assertEquals("ATTACKER_DID_NOT_WIN", result.reason());
        assertFalse(result.ownershipChanged());
    }

    private static CombatResolution resolution(int attacker, int defender) {
        CombatResult power = new CombatResolver().resolve(
                CombatSideInput.neutral("red", TroopType.INFANTRY, attacker),
                CombatSideInput.neutral("npc", TroopType.SPEARMEN, defender));
        return new CombatLossPolicy().apply(power);
    }

    private static Fixture fixture() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "적국"));
        state.addStrategicPoint(new StrategicPoint("market", "시장", StrategicPointType.MAJOR,
                new PointPosition("world", 0, 70, 0), 2));
        state.addGarrison(GarrisonState.npc("market", 1000));
        DomainEventBus bus = new DomainEventBus();
        return new Fixture(state, bus, new CombatOccupationService(state, new TerritoryService(state, bus)));
    }

    private record Fixture(GameState state, DomainEventBus bus, CombatOccupationService service) {}
}
