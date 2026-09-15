package kr.danta.paper.facility;

import kr.danta.core.facility.FacilityService;
import kr.danta.core.facility.FacilityState;
import kr.danta.core.state.GameState;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.structure.Structure;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/**
 * DEV-082 facility world appearance sync.
 * Core facility state is authoritative. Visual sync is best-effort and never loads chunks.
 */
public final class FacilityAppearanceService {
    private static final int SLOT_SPACING = 16; // provisional visual layout; replaceable later by map slot metadata
    private static final int SLOT_ORIGIN_X = 8;
    private static final int SLOT_ORIGIN_Z = 8;

    private final Server server;
    private final GameState gameState;
    private final FacilityService facilityService;
    private final Set<VisualKey> pending = new LinkedHashSet<>();

    public FacilityAppearanceService(Server server, GameState gameState, FacilityService facilityService) {
        this.server = Objects.requireNonNull(server);
        this.gameState = Objects.requireNonNull(gameState);
        this.facilityService = Objects.requireNonNull(facilityService);
    }

    public NamespacedKey templateKey(FacilityState facility) {
        Objects.requireNonNull(facility);
        return new NamespacedKey("danta", "facilities/" + facility.facilityId().toLowerCase()
                + "/tier_" + facility.tier().name().toLowerCase());
    }

    public Optional<Structure> loadTemplate(FacilityState facility) {
        return Optional.ofNullable(server.getStructureManager().loadStructure(templateKey(facility), true));
    }

    /** Queue every authoritative installed facility after snapshot restore/startup. */
    public synchronized void queueAllInstalled() {
        for (var point : gameState.strategicPoints()) {
            for (var facility : facilityService.facilities(point.pointId())) {
                pending.add(new VisualKey(point.pointId(), facility.facilityId()));
            }
        }
    }

    public synchronized void queue(String pointId, String facilityId) {
        pending.add(new VisualKey(pointId, facilityId));
    }

    public synchronized int pendingCount() { return pending.size(); }

    public synchronized SyncResult sync(String pointId, String facilityId) {
        var point = gameState.strategicPoint(pointId)
                .orElseThrow(() -> new IllegalArgumentException("point not found: " + pointId));
        FacilityState facility = facilityService.facility(pointId, facilityId)
                .orElseThrow(() -> new IllegalArgumentException("facility not found: " + facilityId));
        var pos = point.position();
        World world = server.getWorld(pos.worldName());
        if (world == null) {
            pending.add(new VisualKey(pointId, facilityId));
            return SyncResult.WORLD_UNAVAILABLE;
        }

        int slotIndex = slotIndex(pointId, facilityId);
        Location origin = slotOrigin(world, pos.x(), pos.y(), pos.z(), slotIndex);
        if (!world.isChunkLoaded(origin.getBlockX() >> 4, origin.getBlockZ() >> 4)) {
            pending.add(new VisualKey(pointId, facilityId));
            return SyncResult.CHUNK_UNLOADED;
        }

        Optional<Structure> structure = loadTemplate(facility);
        if (structure.isEmpty()) {
            pending.add(new VisualKey(pointId, facilityId));
            return SyncResult.TEMPLATE_MISSING;
        }

        structure.get().place(origin, false, StructureRotation.NONE, Mirror.NONE, 0, 1.0f, new Random(0L));
        pending.remove(new VisualKey(pointId, facilityId));
        return SyncResult.SYNCED;
    }

    /** Called from ChunkLoadEvent; does not itself cause chunk loading. */
    public synchronized int syncLoadedChunk(World world, int chunkX, int chunkZ) {
        int synced = 0;
        for (VisualKey key : Set.copyOf(pending)) {
            var pointOpt = gameState.strategicPoint(key.pointId());
            if (pointOpt.isEmpty()) {
                pending.remove(key);
                continue;
            }
            var point = pointOpt.get();
            if (!point.position().worldName().equals(world.getName())) continue;
            if (facilityService.facility(key.pointId(), key.facilityId()).isEmpty()) {
                pending.remove(key);
                continue;
            }
            int slotIndex = slotIndex(key.pointId(), key.facilityId());
            Location origin = slotOrigin(world, point.position().x(), point.position().y(), point.position().z(), slotIndex);
            if ((origin.getBlockX() >> 4) != chunkX || (origin.getBlockZ() >> 4) != chunkZ) continue;
            if (sync(key.pointId(), key.facilityId()) == SyncResult.SYNCED) synced++;
        }
        return synced;
    }

    private int slotIndex(String pointId, String facilityId) {
        var facilities = facilityService.facilities(pointId);
        for (int i = 0; i < facilities.size(); i++) {
            if (facilities.get(i).facilityId().equals(facilityId)) return i;
        }
        throw new IllegalArgumentException("facility not found in slot order: " + facilityId);
    }

    private Location slotOrigin(World world, int x, int y, int z, int slotIndex) {
        int column = slotIndex % 4;
        int row = slotIndex / 4;
        return new Location(world,
                x + SLOT_ORIGIN_X + column * SLOT_SPACING,
                y,
                z + SLOT_ORIGIN_Z + row * SLOT_SPACING);
    }

    public enum SyncResult { SYNCED, CHUNK_UNLOADED, TEMPLATE_MISSING, WORLD_UNAVAILABLE }
    private record VisualKey(String pointId, String facilityId) {}
}
