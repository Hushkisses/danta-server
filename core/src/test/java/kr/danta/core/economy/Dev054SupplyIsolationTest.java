package kr.danta.core.economy;

import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.*;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class Dev054SupplyIsolationTest {
    @Test void isolatedProductionStaysLocalAndBecomesAvailableWhenRouteReconnects() {
        GameState state = new GameState();
        NationState red = new NationState("red", "Red");
        red.setCapitalPointId("capital");
        state.addNation(red);
        state.addStrategicPoint(new StrategicPoint("capital","Capital",StrategicPointType.CAPITAL,"red",new PointPosition("world",0,64,0),1,Map.of()));
        state.addStrategicPoint(new StrategicPoint("bridge","Bridge",StrategicPointType.GATE,null,new PointPosition("world",1,64,0),1,Map.of()));
        state.addStrategicPoint(new StrategicPoint("forest","Forest",StrategicPointType.FORESTRY,"red",new PointPosition("world",2,64,0),1,Map.of("wood",160L)));
        state.addStrategicEdge(new StrategicEdge("e1","capital","bridge",1000, Set.of()));
        state.addStrategicEdge(new StrategicEdge("e2","bridge","forest",1000, Set.of()));

        StrategicPointProductionService production = new StrategicPointProductionService(state);
        SupplyConnectivityService supply = new SupplyConnectivityService(state);
        production.produceOneTick();

        assertEquals(0, state.getOrCreateStrategicResourceStockpile("red").amount(StrategicResource.WOOD));
        assertEquals(80, state.getOrCreateLocalResourceStockpile("forest").amount(StrategicResource.WOOD));
        assertEquals(80, supply.totalAmount("red", StrategicResource.WOOD));
        assertEquals(0, supply.availableAmount("red", StrategicResource.WOOD));

        state.strategicPoint("bridge").orElseThrow().setOwnerNationId("red");
        assertTrue(supply.isConnectedToCapital("red", "forest"));
        assertEquals(80, supply.availableAmount("red", StrategicResource.WOOD));
    }
}
