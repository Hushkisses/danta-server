package kr.danta.paper.ui;

import kr.danta.core.army.ArmyOrderStatus;
import kr.danta.core.army.ArmyOrderType;
import kr.danta.core.army.ArmyStatus;
import kr.danta.paper.persistence.DatabaseStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UiTextTest {
    @Test
    void translatesPlayerVisibleArmyAndDatabaseStates() {
        assertEquals("주둔 중", UiText.armyStatus(ArmyStatus.STATIONED));
        assertEquals("이동 중", UiText.armyStatus(ArmyStatus.MOVING));
        assertEquals("전투 중", UiText.armyStatus(ArmyStatus.IN_BATTLE));
        assertEquals("이동", UiText.armyOrderType(ArmyOrderType.MOVE));
        assertEquals("대기 중", UiText.armyOrderStatus(ArmyOrderStatus.PENDING));
        assertEquals("정상", UiText.databaseStatus(DatabaseStatus.READY));
    }

    @Test
    void translatesKnownDomainRejectionReasonsWithoutExposingEnglishMessage() {
        assertEquals("해당 군단에는 이미 명령이 등록되어 있습니다.", UiText.playerError(
                new IllegalArgumentException("army already has an order: red_first")));
        assertEquals("목적지가 군단의 현재 위치와 인접하지 않습니다.", UiText.playerError(
                new IllegalArgumentException("destination is not adjacent to army location: a -> b")));
        assertEquals("군단이 주둔 중일 때만 이동 명령을 내릴 수 있습니다.", UiText.playerError(
                new IllegalStateException("army must be STATIONED to receive a move order")));
    }
}
