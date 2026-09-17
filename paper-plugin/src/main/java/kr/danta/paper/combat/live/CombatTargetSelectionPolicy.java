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
 * <p>Search radii are temporary live-combat fixtures, not final season balance values.</p>
 */
public final class CombatTargetSelectionPolicy {
    public static final double DEVELOPMENT_SEARCH_RADIUS = 28.0;
    public static final double DEVELOPMENT_FLANK_MATCHUP_RADIUS = 40.0;

    private final double searchRadius;
    private final double flankMatchupRadius;
    private final RandomGenerator random;

    public CombatTargetSelectionPolicy() {
        this(DEVELOPMENT_SEARCH_RADIUS, DEVELOPMENT_FLANK_MATCHUP_RADIUS, RandomGenerator.getDefault());
    }

    public CombatTargetSelectionPolicy(double searchRadius, RandomGenerator random) {
        this(searchRadius, searchRadius, random);
    }

    public CombatTargetSelectionPolicy(
            double searchRadius,
            double flankMatchupRadius,
            RandomGenerator random
    ) {
        if (!Double.isFinite(searchRadius) || searchRadius <= 0.0) {
            throw new IllegalArgumentException("searchRadius must be finite and > 0");
        }
        if (!Double.isFinite(flankMatchupRadius) || flankMatchupRadius <= 0.0) {
            throw new IllegalArgumentException("flankMatchupRadius must be finite and > 0");
        }
        this.searchRadius = searchRadius;
        this.flankMatchupRadius = flankMatchupRadius;
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

        List<Candidate> nonNullCandidates = candidates.stream()
                .filter(Objects::nonNull)
                .toList();

        if (attackerType == TroopType.ARCHERS) {
            List<Candidate> inRange = nonNullCandidates.stream()
                    .filter(candidate -> candidate.distance() <= attackRange)
                    .toList();
            if (!inRange.isEmpty()) {
                return Optional.of(inRange.get(random.nextInt(inRange.size())).unitId());
            }

            // Preserve random target choice while firing, but when nothing is yet in bow range,
            // acquire the nearest enemy inside the wider combat search radius so archers can
            // advance only until they can shoot instead of deadlocking at their rear position.
            return nonNullCandidates.stream()
                    .filter(candidate -> candidate.distance() <= searchRadius)
                    .min(Comparator.comparingDouble(Candidate::distance)
                            .thenComparing(candidate -> candidate.unitId().toString()))
                    .map(Candidate::unitId);
        }

        List<Candidate> eligible = nonNullCandidates.stream()
                .filter(candidate -> candidate.distance() <= detectionRadius(attackerType, candidate.troopType()))
                .toList();
        if (eligible.isEmpty()) return Optional.empty();

        ArrayList<Candidate> ordered = new ArrayList<>(eligible);
        ordered.sort(Comparator
                .comparingInt((Candidate candidate) -> priority(attackerType, candidate.troopType()))
                .thenComparingDouble(Candidate::distance)
                .thenComparing(candidate -> candidate.unitId().toString()));
        return Optional.of(ordered.getFirst().unitId());
    }

    private double detectionRadius(TroopType attackerType, TroopType targetType) {
        boolean cavalrySpearPair = (attackerType == TroopType.CAVALRY && targetType == TroopType.SPEARMEN)
                || (attackerType == TroopType.SPEARMEN && targetType == TroopType.CAVALRY);
        return cavalrySpearPair ? flankMatchupRadius : searchRadius;
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
