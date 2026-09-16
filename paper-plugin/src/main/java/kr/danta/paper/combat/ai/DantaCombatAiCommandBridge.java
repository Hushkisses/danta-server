package kr.danta.paper.combat.ai;

import java.util.Arrays;

/** DEV-114 bridge from /danta subcommands into CombatAI validation runtime. */
public final class DantaCombatAiCommandBridge {
    private final CombatAiValidationRuntime validationRuntime = new CombatAiValidationRuntime();

    public Result execute(String[] dantaArgs) {
        if (dantaArgs == null || dantaArgs.length == 0 || !"combat-ai".equalsIgnoreCase(dantaArgs[0])) {
            return new Result(false, "");
        }

        String[] combatAiArgs = Arrays.copyOfRange(dantaArgs, 1, dantaArgs.length);
        return new Result(true, validationRuntime.execute(combatAiArgs));
    }

    public record Result(boolean handled, String message) {
        public Result {
            if (message == null) throw new NullPointerException("message");
        }
    }
}
