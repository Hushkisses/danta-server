package kr.danta.paper.combat.live;

import kr.danta.paper.combat.ai.PaperCombatAiRuntime;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** DEV-115 live combat runtime lifecycle boundary. */
public final class LiveCombatRuntime implements PaperCombatActionExecutor.RuntimeAccess {
    private final LiveCombatRuntimeState state = new LiveCombatRuntimeState();
    private final Hooks hooks;
    private final JavaPlugin plugin;
    private BukkitTask loopTask;

    public LiveCombatRuntime() {
        this(null, Hooks.noop());
    }

    public LiveCombatRuntime(Hooks hooks) {
        this(null, hooks);
    }

    public LiveCombatRuntime(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.hooks = new PaperLiveCombatHooks(this);
    }

    /** Keeps the approved Task-9 construction boundary while DEV-114 runtime remains controller-owned. */
    public LiveCombatRuntime(JavaPlugin plugin, PaperCombatAiRuntime ignoredAiRuntime) {
        this(plugin);
        Objects.requireNonNull(ignoredAiRuntime, "ignoredAiRuntime");
    }

    private LiveCombatRuntime(JavaPlugin plugin, Hooks hooks) {
        this.plugin = plugin;
        this.hooks = Objects.requireNonNull(hooks, "hooks");
    }

    public boolean startDemo(World world, Location origin) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(origin, "origin");
        if (!state.begin()) return false;
        try {
            hooks.start(world, origin);
            if (plugin != null) {
                loopTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
            }
            return true;
        } catch (RuntimeException ex) {
            cancelLoop();
            try {
                hooks.stop();
            } finally {
                state.finish();
            }
            throw ex;
        }
    }

    public void stopDemo() {
        if (!state.active()) return;
        cancelLoop();
        try {
            hooks.stop();
        } finally {
            state.finish();
        }
    }

    public void tick() {
        if (!state.active()) return;
        hooks.tick();
    }

    public String status() {
        return state.active() ? "실행 중" : "정지됨";
    }

    public void shutdown() {
        stopDemo();
    }

    public void onTrackedEntityDeath(UUID entityId) {
        if (entityId == null || !state.active()) return;
        hooks.onTrackedEntityDeath(entityId);
    }

    public boolean mayDamage(UUID attackerEntityId, UUID victimEntityId) {
        if (!state.active() || attackerEntityId == null || victimEntityId == null) return false;
        if (hooks instanceof PaperLiveCombatHooks paperHooks) {
            return paperHooks.mayDamage(attackerEntityId, victimEntityId);
        }
        return false;
    }

    @Override
    public Collection<LiveCombatUnit> units() {
        if (hooks instanceof PaperLiveCombatHooks paperHooks) {
            return paperHooks.units();
        }
        return List.of();
    }

    @Override
    public boolean tryAcquireAttack(UUID unitId, long nowMillis, long cooldownMillis) {
        return state.tryAcquireAttack(unitId, nowMillis, cooldownMillis);
    }

    private void cancelLoop() {
        if (loopTask != null) {
            loopTask.cancel();
            loopTask = null;
        }
    }

    public interface Hooks {
        void start(World world, Location origin);

        void tick();

        void stop();

        void onTrackedEntityDeath(UUID entityId);

        static Hooks noop() {
            return new Hooks() {
                @Override
                public void start(World world, Location origin) {
                }

                @Override
                public void tick() {
                }

                @Override
                public void stop() {
                }

                @Override
                public void onTrackedEntityDeath(UUID entityId) {
                }
            };
        }
    }
}
