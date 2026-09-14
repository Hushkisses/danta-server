package kr.danta.paper.ui;

import kr.danta.core.nation.NationStatus;
import kr.danta.core.territory.BattlefieldTag;
import kr.danta.core.territory.StrategicPointType;

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
}
