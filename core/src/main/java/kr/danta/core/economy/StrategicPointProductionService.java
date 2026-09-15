package kr.danta.core.economy;

import kr.danta.core.state.GameState;
import kr.danta.core.territory.StrategicPoint;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** DEV-053 applies v0.3 point base production on each 30-minute EconomyTick. */
public final class StrategicPointProductionService {
    private final GameState gameState;

    public StrategicPointProductionService(GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
    }

    public ProductionResult produceOneTick() {
        Map<String, Map<StrategicResource, Long>> resources = new LinkedHashMap<>();
        Map<String, Long> gold = new LinkedHashMap<>();
        for (StrategicPoint point : gameState.strategicPoints()) {
            String nationId = point.ownerNationId().orElse(null);
            if (nationId == null) continue;
            for (Map.Entry<String, Long> entry : point.baseProductionPerHour().entrySet()) {
                long perTick = entry.getValue() / 2L;
                if (perTick <= 0L) continue;
                String key = entry.getKey().toLowerCase();
                if (key.equals("gold") || key.equals("g")) {
                    gameState.nation(nationId).ifPresent(n -> n.deposit(perTick));
                    gold.merge(nationId, perTick, Math::addExact);
                    continue;
                }
                StrategicResource resource = parseResource(key);
                if (resource != null) {
                    gameState.getOrCreateStrategicResourceStockpile(nationId).deposit(resource, perTick);
                    resources.computeIfAbsent(nationId, ignored -> new LinkedHashMap<>())
                            .merge(resource, perTick, Math::addExact);
                }
            }
        }
        return new ProductionResult(resources, gold);
    }

    private static StrategicResource parseResource(String key) {
        return switch (key) {
            case "food" -> StrategicResource.FOOD;
            case "wood" -> StrategicResource.WOOD;
            case "iron" -> StrategicResource.IRON;
            case "rare_mineral" -> StrategicResource.RARE_MINERAL;
            case "mana_stone" -> StrategicResource.MANA_STONE;
            default -> null;
        };
    }

    public record ProductionResult(Map<String, Map<StrategicResource, Long>> resourcesByNation,
                                   Map<String, Long> goldByNation) {}
}
