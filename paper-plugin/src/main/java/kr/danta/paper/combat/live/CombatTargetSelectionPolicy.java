package kr.danta.paper.combat.live;

import kr.danta.core.combat.TroopType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.random.RandomGenerator;

/**
 * DEV-115 development target-selection policy.
 *
 * <p>The search radius is a temporary live-combat fixture, not a final season balance value.</p>
 */
public final class CombatTargetSelectionPolicy {
    public static final double DEVELOPMENT_SEARCH_RADIUS = 28.0;

    private final double searchRadius;
    private final RandomGenerator random;

    public CombatTargetSelectionPolicy() {
        this(DEVELOPMENT_SEARCH_RADIUS, RandomGenerator.getDefault());
    }

    public CombatTargetSelectionPolicy(double searchRadius, RandomGenerator random) {
        if (!Double.isFinite(searchRadius) || searchRadius <= 0.0) {
            throw new IllegalArgumentException("searchRadius must be finite and > 0");
        }
        this.searchRadius = searchRadius;
        this.random = Objects.requireNonNull(random, "random");
    }

    public Optional<UUID> select(
            TroopType attackerType,
            Collection<Candidate> candidates,
            double attackRange
    ) {
        Objects.requireNonNull(attackerType, "attackerType");
        Objects.requireNonNull(candidates, "candidates");
        if (!Double.isFinite(attackRange) || attackRange < 0.0) {
            throw new IllegalArgumentException("attackRange must be finite and >= 0");
        }

        double limit = attackerType == TroopType.ARCHERS ? attackRange : searchRadius;
        List<Candidate> eligible = candidates.stream()
                .filter(Objects::nonNull)
                .filter(candidate -> candidate.distance() <= limit)
                .toList();
        if (eligible.isEmpty()) return Optional.empty();

        if (attackerType == TroopType.ARCHERS) {
            return Optional.of(eligible.get(random.nextInt(eligible.size())).unitId());
        }

        ArrayList<Candidate> ordered = new ArrayList<>(eligible);
        ordered.sort(Comparator
                .comparingInt((Candidate candidate) -> priority(attackerType, candidate.troopType()))
                .thenComparingDouble(Candidate::distance)
                .thenComparing(candidate -> candidate.unitId().toString()));
        return Optional.of(ordered.getFirst().unitId());
    }

    private static int priority(TroopType attackerType, TroopType targetType) {
        return switch (attackerType) {
            case CAVALRY -> switch (targetType) {
                case SPEARMEN -> 0;
                case ARCHERS -> 1;
                case MAGIC -> 2;
                case INFANTRY -> 3;
                case CAVALRY -> 4;
            };
            case INFANTRY -> switch (targetType) {
                case INFANTRY, SPEARMEN -> 0;
                default -> 1;
            };
            case SPEARMEN -> targetType == TroopType.CAVALRY ? 0 : 1;
            case ARCHERS, MAGIC -> 0;
        };
    }

    public record Candidate(UUID unitId, TroopType troopType, double distance) {
        public Candidate {
            Objects.requireNonNull(unitId, "unitId");
            Objects.requireNonNull(troopType, "troopType");
            if (!Double.isFinite(distance) || distance < 0.0) {
                throw new IllegalArgumentException("distance must be finite and >= 0");
            }
        }
    }
}
