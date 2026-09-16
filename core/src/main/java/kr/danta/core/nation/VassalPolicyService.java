package kr.danta.core.nation;

import kr.danta.core.state.GameState;
import java.util.Objects;

/**
 * DEV-096 tribute and subordination restrictions.
 * Tribute rate is deliberately configurable/provisional; v0.3 only fixes the 10-25% operating range.
 */
public final class VassalPolicyService {
    public static final int MIN_TRIBUTE_PERCENT = 10;
    public static final int MAX_TRIBUTE_PERCENT = 25;
    public static final int DEFAULT_PROVISIONAL_TRIBUTE_PERCENT = 15;

    private final GameState gameState;
    private final VassalService vassals;
    private int tributePercent;

    public VassalPolicyService(GameState gameState, VassalService vassals) {
        this(gameState, vassals, DEFAULT_PROVISIONAL_TRIBUTE_PERCENT);
    }

    public VassalPolicyService(GameState gameState, VassalService vassals, int tributePercent) {
        this.gameState = Objects.requireNonNull(gameState);
        this.vassals = Objects.requireNonNull(vassals);
        setTributePercent(tributePercent);
    }

    public synchronized int tributePercent() { return tributePercent; }

    public synchronized void setTributePercent(int percent) {
        if (percent < MIN_TRIBUTE_PERCENT || percent > MAX_TRIBUTE_PERCENT)
            throw new IllegalArgumentException("tribute percent must be between 10 and 25");
        tributePercent = percent;
    }

    /** Transfers tribute from newly-created treasury revenue only; personal wallets are never involved. */
    public synchronized long transferGoldRevenueTribute(String vassalNationId, long grossGoldRevenue) {
        if (grossGoldRevenue < 0) throw new IllegalArgumentException("grossGoldRevenue must be >= 0");
        VassalRelation relation = vassals.relation(vassalNationId).orElse(null);
        if (relation == null || grossGoldRevenue == 0) return 0L;
        long tribute = Math.multiplyExact(grossGoldRevenue, tributePercent) / 100L;
        if (tribute <= 0) return 0L;
        NationState vassal = requireNation(vassalNationId);
        NationState overlord = requireNation(relation.overlordNationId());
        if (!vassal.tryWithdraw(tribute)) throw new IllegalStateException("vassal treasury revenue is insufficient for tribute");
        overlord.deposit(tribute);
        return tribute;
    }

    public boolean canFormAlliance(String nationId) { return vassals.relation(nationId).isEmpty(); }
    public boolean canSupportJoin(String nationId) { return vassals.relation(nationId).isEmpty(); }

    public boolean canDeclareWar(String attackerNationId, String defenderNationId) {
        VassalRelation relation = vassals.relation(attackerNationId).orElse(null);
        return relation == null || !relation.overlordNationId().equals(defenderNationId);
    }

    /** v0.3: overlord has basic military passage through vassal territory; supply remains separate. */
    public boolean hasOverlordPassage(String guestNationId, String hostNationId) {
        return vassals.relation(hostNationId).map(r -> r.overlordNationId().equals(guestNationId)).orElse(false);
    }

    private NationState requireNation(String id) {
        return gameState.nation(id).orElseThrow(() -> new IllegalArgumentException("nation does not exist: " + id));
    }
}
