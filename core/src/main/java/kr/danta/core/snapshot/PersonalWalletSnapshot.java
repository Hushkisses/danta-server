package kr.danta.core.snapshot;

public record PersonalWalletSnapshot(String playerId, long balance) {
    public PersonalWalletSnapshot {
        if (playerId == null || playerId.isBlank()) throw new IllegalArgumentException("playerId must not be blank");
        if (balance < 0L) throw new IllegalArgumentException("balance must be >= 0");
    }
}
