package kr.danta.core.economy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Dev051StrategicResourceTest {
    @Test void fixedFiveResourcesExist() {
        assertArrayEquals(new StrategicResource[]{StrategicResource.FOOD, StrategicResource.WOOD,
                StrategicResource.IRON, StrategicResource.RARE_MINERAL, StrategicResource.MANA_STONE},
                StrategicResource.values());
    }
    @Test void stockpileDepositsAndConsumesWithoutGoingNegative() {
        StrategicResourceStockpile stock = new StrategicResourceStockpile("red");
        stock.deposit(StrategicResource.FOOD, 100);
        assertTrue(stock.tryConsume(StrategicResource.FOOD, 40));
        assertEquals(60, stock.amount(StrategicResource.FOOD));
        assertFalse(stock.tryConsume(StrategicResource.FOOD, 61));
        assertEquals(60, stock.amount(StrategicResource.FOOD));
    }
}
