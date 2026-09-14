package kr.danta.core.nation;

import java.util.Objects;
import java.util.Optional;

/**
 * Mutable in-memory nation aggregate introduced by DEV-020.
 * Treasury uses whole G units for now; fractional currency is intentionally unsupported.
 */
public final class NationState {
    private final String nationId;
    private String displayName;
    private String capitalPointId;
    private long treasury;
    private NationStatus status;

    public NationState(String nationId, String displayName) {
        this(nationId, displayName, null, 0L, NationStatus.ACTIVE);
    }

    public NationState(String nationId, String displayName, String capitalPointId,
                       long treasury, NationStatus status) {
        this.nationId = requireId(nationId);
        this.displayName = requireName(displayName);
        this.capitalPointId = normalizeOptionalId(capitalPointId);
        if (treasury < 0L) throw new IllegalArgumentException("treasury must be >= 0");
        this.treasury = treasury;
        this.status = Objects.requireNonNull(status, "status");
    }

    public String nationId() { return nationId; }
    public synchronized String displayName() { return displayName; }
    public synchronized Optional<String> capitalPointId() { return Optional.ofNullable(capitalPointId); }
    public synchronized long treasury() { return treasury; }
    public synchronized NationStatus status() { return status; }

    public synchronized void rename(String displayName) {
        this.displayName = requireName(displayName);
    }

    public synchronized void setCapitalPointId(String capitalPointId) {
        this.capitalPointId = normalizeOptionalId(capitalPointId);
    }

    public synchronized void setTreasury(long treasury) {
        if (treasury < 0L) throw new IllegalArgumentException("treasury must be >= 0");
        this.treasury = treasury;
    }

    public synchronized void deposit(long amount) {
        if (amount <= 0L) throw new IllegalArgumentException("amount must be > 0");
        this.treasury = Math.addExact(this.treasury, amount);
    }

    public synchronized boolean tryWithdraw(long amount) {
        if (amount <= 0L) throw new IllegalArgumentException("amount must be > 0");
        if (amount > treasury) return false;
        treasury -= amount;
        return true;
    }

    public synchronized void setStatus(NationStatus status) {
        this.status = Objects.requireNonNull(status, "status");
    }

    private static String requireId(String value) {
        Objects.requireNonNull(value, "nationId");
        String normalized = value.trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException("nationId must not be blank");
        if (!normalized.matches("[A-Za-z0-9_-]{1,32}")) {
            throw new IllegalArgumentException("nationId must match [A-Za-z0-9_-]{1,32}");
        }
        return normalized;
    }

    private static String requireName(String value) {
        Objects.requireNonNull(value, "displayName");
        String normalized = value.trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException("displayName must not be blank");
        if (normalized.length() > 48) throw new IllegalArgumentException("displayName must be <= 48 chars");
        return normalized;
    }

    private static String normalizeOptionalId(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
