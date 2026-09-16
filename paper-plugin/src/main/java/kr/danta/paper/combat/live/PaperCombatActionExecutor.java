package kr.danta.paper.combat.live;

import kr.danta.core.combat.ai.CombatAiAction;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Applies one DEV-115 live execution intent to Bukkit entities.
 * This is intentionally a short-anchor steering primitive, not global pathfinding.
 */
public final class PaperCombatActionExecutor {
    private static final double WAYPOINT_REACHED_DISTANCE = 1.25;

    private final CombatEntityResolver resolver;
    private final CombatAttackPolicy attackPolicy;
    private final CombatTargetPolicy targetPolicy;
    private final WaypointProgressTracker waypointProgress;
    private final WaypointStepMover stepMover;

    public PaperCombatActionExecutor() {
        this(new CombatEntityResolver(), CombatAttackPolicy.developmentDefaults(), new CombatTargetPolicy());
    }

    public PaperCombatActionExecutor(
            CombatEntityResolver resolver,
            CombatAttackPolicy attackPolicy,
            CombatTargetPolicy targetPolicy
    ) {
        if (resolver == null) throw new NullPointerException("resolver");
        if (attackPolicy == null) throw new NullPointerException("attackPolicy");
        if (targetPolicy == null) throw new NullPointerException("targetPolicy");
        this.resolver = resolver;
        this.attackPolicy = attackPolicy;
        this.targetPolicy = targetPolicy;
        this.waypointProgress = new WaypointProgressTracker(WAYPOINT_REACHED_DISTANCE);
        this.stepMover = new WaypointStepMover();
    }

    public void apply(LiveCombatUnit unit, LiveCombatExecution execution, RuntimeAccess runtime) {
        if (unit == null || execution == null || runtime == null) return;

        Optional<LivingEntity> primaryOpt = resolver.primary(unit);
        Optional<LivingEntity> moverOpt = resolver.movementBody(unit);
        if (primaryOpt.isEmpty() || moverOpt.isEmpty()) return;

        LivingEntity primary = primaryOpt.orElseThrow();
        LivingEntity mover = moverOpt.orElseThrow();
        move(unit, execution, mover);

        Optional<LiveCombatUnit> targetUnitOpt = selectedTrackedHostile(unit, execution, runtime.units());
        CombatAttackPolicy.AttackSpec spec = attackPolicy.forType(unit.troopType());

        if (spec.mode() == CombatAttackPolicy.AttackMode.SUPPORT_VISUAL) {
            if (execution.decision().action() == CombatAiAction.SUPPORT
                    && runtime.tryAcquireAttack(unit.unitId(), System.currentTimeMillis(), spec.cooldownMillis())) {
                primary.getWorld().spawnParticle(Particle.ENCHANT, primary.getLocation().add(0, 1.0, 0), 8, 0.5, 0.5, 0.5, 0.02);
            }
            return;
        }

        if (targetUnitOpt.isEmpty()) return;
        Optional<LivingEntity> targetOpt = resolver.primary(targetUnitOpt.orElseThrow());
        if (targetOpt.isEmpty()) return;
        LivingEntity target = targetOpt.orElseThrow();

        double distance = primary.getLocation().distance(target.getLocation());
        if (distance > spec.range()) return;
        long now = System.currentTimeMillis();
        if (!runtime.tryAcquireAttack(unit.unitId(), now, spec.cooldownMillis())) return;

        switch (spec.mode()) {
            case MELEE -> target.damage(spec.damage(), primary);
            case RANGED -> fireArrow(primary, target, spec.damage());
            case SUPPORT_VISUAL -> { /* handled above */ }
        }
    }

    private void move(LiveCombatUnit unit, LiveCombatExecution execution, LivingEntity mover) {
        TacticalRoute route = execution.movementIntent().route();
        Location current = mover.getLocation();
        Optional<TacticalWaypoint> waypointOpt = waypointProgress.target(
                unit.unitId(),
                route,
                current.getX(),
                current.getY(),
                current.getZ());
        if (waypointOpt.isEmpty()) {
            mover.setVelocity(new Vector(0, mover.getVelocity().getY(), 0));
            return;
        }

        TacticalWaypoint waypoint = waypointOpt.orElseThrow();
        WaypointStepMover.Step next = stepMover.step(
                current.getX(), current.getY(), current.getZ(),
                waypoint.x(), waypoint.y(), waypoint.z(),
                attackPolicy.moveSpeed(unit.troopType()));
        Location destination = new Location(
                mover.getWorld(), next.x(), next.y(), next.z(), current.getYaw(), current.getPitch());
        mover.teleport(destination);
    }

    private Optional<LiveCombatUnit> selectedTrackedHostile(
            LiveCombatUnit attacker,
            LiveCombatExecution execution,
            Collection<LiveCombatUnit> units
    ) {
        UUID selectedId = execution.selectedHostileUnitId();
        if (selectedId == null || units == null) return Optional.empty();
        return units.stream()
                .filter(candidate -> selectedId.equals(candidate.unitId()))
                .filter(candidate -> targetPolicy.mayTarget(attacker, candidate))
                .findFirst();
    }

    private static void fireArrow(LivingEntity attacker, LivingEntity target, double damage) {
        World world = attacker.getWorld();
        Location origin = attacker.getEyeLocation();
        Vector direction = target.getEyeLocation().toVector().subtract(origin.toVector()).normalize();
        Arrow arrow = world.spawnArrow(origin, direction, 1.6f, 2.0f);
        arrow.setShooter(attacker);
        arrow.setDamage(damage);
        arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
        arrow.addScoreboardTag("danta_combat_demo_projectile");
    }

    /** Runtime-owned mutable state needed by the executor without coupling it to scheduler implementation. */
    public interface RuntimeAccess {
        Collection<LiveCombatUnit> units();

        boolean tryAcquireAttack(UUID unitId, long nowMillis, long cooldownMillis);
    }
}
