package kr.danta.paper.siege;

import kr.danta.core.territory.StrategicPointType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class Dev113PaperSiegeBridgeTest {
    @Test
    void profileSelectionFollowsPointImportance() {
        assertDoesNotThrow(() -> {
            Class<?> runtimeType = Class.forName("kr.danta.paper.siege.PaperSiegeProgressRuntime");
            Object runtime = runtimeType.getConstructor().newInstance();
            Method profileFor = runtimeType.getMethod("profileFor", StrategicPointType.class);

            assertEquals("CAPITAL_THREE_BATTLE", ((Enum<?>) profileFor.invoke(runtime, StrategicPointType.CAPITAL)).name());
            assertEquals("MAJOR_SINGLE_BATTLE", ((Enum<?>) profileFor.invoke(runtime, StrategicPointType.MAJOR)).name());
            assertEquals("NORMAL_QUICK", ((Enum<?>) profileFor.invoke(runtime, StrategicPointType.FARM)).name());
            assertEquals("NORMAL_QUICK", ((Enum<?>) profileFor.invoke(runtime, StrategicPointType.GATE)).name());
        });
    }

    @Test
    void capitalAdminEventsAdvanceThreeBattleFlowAndExposeKoreanStatus() {
        assertDoesNotThrow(() -> {
            Class<?> runtimeType = Class.forName("kr.danta.paper.siege.PaperSiegeProgressRuntime");
            Object runtime = runtimeType.getConstructor().newInstance();
            Method start = runtimeType.getMethod("start", String.class, StrategicPointType.class);
            Method battleWon = runtimeType.getMethod("recordBattleWin", String.class);
            Method gateBreached = runtimeType.getMethod("recordGateBreach", String.class);
            Method status = runtimeType.getMethod("status", String.class);

            start.invoke(runtime, "capital_red", StrategicPointType.CAPITAL);
            assertTrue(((String) status.invoke(runtime, "capital_red")).contains("외성 전투"));

            battleWon.invoke(runtime, "capital_red");
            assertTrue(((String) status.invoke(runtime, "capital_red")).contains("외성 성문 파괴"));
            gateBreached.invoke(runtime, "capital_red");
            assertTrue(((String) status.invoke(runtime, "capital_red")).contains("광장 전투"));
            battleWon.invoke(runtime, "capital_red");
            assertTrue(((String) status.invoke(runtime, "capital_red")).contains("내성문 파괴"));
            gateBreached.invoke(runtime, "capital_red");
            assertTrue(((String) status.invoke(runtime, "capital_red")).contains("핵심 전투"));
            battleWon.invoke(runtime, "capital_red");
            assertTrue(((String) status.invoke(runtime, "capital_red")).contains("공성 완료"));
        });
    }

    @Test
    void invalidAdminEventReturnsSpecificKoreanRejection() {
        assertDoesNotThrow(() -> {
            Class<?> runtimeType = Class.forName("kr.danta.paper.siege.PaperSiegeProgressRuntime");
            Object runtime = runtimeType.getConstructor().newInstance();
            Method start = runtimeType.getMethod("start", String.class, StrategicPointType.class);
            Method gateBreached = runtimeType.getMethod("recordGateBreach", String.class);

            start.invoke(runtime, "capital_red", StrategicPointType.CAPITAL);
            Throwable thrown = assertThrows(Throwable.class, () -> gateBreached.invoke(runtime, "capital_red"));
            Throwable cause = thrown.getCause() == null ? thrown : thrown.getCause();
            assertInstanceOf(IllegalStateException.class, cause);
            assertTrue(cause.getMessage().contains("현재 단계에서는 성문을 파괴할 수 없습니다"));
        });
    }
}
