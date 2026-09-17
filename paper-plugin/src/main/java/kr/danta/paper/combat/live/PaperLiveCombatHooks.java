package kr.danta.paper.combat.live;

import kr.danta.core.combat.TroopType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Paper implementation behind the DEV-115 live combat runtime.
 * Owns only temporary demo entities and never mutates strategic army state.
 */
final class PaperLiveCombatHooks implements LiveCombatRuntime.Hooks {
    private static final double SPAWN_X_OFFSET = 12.0;
    private static final double SPAWN_Z_SPACING = 2.5;

    private final LiveCombatUnitRegistry registry = new LiveCombatUnitRegistry();
    private final PaperCombatUnitFactory unitFactory = new PaperCombatUnitFactory();
    private final CombatEntityResolver resolver = new CombatEntityResolver();
    private final CombatAttackPolicy attackPolicy = CombatAttackPolicy.developmentDefaults();
    private final CombatTargetPolicy targetPolicy = new CombatTargetPolicy();
    private final CombatTargetSelectionPolicy targetSelectionPolicy = new CombatTargetSelectionPolicy();
    private final PaperCombatActionExecutor executor = new PaperCombatActionExecutor(resolver, attackPolicy, targetPolicy);
    private final LiveCombatController controller = new LiveCombatController();
    private final LiveCombatLoopCadence cadence = new LiveCombatLoopCadence(2, 10);
    private final PaperCombatActionExecutor.RuntimeAccess runtimeAccess;
    private final Map<UUID, LiveCombatExecution> executions = new HashMap<>();

    private DemoBattlefieldLayout layout;

    PaperLiveCombatHooks(PaperCombatActionExecutor.RuntimeAccess runtimeAccess) {
        this.runtimeAccess = java.util.Objects.requireNonNull(runtimeAccess, "runtimeAccess");
    }

    @Override
    public void start(World world, Location origin) {
        if (!registry.units().isEmpty()) {
            throw new IllegalStateException("live combat demo already owns units");
        }

        layout = DemoBattlefieldLayout.around(origin.getX(), origin.getY(), origin.getZ());
        LiveCombatDemoFormation formation = LiveCombatDemoFormation.developmentDefaults();
        ArrayList<PaperCombatUnitFactory.SpawnedCombatUnit> spawned = new ArrayList<>();

        try {
            for (LiveCombatDemoFormation.Slot slot : formation.slots()) {
                Location spawn = spawnLocation(world, origin, slot);
                PaperCombatUnitFactory.SpawnedCombatUnit created = unitFactory.spawn(
                        world, spawn, slot.side(), slot.troopType());
                prepareControlledMob(created.primary());
                if (created.mount() != null) prepareControlledMob(created.mount());
                registry.register(created.unit());
                CombatHealthBarDisplay.sync(created.unit(), created.primary());
                spawned.add(created);
            }
            reevaluateAll();
        } catch (RuntimeException ex) {
            for (PaperCombatUnitFactory.SpawnedCombatUnit created : spawned) {
                removeEntity(created.primary());
                removeEntity(created.mount());
            }
            registry.clear();
            executions.clear();
            layout = null;
            throw ex;
        }
    }

    @Override
    public void tick() {
        cleanupDeadUnits();
        if (registry.units().isEmpty()) return;

        LiveCombatLoopCadence.TickSchedule schedule = cadence.advance();
        if (schedule.decisionDue()) {
            reevaluateAll();
        }
        if (schedule.movementDue()) {
            executeAll();
            syncHealthBars();
        }
    }

    @Override
    public void stop() {
        for (LiveCombatUnit unit : java.util.List.copyOf(registry.units())) {
            retire(unit);
        }
        registry.clear();
        executions.clear();
        layout = null;
    }

    @Override
    public void onTrackedEntityDeath(UUID entityId) {
        registry.byEntity(entityId).ifPresent(this::retire);
    }

    Collection<LiveCombatUnit> units() {
        return registry.units();
    }

    boolean mayDamage(UUID attackerEntityId, UUID victimEntityId) {
        Optional<LiveCombatUnit> attacker = registry.byEntity(attackerEntityId);
        Optional<LiveCombatUnit> victim = registry.byEntity(victimEntityId);
        return attacker.isPresent()
                && victim.isPresent()
                && attacker.get().side() != victim.get().side();
    }

    private void reevaluateAll() {
        if (layout == null) return;
        for (LiveCombatUnit unit : registry.units()) {
            controllerView(unit).ifPresent(view ->
                    executions.put(unit.unitId(), controller.decide(unit, view, layout)));
        }
    }

    private void executeAll() {
        for (LiveCombatUnit unit : registry.units()) {
            LiveCombatExecution execution = executions.get(unit.unitId());
            if (execution != null) {
                executor.apply(unit, execution, runtimeAccess);
            }
        }
    }

    private void syncHealthBars() {
        for (LiveCombatUnit unit : registry.units()) {
            resolver.primary(unit).ifPresent(entity -> CombatHealthBarDisplay.sync(unit, entity));
        }
    }

    private Optional<LiveCombatController.BattlefieldView> controllerView(LiveCombatUnit unit) {
        Optional<LivingEntity> selfOpt = resolver.primary(unit);
        if (selfOpt.isEmpty()) return Optional.empty();
        LivingEntity self = selfOpt.orElseThrow();

        List<LiveCombatUnit> hostileUnits = registry.units().stream()
                .filter(candidate -> candidate.side() != unit.side())
                .filter(candidate -> resolver.primary(candidate).isPresent())
                .toList();

        Optional<LiveCombatUnit> nearestHostile = hostileUnits.stream()
                .min(Comparator.comparingDouble(candidate -> distanceSquared(self, candidate)));

        if (nearestHostile.isEmpty()) {
            return Optional.of(new LiveCombatController.BattlefieldView(
                    999.0, null, false, false, false, false, false, false,
                    alliedCount(unit) >= 2, null));
        }

        LiveCombatUnit hostile = nearestHostile.orElseThrow();
        LivingEntity hostileEntity = resolver.primary(hostile).orElseThrow();
        double distance = self.getLocation().distance(hostileEntity.getLocation());

        List<CombatTargetSelectionPolicy.Candidate> targetCandidates = hostileUnits.stream()
                .map(candidate -> new CombatTargetSelectionPolicy.Candidate(
                        candidate.unitId(), candidate.troopType(),
                        Math.sqrt(distanceSquared(self, candidate))))
                .toList();
        UUID selectedTargetId = targetSelectionPolicy.select(
                unit.troopType(), targetCandidates, attackPolicy.forType(unit.troopType()).range())
                .orElse(null);

        boolean backlineThreatened = (unit.troopType() == TroopType.ARCHERS || unit.troopType() == TroopType.MAGIC)
                && distance <= 4.0;
        boolean exposedEnemyBackline = unit.troopType() == TroopType.CAVALRY
                && registry.units().stream().anyMatch(candidate -> candidate.side() != unit.side()
                && (candidate.troopType() == TroopType.ARCHERS || candidate.troopType() == TroopType.MAGIC));
        boolean spearScreenPresent = registry.units().stream().anyMatch(candidate -> candidate.side() != unit.side()
                && candidate.troopType() == TroopType.SPEARMEN
                && resolver.primary(candidate).map(entity -> entity.getLocation().distance(self.getLocation()) <= 7.0).orElse(false));
        boolean frontlineSupportPresent = registry.units().stream().anyMatch(candidate -> candidate.side() == unit.side()
                && candidate.unitId() != unit.unitId()
                && (candidate.troopType() == TroopType.INFANTRY || candidate.troopType() == TroopType.SPEARMEN));
        boolean survivalThreatened = self.getHealth() <= Math.max(1.0, self.getMaxHealth() * 0.25);

        return Optional.of(new LiveCombatController.BattlefieldView(
                distance,
                hostile.troopType(),
                frontlineSupportPresent,
                backlineThreatened,
                exposedEnemyBackline,
                false,
                spearScreenPresent,
                survivalThreatened,
                alliedCount(unit) >= 2,
                selectedTargetId));
    }

    private int alliedCount(LiveCombatUnit unit) {
        int count = 0;
        for (LiveCombatUnit candidate : registry.units()) {
            if (candidate.side() == unit.side() && !candidate.unitId().equals(unit.unitId())) count++;
        }
        return count;
    }

    private double distanceSquared(LivingEntity self, LiveCombatUnit candidate) {
        return resolver.primary(candidate)
                .map(entity -> entity.getLocation().distanceSquared(self.getLocation()))
                .orElse(Double.POSITIVE_INFINITY);
    }

    private void cleanupDeadUnits() {
        for (LiveCombatUnit unit : java.util.List.copyOf(registry.units())) {
            boolean primaryAlive = resolver.primary(unit).isPresent();
            boolean mountAlive = unit.mountEntityId() == null || resolver.living(unit.mountEntityId()).isPresent();
            if (!primaryAlive || !mountAlive) retire(unit);
        }
    }

    private void retire(LiveCombatUnit unit) {
        removeById(unit.primaryEntityId());
        removeById(unit.mountEntityId());
        executions.remove(unit.unitId());
        registry.remove(unit.unitId());
    }

    private static Location spawnLocation(World world, Location origin, LiveCombatDemoFormation.Slot slot) {
        double x = origin.getX() + (slot.side() == CombatSide.RED ? -SPAWN_X_OFFSET : SPAWN_X_OFFSET);
        double zIndex = slot.formationIndex() - 2.0;
        double z = origin.getZ() + zIndex * SPAWN_Z_SPACING;
        return new Location(world, x, origin.getY(), z);
    }

    private static void prepareControlledMob(LivingEntity entity) {
        entity.setAI(true);
        entity.setCollidable(true);
        if (entity instanceof Mob mob) {
            mob.setAware(true);
            mob.setTarget(null);
        }
    }

    private static void removeById(UUID entityId) {
        if (entityId == null) return;
        Entity entity = Bukkit.getEntity(entityId);
        if (entity != null && entity.isValid()) entity.remove();
    }

    private static void removeEntity(Entity entity) {
        if (entity != null && entity.isValid()) entity.remove();
    }
}
