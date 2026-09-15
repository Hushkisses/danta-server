package kr.danta.core.economy;

import kr.danta.core.army.ArmyState;
import kr.danta.core.army.ArmyStatus;
import kr.danta.core.state.GameState;

import java.util.Objects;

/**
 * DEV-055 minimal army food/supply consumption.
 * Balance numbers are provisional because design v0.3 explicitly leaves exact army food formulas open.
 */
public final class ArmySupplyService {
    public static final long FOOD_PER_1000_STATIONED_PER_TICK = 1L;
    public static final long FOOD_PER_1000_MOVING_PER_TICK = 20L;
    public static final long FOOD_PER_1000_BATTLE_PER_TICK = 40L;

    private final GameState gameState;
    public ArmySupplyService(GameState gameState) { this.gameState = Objects.requireNonNull(gameState); }

    public TickResult consumeOneTick() {
        long requested = 0, consumed = 0;
        int supplied = 0, shortfall = 0;
        for (ArmyState army : gameState.armies()) {
            long need = foodNeed(army);
            if (need <= 0) continue;
            requested = Math.addExact(requested, need);
            StrategicResourceStockpile stock = gameState.getOrCreateStrategicResourceStockpile(army.ownerNationId());
            long actual = Math.min(stock.amount(StrategicResource.FOOD), need);
            if (actual > 0) stock.tryConsume(StrategicResource.FOOD, actual);
            consumed = Math.addExact(consumed, actual);
            if (actual == need) supplied++; else shortfall++;
        }
        return new TickResult(requested, consumed, supplied, shortfall);
    }

    public long foodNeed(ArmyState army) {
        Objects.requireNonNull(army);
        if (army.baseTroops() <= 0) return 0;
        long units = Math.max(1L, (army.baseTroops() + 999L) / 1000L);
        long rate = switch (army.status()) {
            case STATIONED -> FOOD_PER_1000_STATIONED_PER_TICK;
            case MOVING -> FOOD_PER_1000_MOVING_PER_TICK;
            case IN_BATTLE -> FOOD_PER_1000_BATTLE_PER_TICK;
        };
        return Math.multiplyExact(units, rate);
    }

    public record TickResult(long requestedFood, long consumedFood, int fullySuppliedArmies, int shortfallArmies) {}
}
