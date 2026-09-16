package kr.danta.paper.ui;

import kr.danta.core.army.ArmyOrderStatus;
import kr.danta.core.army.ArmyOrderType;
import kr.danta.core.army.ArmyStatus;
import kr.danta.core.nation.NationStatus;
import kr.danta.core.territory.BattlefieldTag;
import kr.danta.core.territory.StrategicPointType;
import kr.danta.paper.persistence.DatabaseStatus;

/**
 * Player-facing Korean labels for internal domain enums.
 *
 * <p>Internal identifiers remain stable English enum/ID values for code, DB, snapshots, and admin
 * tooling. Player-facing GUI code must translate through this class instead of exposing raw enum
 * names. DEV-025 and later GUIs should reuse this layer.</p>
 */
public final class UiText {
    private UiText() {}

    public static String nationStatus(NationStatus status) {
        if (status == null) return "알 수 없음";
        return switch (status) {
            case ACTIVE -> "독립국";
            case VASSAL -> "속국";
        };
    }

    public static String strategicPointType(StrategicPointType type) {
        if (type == null) return "알 수 없음";
        return switch (type) {
            case CAPITAL -> "수도";
            case FARM -> "농업 거점";
            case FORESTRY -> "임업 거점";
            case MINE -> "광업 거점";
            case COMMERCIAL -> "상업 거점";
            case ACADEMIC -> "학술 거점";
            case BARRACKS -> "병영 거점";
            case PORT -> "항구";
            case GATE -> "관문";
            case MAJOR -> "주요 거점";
        };
    }

    public static String battlefieldTag(BattlefieldTag tag) {
        if (tag == null) return "알 수 없음";
        return switch (tag) {
            case PLAIN -> "평야";
            case ROAD -> "도로";
            case MOUNTAIN_PASS -> "산악로";
            case FOREST_PATH -> "숲길";
            case CANYON -> "협곡";
            case COASTAL -> "해안";
            case SEA_ROUTE -> "해상로";
            case LANDING_ROUTE -> "상륙로";
        };
    }

    public static String armyStatus(ArmyStatus status) {
        if (status == null) return "알 수 없음";
        return switch (status) {
            case STATIONED -> "주둔 중";
            case MOVING -> "이동 중";
            case IN_BATTLE -> "전투 중";
        };
    }

    public static String armyOrderType(ArmyOrderType type) {
        if (type == null) return "알 수 없음";
        return switch (type) {
            case MOVE -> "이동";
        };
    }

    public static String armyOrderStatus(ArmyOrderStatus status) {
        if (status == null) return "알 수 없음";
        return switch (status) {
            case PENDING -> "대기 중";
        };
    }

    public static String databaseStatus(DatabaseStatus status) {
        if (status == null) return "알 수 없음";
        return switch (status) {
            case DISABLED -> "사용 안 함";
            case CONNECTING -> "연결 중";
            case READY -> "정상";
            case DEGRADED -> "연결 불안정";
            case CLOSED -> "종료됨";
        };
    }

    public static String playerError(Throwable error) {
        if (error instanceof NumberFormatException) return "숫자 입력 형식이 올바르지 않습니다.";
        String message = error == null || error.getMessage() == null ? "" : error.getMessage();
        if (message.startsWith("minimum subordination period not elapsed")) return "아직 최소 종속기간이 지나지 않아 독립전쟁을 선언할 수 없습니다.";
        if (message.startsWith("independence war already active")) return "이미 독립전쟁이 진행 중입니다.";
        if (message.startsWith("independence war redeclare cooldown active")) return "독립전쟁 재선언 대기시간이 아직 끝나지 않았습니다.";
        if (message.startsWith("vassal does not control own capital")) return "자국 수도를 확보한 상태에서만 독립전쟁을 선언할 수 있습니다.";
        if (message.startsWith("vassal cannot form alliance")) return "속국은 독자적으로 혈맹을 체결할 수 없습니다. 종속 관계를 먼저 해소해야 합니다.";
        if (message.startsWith("vassal cannot declare war on overlord")) return "속국은 종주국에 일반 전쟁을 선포할 수 없습니다. 독립전쟁은 별도의 독립 절차를 통해서만 가능합니다.";
        if (message.startsWith("vassal cannot independently support-join war")) return "속국은 독자적으로 다른 국가의 전쟁에 지원 참전할 수 없습니다.";
        if (message.startsWith("nation is not vassal")) return "해당 국가는 현재 속국이 아닙니다.";
        if (message.startsWith("nation is already vassal")) return "이미 속국인 국가입니다.";
        if (message.startsWith("nation has no designated capital")) return "해당 국가에 지정된 수도가 없어 속국화할 수 없습니다.";
        if (message.startsWith("designated capital point does not exist")) return "지정된 수도 거점을 찾을 수 없습니다.";
        if (message.startsWith("capital is not controlled by victor")) return "종주국이 패배국의 수도를 점령한 상태가 아니므로 속국화할 수 없습니다.";
        if (message.startsWith("vassal and overlord must differ")) return "자기 자신을 종주국으로 지정할 수 없습니다.";
                if (message.startsWith("npc nation is not independent")) return "독립 상태인 NPC 소국만 새 동맹을 맺을 수 있습니다.";
        if (message.startsWith("npc nation is annexed")) return "이미 합병된 NPC 소국에는 이 외교 행동을 할 수 없습니다.";
        if (message.startsWith("annexed npc requires restoration system")) return "합병된 NPC 소국은 단순 명령으로 독립시킬 수 없습니다. 복국 절차가 필요합니다.";
        if (message.startsWith("npc cannot target itself")) return "NPC 소국은 자기 자신과 동맹·복속·합병 관계를 맺을 수 없습니다.";
        if (message.startsWith("unknown npc political action")) return "알 수 없는 NPC 외교 행동입니다.";
                if (message.startsWith("nation is already npc controlled")) return "이미 NPC 국가로 등록된 국가입니다.";
        if (message.startsWith("nation is not npc controlled")) return "NPC 국가로 등록되지 않은 국가입니다.";
        if (message.startsWith("npc strategic ai phase must be")) return "현재 NPC 전략 AI 상태에서는 해당 단계로 전환할 수 없습니다.";
        if (message.startsWith("unknown npc step")) return "알 수 없는 NPC 전략 AI 단계 명령입니다.";
                if (message.startsWith("cannot declare war on alliance")) return "혈맹 관계인 국가에는 전쟁을 선포할 수 없습니다. 먼저 혈맹 관계를 해제해야 합니다.";
        if (message.startsWith("cannot join against alliance")) return "해당 전쟁에 참전하면 현재 혈맹국과 적대하게 되므로 참전할 수 없습니다.";
        if (message.startsWith("nation already participates")) return "해당 국가는 이미 이 전쟁에 참전 중입니다.";
        if (message.startsWith("war not found")) return "해당 전쟁을 찾을 수 없습니다.";
        if (message.startsWith("nation does not exist")) return "해당 국가를 찾을 수 없습니다.";
        if (message.startsWith("self relation is not allowed")) return "같은 국가끼리는 외교 관계를 설정할 수 없습니다.";
                if (message.startsWith("army already has an order")) return "해당 군단에는 이미 명령이 등록되어 있습니다.";
        if (message.startsWith("destination is not adjacent")) return "목적지가 군단의 현재 위치와 인접하지 않습니다.";
        if (message.startsWith("army is already at destination")) return "군단이 이미 해당 목적지에 있습니다.";
        if (message.startsWith("army must be stationed before loading expedition supply")) return "군단이 주둔 중일 때만 출정 보급을 적재할 수 있습니다.";
        if (message.startsWith("not enough food for expedition supply")) return "출정 보급을 적재하기 위한 국가 식량이 부족합니다.";
        if (message.startsWith("army must be STATIONED")) return "군단이 주둔 중일 때만 이동 명령을 내릴 수 있습니다.";
        if (message.startsWith("operation queue must contain at least one destination")) return "연속 작전에는 목적지가 하나 이상 필요합니다.";
        if (message.startsWith("operation route contains duplicate current point")) return "연속 작전 경로에 현재 위치와 같은 거점이 연속으로 포함되어 있습니다.";
        if (message.startsWith("operation route contains non-adjacent leg")) return "연속 작전 경로에는 서로 직접 연결되지 않은 거점이 포함되어 있습니다.";
        if (message.startsWith("army has no current order")) return "해당 군단에는 현재 이동 명령이 없습니다.";
        if (message.startsWith("army location no longer matches order origin")) return "군단 위치가 이동 명령의 출발지와 일치하지 않습니다.";
        if (message.startsWith("movement order route no longer matches strategic edge")) return "이동 명령의 경로 정보가 현재 전략 간선과 일치하지 않습니다.";
        if (message.startsWith("order belongs to another army")) return "이동 명령과 군단 정보가 일치하지 않습니다.";
        if (message.startsWith("order is not a movement order")) return "현재 명령은 이동시간을 계산할 수 있는 이동 명령이 아닙니다.";
        if (message.startsWith("army not found")) return "해당 군단을 찾을 수 없습니다.";
        if (message.startsWith("strategic point not found")) return "해당 전략 거점을 찾을 수 없습니다.";
        if (message.startsWith("general is already owned")) return "이미 다른 국가가 소유하고 있는 장수입니다.";
        if (message.startsWith("general not found")) return "해당 장수를 찾을 수 없습니다.";
        if (message.startsWith("catalog general not found")) return "해당 장수는 현재 장수 후보 목록에 없습니다.";
        if (message.startsWith("nation not found")) return "해당 국가를 찾을 수 없습니다.";
        if (message.startsWith("strategic edge not found")) return "해당 전략 간선을 찾을 수 없습니다.";
        if (message.contains("already exists")) return "같은 식별자를 사용하는 데이터가 이미 존재합니다.";
        if (message.startsWith("Usage:")) return message.replace("Usage:", "사용법:");
        return "요청을 처리할 수 없습니다. 입력값을 확인해 주세요.";
    }
}
