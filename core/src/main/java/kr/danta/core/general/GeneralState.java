package kr.danta.core.general;

import java.util.Objects;
import java.util.Optional;

/**
 * DEV-070 general aggregate baseline.
 * Grade F-S and level 1-10 are authoritative structure; numeric combat bonuses
 * are deliberately deferred to DEV-071+.
 */
public final class GeneralState {
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 10;

    private final String generalId;
    private final String ownerNationId;
    private GeneralGrade grade;
    private int level;
    private GeneralStats stats;
    private GeneralTroopSynergy troopSynergy;

    public GeneralState(String generalId, String ownerNationId, GeneralGrade grade, int level) {
        this(generalId, ownerNationId, grade, level, GeneralStats.zero());
    }

    public GeneralState(String generalId, String ownerNationId, GeneralGrade grade, int level, GeneralStats stats) {
        this.generalId = requireId(generalId, "generalId");
        this.ownerNationId = requireId(ownerNationId, "ownerNationId");
        this.grade = Objects.requireNonNull(grade, "grade");
        setLevel(level);
        this.stats = Objects.requireNonNull(stats, "stats");
    }

    public String generalId() { return generalId; }
    public String ownerNationId() { return ownerNationId; }
    public synchronized GeneralGrade grade() { return grade; }
    public synchronized int level() { return level; }
    public synchronized GeneralStats stats() { return stats; }
    public synchronized Optional<GeneralTroopSynergy> troopSynergy() { return Optional.ofNullable(troopSynergy); }

    public synchronized void setTroopSynergy(GeneralTroopSynergy troopSynergy) {
        this.troopSynergy = Objects.requireNonNull(troopSynergy, "troopSynergy");
    }

    public synchronized void clearTroopSynergy() {
        this.troopSynergy = null;
    }

    public synchronized void setStats(GeneralStats stats) {
        this.stats = Objects.requireNonNull(stats, "stats");
    }

    public synchronized void setGrade(GeneralGrade grade) {
        this.grade = Objects.requireNonNull(grade, "grade");
    }

    public synchronized void setLevel(int level) {
        if (level < MIN_LEVEL || level > MAX_LEVEL)
            throw new IllegalArgumentException("level must be between 1 and 10");
        this.level = level;
    }

    public synchronized boolean canLevelUp() {
        return level < MAX_LEVEL;
    }

    public synchronized void levelUp() {
        if (!canLevelUp()) throw new IllegalStateException("general is already at max level");
        level++;
    }

    private static String requireId(String value, String label) {
        Objects.requireNonNull(value, label);
        String normalized = value.trim();
        if (!normalized.matches("[A-Za-z0-9_-]{1,48}"))
            throw new IllegalArgumentException(label + " must match [A-Za-z0-9_-]{1,48}");
        return normalized;
    }
}
