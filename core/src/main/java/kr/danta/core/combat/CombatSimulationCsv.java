package kr.danta.core.combat;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** DEV-066 pure CSV formatter for combat balance summaries. */
public final class CombatSimulationCsv {
    public static final String HEADER =
            "scenario_id,runs,first_wins,second_wins,draws,first_win_rate,second_win_rate,draw_rate,avg_first_losses,avg_second_losses";

    private CombatSimulationCsv() {}

    public static String format(List<CombatSimulationSummary> summaries) {
        Objects.requireNonNull(summaries, "summaries");
        StringBuilder out = new StringBuilder(HEADER).append('\n');
        for (CombatSimulationSummary s : summaries) {
            Objects.requireNonNull(s, "summary");
            out.append(escape(s.scenarioId())).append(',')
                    .append(s.runs()).append(',')
                    .append(s.firstWins()).append(',')
                    .append(s.secondWins()).append(',')
                    .append(s.draws()).append(',')
                    .append(decimal(s.firstWinRate())).append(',')
                    .append(decimal(s.secondWinRate())).append(',')
                    .append(decimal(s.drawRate())).append(',')
                    .append(decimal(s.averageFirstLosses())).append(',')
                    .append(decimal(s.averageSecondLosses())).append('\n');
        }
        return out.toString();
    }

    private static String decimal(double value) {
        return String.format(Locale.ROOT, "%.6f", value);
    }

    private static String escape(String value) {
        if (value.indexOf(',') < 0 && value.indexOf('"') < 0 && value.indexOf('\n') < 0 && value.indexOf('\r') < 0) {
            return value;
        }
        return '"' + value.replace(""", """") + '"';
    }
}
