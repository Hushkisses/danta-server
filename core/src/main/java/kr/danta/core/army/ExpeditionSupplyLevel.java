package kr.danta.core.army;

/** DEV-056 selectable expedition supply level. Exact balance amounts remain provisional. */
public enum ExpeditionSupplyLevel {
    LIGHT("경량", 100L),
    STANDARD("표준", 300L),
    HEAVY("대량", 600L);

    private final String displayName;
    private final long foodAmount;

    ExpeditionSupplyLevel(String displayName, long foodAmount) {
        this.displayName = displayName;
        this.foodAmount = foodAmount;
    }

    public String displayName() { return displayName; }
    public long foodAmount() { return foodAmount; }
}
