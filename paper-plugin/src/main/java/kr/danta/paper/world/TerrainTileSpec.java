package kr.danta.paper.world;

import java.util.Objects;

/** DEV-MAP-004 one logical terrain tile placement. */
public record TerrainTileSpec(int tileX, int tileZ, TerrainTileType type, int quarterTurns) {
    public TerrainTileSpec {
        Objects.requireNonNull(type, "type");
        if (quarterTurns < 0 || quarterTurns > 3) {
            throw new IllegalArgumentException("quarterTurns must be between 0 and 3");
        }
    }

    public boolean eastWest() {
        return quarterTurns % 2 == 1;
    }
}
