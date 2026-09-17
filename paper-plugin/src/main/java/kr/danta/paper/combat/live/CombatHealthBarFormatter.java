package kr.danta.paper.combat.live;

import kr.danta.core.combat.TroopType;

/** Pure formatter for DEV-115 overhead combat labels. */
public final class CombatHealthBarFormatter {
    private static final int SEGMENTS = 10;

    private CombatHealthBarFormatter() {}

    public static String format(CombatSide side, TroopType troopType, double health, double maxHealth) {
        if (side == null) throw new NullPointerException("side");
        if (troopType == null) throw new NullPointerException("troopType");
        if (!Double.isFinite(health)) throw new IllegalArgumentException("health must be finite");
        if (!Double.isFinite(maxHealth) || maxHealth <= 0.0) throw new IllegalArgumentException("maxHealth must be > 0");

        double clamped = Math.max(0.0, Math.min(health, maxHealth));
        int shownHealth = clamped <= 0.0 ? 0 : (int) Math.ceil(clamped);
        int shownMax = (int) Math.ceil(maxHealth);

        int filled = clamped <= 0.0 ? 0 : (int) Math.ceil((clamped / maxHealth) * SEGMENTS);
        filled = Math.max(0, Math.min(SEGMENTS, filled));

        return sideLabel(side) + " " + troopLabel(troopType)
                + "  ♥ " + shownHealth + "/" + shownMax + "  "
                + "█".repeat(filled) + "░".repeat(SEGMENTS - filled);
    }

    private static String sideLabel(CombatSide side) {
        return switch (side) {
            case RED -> "[적]";
            case BLUE -> "[청]";
        };
    }

    private static String troopLabel(TroopType troopType) {
        return switch (troopType) {
            case INFANTRY -> "보병";
            case SPEARMEN -> "창병";
            case ARCHERS -> "궁병";
            case CAVALRY -> "기병";
            case MAGIC -> "마법병";
        };
    }
}
