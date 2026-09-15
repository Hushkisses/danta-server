package kr.danta.core.facility;

/** DEV-080 facility upgrade tiers. */
public enum FacilityTier {
    I(1), II(2), III(3);
    private final int level;
    FacilityTier(int level) { this.level = level; }
    public int level() { return level; }
    public FacilityTier next() {
        return switch (this) { case I -> II; case II -> III; case III -> throw new IllegalStateException("facility already tier III"); };
    }
}
