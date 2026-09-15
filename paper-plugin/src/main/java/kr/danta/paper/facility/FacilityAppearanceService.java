package kr.danta.paper.facility;

import kr.danta.core.facility.FacilityState;
import kr.danta.core.state.GameState;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.structure.Structure;

import java.util.Objects;
import java.util.Optional;

/**
 * DEV-082 NBT appearance loader foundation.
 * Authoritative facility state remains in core; missing/unloaded visuals never change game state.
 */
public final class FacilityAppearanceService {
    private final Server server;
    private final GameState gameState;

    public FacilityAppearanceService(Server server, GameState gameState) {
        this.server = Objects.requireNonNull(server);
        this.gameState = Objects.requireNonNull(gameState);
    }

    public NamespacedKey templateKey(FacilityState facility) {
        Objects.requireNonNull(facility);
        return new NamespacedKey("danta", "facilities/" + facility.facilityId().toLowerCase()
                + "/tier_" + facility.tier().name().toLowerCase());
    }

    public Optional<Structure> loadTemplate(FacilityState facility) {
        NamespacedKey key = templateKey(facility);
        Structure structure = server.getStructureManager().getStructure(key);
        return Optional.ofNullable(structure);
    }

    public boolean canSyncNow(String pointId) {
        var point = gameState.strategicPoint(pointId)
                .orElseThrow(() -> new IllegalArgumentException("point not found: " + pointId));
        var pos = point.position();
        World world = server.getWorld(pos.worldName());
        return world != null && world.isChunkLoaded(pos.x() >> 4, pos.z() >> 4);
    }
}
