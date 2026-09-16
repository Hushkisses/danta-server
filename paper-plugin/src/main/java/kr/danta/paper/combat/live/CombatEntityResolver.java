package kr.danta.paper.combat.live;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Optional;
import java.util.UUID;

/** Resolves temporary DEV-115 combat identities back to live Bukkit entities. */
public final class CombatEntityResolver {

    public Optional<LivingEntity> primary(LiveCombatUnit unit) {
        if (unit == null) return Optional.empty();
        return living(unit.primaryEntityId());
    }

    public Optional<LivingEntity> movementBody(LiveCombatUnit unit) {
        if (unit == null) return Optional.empty();
        if (unit.mountEntityId() != null) {
            Optional<LivingEntity> mount = living(unit.mountEntityId());
            if (mount.isPresent()) return mount;
        }
        return primary(unit);
    }

    public Optional<LivingEntity> living(UUID entityId) {
        if (entityId == null) return Optional.empty();
        Entity entity = Bukkit.getEntity(entityId);
        if (!(entity instanceof LivingEntity living) || !living.isValid() || living.isDead()) {
            return Optional.empty();
        }
        return Optional.of(living);
    }

    public Location toLocation(World world, TacticalWaypoint waypoint) {
        if (world == null) throw new NullPointerException("world");
        if (waypoint == null) throw new NullPointerException("waypoint");
        return new Location(world, waypoint.x(), waypoint.y(), waypoint.z());
    }
}
