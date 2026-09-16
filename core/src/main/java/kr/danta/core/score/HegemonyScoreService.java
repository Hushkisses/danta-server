package kr.danta.core.score;

import kr.danta.core.state.GameState;
import kr.danta.core.territory.StrategicPoint;
import kr.danta.core.territory.StrategicPointType;
import kr.danta.core.nation.VassalRelation;
import kr.danta.core.nation.VassalService;
import kr.danta.core.npc.NpcNationService;
import kr.danta.core.npc.NpcNationState;
import kr.danta.core.npc.NpcPoliticalStatus;

import java.util.Objects;

/**
 * DEV-103 current hegemony score.
 *
 * <p>Unlike accumulated fame, this is derived from the authoritative current world state and is
 * intentionally not persisted as a second mutable score. DEV-104 will supply the complete scoring
 * sources/weights. DEV-103 establishes the recomputable current-state contract only.</p>
 */
public final class HegemonyScoreService {
    private final GameState gameState;
    private final HegemonyScorePolicy policy;
    private final VassalService vassals;
    private final NpcNationService npcs;

    public HegemonyScoreService(GameState gameState, HegemonyScorePolicy policy) {
        this(gameState, policy, null, null);
    }

    public HegemonyScoreService(GameState gameState, HegemonyScorePolicy policy,
                                VassalService vassals, NpcNationService npcs) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
        this.policy = Objects.requireNonNull(policy, "policy");
        this.vassals = vassals;
        this.npcs = npcs;
    }

    public long score(String nationId) {
        requireNation(nationId);
        long total = 0L;
        for (StrategicPoint point : gameState.strategicPoints()) {
            if (!point.ownerNationId().filter(nationId::equals).isPresent()) continue;
            total = Math.addExact(total, policy.strategicPointValue(point.type()));
        }
        if (vassals != null) {
            for (VassalRelation relation : vassals.relations()) {
                if (relation.overlordNationId().equals(nationId)) {
                    total = Math.addExact(total, policy.vassalValue());
                }
            }
        }
        if (npcs != null) {
            for (NpcNationState npc : npcs.states()) {
                if (npc.politicalStatus() == NpcPoliticalStatus.SUBJUGATED
                        && nationId.equals(npc.patronNationId())) {
                    total = Math.addExact(total, policy.subjugatedNpcValue());
                }
            }
        }
        return total;
    }

    private void requireNation(String nationId) {
        if (nationId == null || nationId.isBlank() || !gameState.hasNation(nationId)) {
            throw new IllegalArgumentException("nation not found: " + nationId);
        }
    }

    public interface HegemonyScorePolicy {
        long strategicPointValue(StrategicPointType type);
        default long vassalValue() { return 0L; }
        default long subjugatedNpcValue() { return 0L; }
    }
}
