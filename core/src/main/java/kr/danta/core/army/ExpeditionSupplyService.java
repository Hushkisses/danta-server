package kr.danta.core.army;

import kr.danta.core.economy.StrategicResource;
import kr.danta.core.economy.StrategicResourceStockpile;
import kr.danta.core.state.GameState;

import java.util.Objects;

/** DEV-056 loads expedition food from the owning nation's strategic stockpile. */
public final class ExpeditionSupplyService {
    private final GameState gameState;

    public ExpeditionSupplyService(GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState);
    }

    public LoadResult load(String armyId, ExpeditionSupplyLevel level) {
        ArmyState army = gameState.army(armyId)
                .orElseThrow(() -> new IllegalArgumentException("army not found: " + armyId));
        Objects.requireNonNull(level, "level");
        if (army.status() != ArmyStatus.STATIONED) {
            throw new IllegalStateException("army must be stationed before loading expedition supply");
        }
        StrategicResourceStockpile stock = gameState.getOrCreateStrategicResourceStockpile(army.ownerNationId());
        long amount = level.foodAmount();
        if (!stock.tryConsume(StrategicResource.FOOD, amount)) {
            throw new IllegalStateException("not enough food for expedition supply");
        }
        army.setExpeditionSupply(level, amount);
        return new LoadResult(level, amount, stock.amount(StrategicResource.FOOD));
    }

    public record LoadResult(ExpeditionSupplyLevel level, long loadedFood, long nationFoodRemaining) {}
}
