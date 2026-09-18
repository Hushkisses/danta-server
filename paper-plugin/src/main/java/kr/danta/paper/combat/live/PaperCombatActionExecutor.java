package kr.danta.paper.combat.live;

import kr.danta.core.combat.ai.CombatAiAction;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/** Applies one DEV-115 live execution intent to Bukkit entities. */
public final class PaperCombatActionExecutor {
    private static final double WAYPOINT_REACHED_DISTANCE = 1.25;
    private static final double ARROW_SPEED = 1.6;
    private static final double ARROW_LIFT_PER_HORIZONTAL_BLOCK = 0.20;
    private static final float ARROW_SPREAD = 0.0f;

    private final CombatEntityResolver resolver;
    private final CombatAttackPolicy attackPolicy;
    private final CombatTargetPolicy targetPolicy;
    private final WaypointProgressTracker waypointProgress;

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
    }

    public void apply(LiveCombatUnit unit, LiveCombatExecution execution, RuntimeAccess runtime) {
        if (unit == null || execution == null || runtime == null) return;

        Optional<LivingEntity> primaryOpt = resolver.primary(unit);
        Optional<LivingEntity> moverOpt = resolver.movementBody(unit);
        if (primaryOpt.isEmpty() || moverOpt.isEmpty()) return;

        LivingEntity primary = primaryOpt.orElseThrow();
        LivingEntity mover = moverOpt.orElseThrow();
        if (CombatMovementFallbackPolicy.shouldHoldCurrentPosition(
                execution.decision().action(), execution.selectedHostileUnitId())) {
            stopMovement(mover);
        } else {
            move(unit, execution, primary, mover);
        }

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
        LiveCombatUnit targetUnit = targetUnitOpt.orElseThrow();
        Optional<LivingEntity> targetOpt = resolver.primary(targetUnit);
        if (targetOpt.isEmpty()) return;
        LivingEntity target = targetOpt.orElseThrow();

        face(primary, target.getEyeLocation());
        double distance = primary.getLocation().distance(target.getLocation());

        if (CombatEngagementPolicy.shouldHoldRangedPosition(
                unit.troopType(), spec.mode(), execution.decision().action(), distance, spec.range())) {
            stopMovement(mover);
        }

        boolean generalClosing = CombatEngagementPolicy.shouldChase(
                spec.mode(), execution.decision().action(), distance, spec.range());
        boolean flankMatchupClosing = CombatEngagementPolicy.shouldChasePriorityMatchup(
                spec.mode(), unit.troopType(), targetUnit.troopType(),
                execution.decision().action(), distance, spec.range());
        boolean backlineClosing = CombatEngagementPolicy.shouldChaseBacklineTarget(
                spec.mode(), unit.troopType(), targetUnit.troopType(),
                execution.decision().action(), distance, spec.range());
        boolean rangedClosing = CombatEngagementPolicy.shouldChaseRangedTarget(
                spec.mode(), execution.decision().action(), distance, spec.range());
        if (generalClosing || flankMatchupClosing || backlineClosing || rangedClosing) {
            chase(unit, mover, target);
            return;
        }
        if (spec.mode() == CombatAttackPolicy.AttackMode.MELEE && distance <= spec.range()) {
            stopMovement(mover);
        }
        if (distance > spec.range()) return;

        long now = System.currentTimeMillis();
        if (!runtime.tryAcquireAttack(unit.unitId(), now, spec.cooldownMillis())) return;

        switch (spec.mode()) {
            case MELEE -> {
                primary.swingMainHand();
                target.damage(spec.damage(), primary);
                CombatHealthBarDisplay.sync(targetUnit, target);
            }
            case RANGED -> fireArrow(primary, target, spec.damage());
            case SUPPORT_VISUAL -> { /* handled above */ }
        }
    }

    private void chase(LiveCombatUnit unit, LivingEntity mover, LivingEntity target) {
        face(mover, target.getLocation());
        if (mover instanceof Mob mob) {
            double pathfinderSpeed = CombatMobNavigationPolicy.pathfinderSpeedMultiplier(
                    attackPolicy.moveSpeed(unit.troopType()));
            mob.getPathfinder().moveTo(target.getLocation(), pathfinderSpeed);
        }
    }

    private void move(
            LiveCombatUnit unit,
            LiveCombatExecution execution,
            LivingEntity primary,
            LivingEntity mover
    ) {
        TacticalRoute route = execution.movementIntent().route();
        Location current = mover.getLocation();
        Optional<TacticalWaypoint> waypointOpt = waypointProgress.target(
                unit.unitId(), route, current.getX(), current.getY(), current.getZ());
        if (waypointOpt.isEmpty()) {
            stopMovement(mover);
            return;
        }

        TacticalWaypoint waypoint = waypointOpt.orElseThrow();
        Location goal = resolver.toLocation(mover.getWorld(), waypoint);
        face(primary, goal);
        if (mover != primary) face(mover, goal);

        if (mover instanceof Mob mob) {
            double pathfinderSpeed = CombatMobNavigationPolicy.pathfinderSpeedMultiplier(
                    attackPolicy.moveSpeed(unit.troopType()));
            mob.getPathfinder().moveTo(goal, pathfinderSpeed);
            return;
        }
        mover.teleport(goal);
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

    private static void stopMovement(LivingEntity mover) {
        if (mover instanceof Mob mob) mob.getPathfinder().stopPathfinding();
    }

    private static void face(LivingEntity entity, Location target) {
        if (entity instanceof Mob mob) mob.lookAt(target);
    }

    private static void fireArrow(LivingEntity attacker, LivingEntity target, double damage) {
        World world = attacker.getWorld();
        Location origin = attacker.getEyeLocation();
        var bounds = target.getBoundingBox();
        Location targetCenter = new Location(
                world,
                (bounds.getMinX() + bounds.getMaxX()) * 0.5,
                (bounds.getMinY() + bounds.getMaxY()) * 0.5,
                (bounds.getMinZ() + bounds.getMaxZ()) * 0.5);
        double dx = targetCenter.getX() - origin.getX();
        double dz = targetCenter.getZ() - origin.getZ();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        CombatProjectileAim.AimOffset aim = CombatProjectileAim.compensatedOffset(
                horizontalDistance,
                targetCenter.getY() - origin.getY(),
                ARROW_SPEED,
                ARROW_GRAVITY_PER_TICK,
                ARROW_DRAG);
        Vector direction = new Vector(dx, aim.vertical(), dz).normalize();
        Arrow arrow = world.spawnArrow(origin, direction, (float) ARROW_SPEED, ARROW_SPREAD);
        arrow.setShooter(attacker);
        arrow.setDamage(damage);
        arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
        arrow.addScoreboardTag("danta_combat_demo_projectile");
    }

    public interface RuntimeAccess {
        Collection<LiveCombatUnit> units();
        boolean tryAcquireAttack(UUID unitId, long nowMillis, long cooldownMillis);
    }
}
