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
        if (message.startsWith("nation not found")) return "해당 국가를 찾을 수 없습니다.";
        if (message.startsWith("strategic edge not found")) return "해당 전략 간선을 찾을 수 없습니다.";
        if (message.contains("already exists")) return "같은 식별자를 사용하는 데이터가 이미 존재합니다.";
        if (message.startsWith("Usage:")) return message.replace("Usage:", "사용법:");
        return "요청을 처리할 수 없습니다. 입력값을 확인해 주세요.";
    }
}
