package kr.danta.core.nation;

import kr.danta.core.economy.StrategicResource;
import kr.danta.core.state.GameState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev098IndependenceSupportServiceTest {
    @Test void thirdCountryCanFundVassalWithoutChangingVassalState() {
        var g = state();
        var v = new VassalService(g);
        v.onCapitalFallen("red", "blue", 100L);
        var service = new IndependenceSupportService(g, v);
        g.nation("green").orElseThrow().setTreasury(100L);
        g.nation("red").orElseThrow().setTreasury(10L);

        var result = service.transferGold("green", "red", 40L);

        assertEquals(60L, result.supporterTreasury());
        assertEquals(50L, result.recipientTreasury());
        assertTrue(v.relation("red").isPresent());
    }

    @Test void thirdCountryCanSendStrategicResources() {
        var g = state();
        var v = new VassalService(g);
        v.onCapitalFallen("red", "blue", 100L);
        var service = new IndependenceSupportService(g, v);
        g.getOrCreateStrategicResourceStockpile("green").set(StrategicResource.IRON, 25L);

        var result = service.transferResource("green", "red", StrategicResource.IRON, 7L);

        assertEquals(18L, result.supporterAmount());
        assertEquals(7L, result.recipientAmount());
    }

    @Test void overlordCannotUseIndependenceSupportChannel() {
        var g = state();
        var v = new VassalService(g);
        v.onCapitalFallen("red", "blue", 100L);
        var service = new IndependenceSupportService(g, v);
        g.nation("blue").orElseThrow().setTreasury(100L);
        var ex = assertThrows(IllegalStateException.class, () -> service.transferGold("blue", "red", 10L));
        assertEquals("overlord cannot provide independence support", ex.getMessage());
    }

    @Test void independentNationIsNotEligibleRecipient() {
        var g = state();
        var service = new IndependenceSupportService(g, new VassalService(g));
        var ex = assertThrows(IllegalStateException.class, () -> service.transferGold("green", "red", 10L));
        assertEquals("independence support recipient is not vassal", ex.getMessage());
    }

    @Test void insufficientSupportDoesNotPartiallyTransfer() {
        var g = state();
        var v = new VassalService(g);
        v.onCapitalFallen("red", "blue", 100L);
        var service = new IndependenceSupportService(g, v);
        g.nation("green").orElseThrow().setTreasury(5L);
        g.nation("red").orElseThrow().setTreasury(10L);

        assertThrows(IllegalStateException.class, () -> service.transferGold("green", "red", 6L));
        assertEquals(5L, g.nation("green").orElseThrow().treasury());
        assertEquals(10L, g.nation("red").orElseThrow().treasury());
    }

    private static GameState state() {
        var g = new GameState();
        var red = new NationState("red", "Red");
        red.setCapitalPointId("red_capital");
        var blue = new NationState("blue", "Blue");
        var green = new NationState("green", "Green");
        g.addNation(red); g.addNation(blue); g.addNation(green);
        g.addStrategicPoint(new kr.danta.core.territory.StrategicPoint(
                "red_capital", "Red Capital", kr.danta.core.territory.StrategicPointType.CAPITAL,
                new kr.danta.core.territory.PointPosition("world", 0, 70, 0), 3, java.util.Map.of(), "blue"));
        return g;
    }
}
