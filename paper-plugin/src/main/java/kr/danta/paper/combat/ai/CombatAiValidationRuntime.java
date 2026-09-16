package kr.danta.paper.combat.ai;

import java.util.Locale;

import kr.danta.core.combat.TroopType;
import kr.danta.core.combat.ai.CombatAiAction;
import kr.danta.core.combat.ai.CombatAiDecision;

/** DEV-114 deterministic validation runtime for /danta combat-ai commands. */
public final class CombatAiValidationRuntime {
    private static final String ROOT_USAGE =
            "사용법: /danta combat-ai profiles 또는 /danta combat-ai decide <병종> <시나리오>";
    private static final String DECIDE_USAGE =
            "사용법: /danta combat-ai decide <병종> <시나리오>";

    private final PaperCombatAiRuntime runtime = new PaperCombatAiRuntime();

    public String execute(String[] args) {
        if (args == null || args.length == 0) return ROOT_USAGE;

        String operation = args[0].toLowerCase(Locale.ROOT);
        return switch (operation) {
            case "profiles" -> profiles();
            case "decide" -> decide(args);
            default -> ROOT_USAGE;
        };
    }

    private String profiles() {
        return "CombatAI 병종: 보병, 창병, 궁병, 기병, 마법병";
    }

    private String decide(String[] args) {
        if (args.length < 3) return DECIDE_USAGE;

        String profileToken = args[1].toLowerCase(Locale.ROOT);
        String scenarioToken = args[2].toLowerCase(Locale.ROOT);

        TroopType troopType = parseTroopType(profileToken);
        if (troopType == null) return "알 수 없는 병종입니다: " + args[1];

        PaperCombatAiObservationFactory.TacticalSnapshot snapshot = scenario(scenarioToken);
        if (snapshot == null) return "알 수 없는 전투 시나리오입니다: " + args[2];

        CombatAiDecision decision = runtime.decide(troopType, snapshot);
        return troopLabel(troopType) + " 판단: " + actionLabel(decision.action());
    }

    private static TroopType parseTroopType(String token) {
        return switch (token) {
            case "infantry" -> TroopType.INFANTRY;
            case "spearmen" -> TroopType.SPEARMEN;
            case "archers" -> TroopType.ARCHERS;
            case "cavalry" -> TroopType.CAVALRY;
            case "magic" -> TroopType.MAGIC;
            default -> null;
        };
    }

    private static PaperCombatAiObservationFactory.TacticalSnapshot scenario(String token) {
        return switch (token) {
            case "frontline" -> snapshot(2.5, TroopType.INFANTRY, true, false, false, false, false, false, true);
            case "cavalry-threat" -> snapshot(4.0, TroopType.CAVALRY, true, false, false, false, false, false, true);
            case "close-threat" -> snapshot(3.0, TroopType.CAVALRY, true, true, false, false, false, false, true);
            case "exposed-backline" -> snapshot(9.0, TroopType.ARCHERS, true, false, true, false, false, false, true);
            case "retreating-target" -> snapshot(9.0, TroopType.INFANTRY, true, false, false, true, false, false, true);
            case "support" -> snapshot(10.0, TroopType.INFANTRY, true, false, false, false, false, false, true);
            default -> null;
        };
    }

    private static PaperCombatAiObservationFactory.TacticalSnapshot snapshot(
            double distance,
            TroopType hostileType,
            boolean frontlineSupportPresent,
            boolean backlineThreatened,
            boolean exposedEnemyBackline,
            boolean hostileRetreating,
            boolean spearScreenPresent,
            boolean survivalThreatened,
            boolean alliedCombatGroupPresent
    ) {
        return new PaperCombatAiObservationFactory.TacticalSnapshot(
                distance,
                hostileType,
                frontlineSupportPresent,
                backlineThreatened,
                exposedEnemyBackline,
                hostileRetreating,
                spearScreenPresent,
                survivalThreatened,
                alliedCombatGroupPresent);
    }

    private static String troopLabel(TroopType type) {
        return switch (type) {
            case INFANTRY -> "보병";
            case SPEARMEN -> "창병";
            case ARCHERS -> "궁병";
            case CAVALRY -> "기병";
            case MAGIC -> "마법병";
        };
    }

    private static String actionLabel(CombatAiAction action) {
        return switch (action) {
            case HOLD -> "대기";
            case ADVANCE -> "전진";
            case ENGAGE -> "교전";
            case RETREAT -> "후퇴";
            case SCREEN -> "엄호/차단";
            case FLANK -> "우회";
            case PURSUE -> "추격";
            case SUPPORT -> "지원";
        };
    }
}
