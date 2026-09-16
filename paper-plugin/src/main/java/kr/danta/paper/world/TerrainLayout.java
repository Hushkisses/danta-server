package kr.danta.paper.world;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** DEV-MAP-004 validated logical layout; independent from Bukkit rendering. */
public record TerrainLayout(String layoutId, int tileSize, List<TerrainTileSpec> tiles) {
    public TerrainLayout {
        Objects.requireNonNull(layoutId, "layoutId");
        if (layoutId.isBlank()) throw new IllegalArgumentException("layoutId must not be blank");
        if (tileSize < 16) throw new IllegalArgumentException("tileSize must be >= 16");
        tiles = List.copyOf(Objects.requireNonNull(tiles, "tiles"));
        Set<String> occupied = new HashSet<>();
        for (TerrainTileSpec tile : tiles) {
            String key = tile.tileX() + ":" + tile.tileZ();
            if (!occupied.add(key)) throw new IllegalArgumentException("duplicate terrain tile coordinate: " + key);
        }
    }

    public long count(TerrainTileType type) {
        return tiles.stream().filter(tile -> tile.type() == type).count();
    }
}
