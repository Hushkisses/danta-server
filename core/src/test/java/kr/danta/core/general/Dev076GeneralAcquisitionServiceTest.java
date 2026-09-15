package kr.danta.core.general;

import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Dev076GeneralAcquisitionServiceTest {
    @Test void acquisitionCreatesOwnedStateFromCatalogDefinition() {
        GameState state = new GameState();
        state.addNation(new NationState("red","Red"));
        GeneralDefinition definition = new GeneralDefinition("elite","정예",GeneralGrade.A,4,
                new GeneralStats(80,70,60,50), List.of("trait"), List.of("ability"));
        GeneralState acquired = new GeneralAcquisitionService(state).acquire(definition,"red");
        assertEquals("red", acquired.ownerNationId());
        assertEquals(GeneralGrade.A, acquired.grade());
        assertEquals(80, acquired.stats().command());
        assertEquals(List.of("trait"), acquired.traits().stream().map(GeneralTrait::traitId).toList());
        assertEquals(List.of("ability"), acquired.abilities().stream().map(GeneralAbility::abilityId).toList());
    }

    @Test void sameUniqueGeneralCannotBeAcquiredTwice() {
        GameState state = new GameState();
        state.addNation(new NationState("red","Red"));
        state.addNation(new NationState("blue","Blue"));
        GeneralDefinition definition = new GeneralDefinition("elite","정예",GeneralGrade.S,5,
                GeneralStats.zero(), List.of(), List.of("ability"));
        GeneralAcquisitionService service = new GeneralAcquisitionService(state);
        service.acquire(definition,"red");
        assertThrows(IllegalStateException.class, () -> service.acquire(definition,"blue"));
    }
}
