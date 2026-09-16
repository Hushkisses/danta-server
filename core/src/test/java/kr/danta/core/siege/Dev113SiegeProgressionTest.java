package kr.danta.core.siege;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class Dev113SiegeProgressionTest {
    @Test
    void capitalSiegeRequiresThreeBattlesSeparatedByTwoGateBreaches() {
        assertDoesNotThrow(() -> {
            Class<?> progressType = Class.forName("kr.danta.core.siege.SiegeProgress");
            Class<?> stageType = Class.forName("kr.danta.core.siege.SiegeStage");
            Class<?> eventType = Class.forName("kr.danta.core.siege.SiegeProgressEvent");
            Class<?> profileType = Class.forName("kr.danta.core.siege.SiegeEngagementProfile");

            Object capital = Enum.valueOf((Class<Enum>) profileType.asSubclass(Enum.class), "CAPITAL_THREE_BATTLE");
            Constructor<?> ctor = progressType.getConstructor(profileType);
            Object progress = ctor.newInstance(capital);
            Method stage = progressType.getMethod("stage");
            Method apply = progressType.getMethod("apply", eventType);
            Method complete = progressType.getMethod("complete");

            assertEquals("OUTER_BATTLE", ((Enum<?>) stage.invoke(progress)).name());

            Object outerWin = Enum.valueOf((Class<Enum>) eventType.asSubclass(Enum.class), "BATTLE_WON");
            Object gateBreach = Enum.valueOf((Class<Enum>) eventType.asSubclass(Enum.class), "GATE_BREACHED");

            apply.invoke(progress, outerWin);
            assertEquals("OUTER_GATE", ((Enum<?>) stage.invoke(progress)).name());
            apply.invoke(progress, gateBreach);
            assertEquals("PLAZA_BATTLE", ((Enum<?>) stage.invoke(progress)).name());
            apply.invoke(progress, outerWin);
            assertEquals("INNER_GATE", ((Enum<?>) stage.invoke(progress)).name());
            apply.invoke(progress, gateBreach);
            assertEquals("CORE_BATTLE", ((Enum<?>) stage.invoke(progress)).name());
            apply.invoke(progress, outerWin);
            assertEquals("COMPLETE", ((Enum<?>) stage.invoke(progress)).name());
            assertEquals(true, complete.invoke(progress));
        });
    }

    @Test
    void capitalSiegeRejectsSkippingAhead() {
        assertDoesNotThrow(() -> {
            Class<?> progressType = Class.forName("kr.danta.core.siege.SiegeProgress");
            Class<?> eventType = Class.forName("kr.danta.core.siege.SiegeProgressEvent");
            Class<?> profileType = Class.forName("kr.danta.core.siege.SiegeEngagementProfile");
            Object capital = Enum.valueOf((Class<Enum>) profileType.asSubclass(Enum.class), "CAPITAL_THREE_BATTLE");
            Object progress = progressType.getConstructor(profileType).newInstance(capital);
            Object gateBreach = Enum.valueOf((Class<Enum>) eventType.asSubclass(Enum.class), "GATE_BREACHED");
            Method apply = progressType.getMethod("apply", eventType);

            Throwable thrown = assertThrows(Throwable.class, () -> apply.invoke(progress, gateBreach));
            Throwable cause = thrown.getCause() == null ? thrown : thrown.getCause();
            assertInstanceOf(IllegalStateException.class, cause);
        });
    }

    @Test
    void majorPointUsesOneBattleWhileNormalPointUsesQuickResolution() {
        assertDoesNotThrow(() -> {
            Class<?> progressType = Class.forName("kr.danta.core.siege.SiegeProgress");
            Class<?> eventType = Class.forName("kr.danta.core.siege.SiegeProgressEvent");
            Class<?> profileType = Class.forName("kr.danta.core.siege.SiegeEngagementProfile");
            Method complete = progressType.getMethod("complete");
            Method apply = progressType.getMethod("apply", eventType);

            Object major = Enum.valueOf((Class<Enum>) profileType.asSubclass(Enum.class), "MAJOR_SINGLE_BATTLE");
            Object quick = Enum.valueOf((Class<Enum>) profileType.asSubclass(Enum.class), "NORMAL_QUICK");
            Object battleWon = Enum.valueOf((Class<Enum>) eventType.asSubclass(Enum.class), "BATTLE_WON");
            Object quickResolved = Enum.valueOf((Class<Enum>) eventType.asSubclass(Enum.class), "QUICK_RESOLVED");

            Object majorProgress = progressType.getConstructor(profileType).newInstance(major);
            assertEquals(false, complete.invoke(majorProgress));
            apply.invoke(majorProgress, battleWon);
            assertEquals(true, complete.invoke(majorProgress));

            Object quickProgress = progressType.getConstructor(profileType).newInstance(quick);
            assertEquals(false, complete.invoke(quickProgress));
            apply.invoke(quickProgress, quickResolved);
            assertEquals(true, complete.invoke(quickProgress));
        });
    }
}
