package kr.danta.core.nation;

import kr.danta.core.economy.StrategicResource;
import kr.danta.core.economy.StrategicResourceStockpile;
import kr.danta.core.state.GameState;

import java.util.Objects;

/**
 * DEV-098 third-country material support for a vassal preparing for independence.
 *
 * <p>This is deliberately material support, not military participation. A third country may transfer
 * treasury gold or strategic resources to a vassal before or during an independence war. Receiving
 * support never auto-declares independence and does not bypass DEV-097 declaration conditions.</p>
 */
public final class IndependenceSupportService {
    private final GameState gameState;
    private final VassalService vassals;

    public IndependenceSupportService(GameState gameState, VassalService vassals) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
        this.vassals = Objects.requireNonNull(vassals, "vassals");
    }

    public synchronized GoldSupportResult transferGold(String supporterNationId, String vassalNationId, long amount) {
        requireSupportPair(supporterNationId, vassalNationId);
        if (amount <= 0L) throw new IllegalArgumentException("support amount must be > 0");

        NationState supporter = requireNation(supporterNationId);
        NationState recipient = requireNation(vassalNationId);
        if (!supporter.tryWithdraw(amount)) throw new IllegalStateException("supporter treasury is insufficient");
        try {
            recipient.deposit(amount);
        } catch (RuntimeException ex) {
            supporter.deposit(amount);
            throw ex;
        }
        return new GoldSupportResult(supporterNationId, vassalNationId, amount,
                supporter.treasury(), recipient.treasury());
    }

    public synchronized ResourceSupportResult transferResource(String supporterNationId, String vassalNationId,
                                                               StrategicResource resource, long amount) {
        requireSupportPair(supporterNationId, vassalNationId);
        Objects.requireNonNull(resource, "resource");
        if (amount <= 0L) throw new IllegalArgumentException("support amount must be > 0");

        StrategicResourceStockpile supporter = gameState.getOrCreateStrategicResourceStockpile(supporterNationId);
        StrategicResourceStockpile recipient = gameState.getOrCreateStrategicResourceStockpile(vassalNationId);
        if (!supporter.tryConsume(resource, amount)) throw new IllegalStateException("supporter strategic resource is insufficient");
        try {
            recipient.deposit(resource, amount);
        } catch (RuntimeException ex) {
            supporter.deposit(resource, amount);
            throw ex;
        }
        return new ResourceSupportResult(supporterNationId, vassalNationId, resource, amount,
                supporter.amount(resource), recipient.amount(resource));
    }

    public VassalRelation requireEligibleRecipient(String supporterNationId, String vassalNationId) {
        requireNation(supporterNationId);
        requireNation(vassalNationId);
        if (supporterNationId.equals(vassalNationId)) throw new IllegalArgumentException("nation cannot support itself");
        VassalRelation relation = vassals.relation(vassalNationId)
                .orElseThrow(() -> new IllegalStateException("independence support recipient is not vassal"));
        if (relation.overlordNationId().equals(supporterNationId))
            throw new IllegalStateException("overlord cannot provide independence support");
        return relation;
    }

    private void requireSupportPair(String supporterNationId, String vassalNationId) {
        requireEligibleRecipient(supporterNationId, vassalNationId);
    }

    private NationState requireNation(String id) {
        return gameState.nation(id).orElseThrow(() -> new IllegalArgumentException("nation does not exist: " + id));
    }

    public record GoldSupportResult(String supporterNationId, String recipientNationId, long amount,
                                    long supporterTreasury, long recipientTreasury) {}

    public record ResourceSupportResult(String supporterNationId, String recipientNationId, StrategicResource resource,
                                        long amount, long supporterAmount, long recipientAmount) {}
}
