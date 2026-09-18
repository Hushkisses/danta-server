package kr.danta.paper.siege;

import kr.danta.core.siege.SiegeSide;

import java.util.Locale;
import java.util.Objects;

/** Small Paper-independent helpers for DEV-118 siege player validation. */
public final class SiegePlayerCommandPolicy {
    private SiegePlayerCommandPolicy() {}

    public static SiegeSide parseSide(String token) {
        Objects.requireNonNull(token, "token");
        return switch (token.trim().toLowerCase(Locale.ROOT)) {
            case "attack" -> SiegeSide.ATTACKER;
            case "defend" -> SiegeSide.DEFENDER;
            default -> throw new IllegalArgumentException("공성 측은 attack 또는 defend 중 하나여야 합니다.");
        };
    }

    public static String sideText(SiegeSide side) {
        Objects.requireNonNull(side, "side");
        return side == SiegeSide.ATTACKER ? "공격" : "방어";
    }

    public static boolean lethal(double currentHealth, double finalDamage) {
        if (!Double.isFinite(currentHealth) || currentHealth < 0.0) {
            throw new IllegalArgumentException("currentHealth must be finite and >= 0");
        }
        if (!Double.isFinite(finalDamage) || finalDamage < 0.0) {
            throw new IllegalArgumentException("finalDamage must be finite and >= 0");
        }
        return finalDamage >= currentHealth;
    }
}
