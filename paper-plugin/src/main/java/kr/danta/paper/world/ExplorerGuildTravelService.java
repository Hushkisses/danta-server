package kr.danta.paper.world;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Objects;

/** DEV-MAP-003 validates configured gates and performs safe cross-world travel. */
public final class ExplorerGuildTravelService {
    private final DantaWorldService worlds;
    private final DantaWorldConfig config;
    private final ExplorerGuildTravelPolicy policy;

    public ExplorerGuildTravelService(DantaWorldService worlds, DantaWorldConfig config,
                                      ExplorerGuildTravelPolicy policy) {
        this.worlds = Objects.requireNonNull(worlds, "worlds");
        this.config = Objects.requireNonNull(config, "config");
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    public boolean isConfiguredGate(Block block, TravelGate gate) {
        if (block == null || gate == null || block.getType() != Material.LODESTONE) return false;
        WorldRole expectedRole = gate == TravelGate.EXPLORERS_GUILD
                ? WorldRole.STRATEGIC_MAIN : WorldRole.WILDERNESS;
        if (worlds.roleOf(block.getWorld()).orElse(null) != expectedRole) return false;
        Location configured = worlds.relativeLocation(expectedRole, config.gateOffset(gate));
        return block.getX() == configured.getBlockX()
                && block.getY() == configured.getBlockY()
                && block.getZ() == configured.getBlockZ();
    }

    public TravelResult travel(Player player, TravelGate gate) {
        Objects.requireNonNull(player, "player");
        WorldRole from = worlds.roleOf(player.getWorld())
                .orElseThrow(() -> new IllegalStateException("현재 월드는 탐험가 길드 이동 대상이 아닙니다."));
        if (!policy.canTravel(from, gate)) {
            throw new IllegalStateException("현재 월드에서는 이 이동 거점을 사용할 수 없습니다.");
        }
        WorldRole target = policy.destinationRole(from, gate);
        Location configured = worlds.relativeLocation(target, config.arrivalOffset(target));
        Location safe = safeDestination(configured);
        if (!player.teleport(safe)) {
            throw new IllegalStateException("목적지로 이동하지 못했습니다. 잠시 후 다시 시도해 주세요.");
        }
        return new TravelResult(from, target, safe);
    }

    private static Location safeDestination(Location configured) {
        World world = Objects.requireNonNull(configured.getWorld());
        int x = configured.getBlockX(), baseY = configured.getBlockY(), z = configured.getBlockZ();
        for (int dy = 0; dy <= 4; dy++) {
            int feetY = baseY + dy;
            Block floor = world.getBlockAt(x, feetY - 1, z);
            Block feet = world.getBlockAt(x, feetY, z);
            Block head = world.getBlockAt(x, feetY + 1, z);
            if (isSafeFloor(floor.getType()) && feet.getType().isAir() && head.getType().isAir()) {
                return new Location(world, x + 0.5, feetY, z + 0.5, 0.0f, 0.0f);
            }
        }
        throw new IllegalStateException("안전한 이동 목적지를 찾지 못했습니다. 관리자에게 알려 주세요.");
    }

    private static boolean isSafeFloor(Material material) {
        if (!material.isSolid()) return false;
        return material != Material.MAGMA_BLOCK
                && material != Material.CACTUS
                && material != Material.CAMPFIRE
                && material != Material.SOUL_CAMPFIRE;
    }

    public record TravelResult(WorldRole from, WorldRole to, Location destination) {}
}
