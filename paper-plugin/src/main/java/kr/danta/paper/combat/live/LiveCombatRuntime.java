package kr.danta.paper.combat.live;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;
import java.util.UUID;

/** DEV-115 live combat runtime lifecycle boundary. */
public final class LiveCombatRuntime {
    private final LiveCombatRuntimeState state = new LiveCombatRuntimeState();
    private final Hooks hooks;

    public LiveCombatRuntime() {
        this(Hooks.noop());
    }

    public LiveCombatRuntime(Hooks hooks) {
        this.hooks = Objects.requireNonNull(hooks, "hooks");
    }

    public boolean startDemo(World world, Location origin) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(origin, "origin");
        if (!state.begin()) return false;
        try {
            hooks.start(world, origin);
            return true;
        } catch (RuntimeException ex) {
            state.finish();
            throw ex;
        }
    }

    public void stopDemo() {
        if (!state.active()) return;
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
