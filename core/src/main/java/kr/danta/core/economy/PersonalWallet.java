package kr.danta.core.economy;

import java.util.Objects;

/** DEV-050 personal currency wallet, intentionally separate from NationState treasury. */
public final class PersonalWallet {
    private final String playerId;
    private long balance;

    public PersonalWallet(String playerId, long balance) {
        this.playerId = requireId(playerId);
        if (balance < 0L) throw new IllegalArgumentException("balance must be >= 0");
        this.balance = balance;
    }

    public String playerId() { return playerId; }
    public synchronized long balance() { return balance; }

    public synchronized void deposit(long amount) {
        if (amount <= 0L) throw new IllegalArgumentException("amount must be > 0");
        balance = Math.addExact(balance, amount);
    }

    public synchronized boolean tryWithdraw(long amount) {
        if (amount <= 0L) throw new IllegalArgumentException("amount must be > 0");
        if (amount > balance) return false;
        balance -= amount;
        return true;
    }

    private static String requireId(String value) {
        Objects.requireNonNull(value, "playerId");
        String normalized = value.trim();
        if (normalized.isEmpty() || normalized.length() > 64) throw new IllegalArgumentException("invalid playerId");
        return normalized;
    }
}
