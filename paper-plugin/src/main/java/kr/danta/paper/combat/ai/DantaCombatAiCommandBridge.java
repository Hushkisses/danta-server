package kr.danta.paper.combat.ai;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/** DEV-114/115 bridge from /danta subcommands into CombatAI validation and demo routing. */
public final class DantaCombatAiCommandBridge {
    private final CombatAiValidationRuntime validationRuntime = new CombatAiValidationRuntime();

    public Result execute(String[] dantaArgs) {
        if (dantaArgs == null || dantaArgs.length == 0 || !"combat-ai".equalsIgnoreCase(dantaArgs[0])) {
            return new Result(false, "");
        }

        Optional<DemoAction> demoAction = parseDemoAction(dantaArgs);
        if (demoAction.isPresent()) {
            return new Result(true, demoMessage(demoAction.orElseThrow()));
        }

        String[] combatAiArgs = Arrays.copyOfRange(dantaArgs, 1, dantaArgs.length);
        return new Result(true, validationRuntime.execute(combatAiArgs));
    }

    public Optional<DemoAction> parseDemoAction(String[] dantaArgs) {
        if (dantaArgs == null || dantaArgs.length != 3) return Optional.empty();
        if (!"combat-ai".equalsIgnoreCase(dantaArgs[0]) || !"demo".equalsIgnoreCase(dantaArgs[1])) {
            return Optional.empty();
        }

        return switch (dantaArgs[2].toLowerCase(Locale.ROOT)) {
            case "start" -> Optional.of(DemoAction.START);
            case "stop" -> Optional.of(DemoAction.STOP);
            case "status" -> Optional.of(DemoAction.STATUS);
            default -> Optional.empty();
        };
    }

    private static String demoMessage(DemoAction action) {
        return switch (action) {
            case START -> "§eCombatAI 전투 시연 시작 요청";
            case STOP -> "§eCombatAI 전투 시연 종료 요청";
            case STATUS -> "§eCombatAI 전투 시연 상태 요청";
        };
    }

    public enum DemoAction {
        START,
        STOP,
        STATUS
    }

    public record Result(boolean handled, String message) {
        public Result {
            if (message == null) throw new NullPointerException("message");
        }
    }
}
