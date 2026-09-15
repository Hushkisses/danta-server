package kr.danta.core.combat;

public record CombatResolution(
        CombatResult powerResult,
        CombatLossResult first,
        CombatLossResult second
) {}
