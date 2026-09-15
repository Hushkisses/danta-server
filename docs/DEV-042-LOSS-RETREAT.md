# Execution-plan DEV-042 — Loss / retreat minimum model

Status: IMPLEMENTED — awaiting Windows build/simulator verification

## Goal
Add the execution plan's minimum post-combat casualty and retreat result without pretending the final v0.3 casualty formula is already fixed.

## Temporary balance decision
Representative-approved prototype values on 2026-09-15:
- winner loss: 10%
- loser loss: 25%
- loser: retreat required
- draw: temporary symmetric 10% loss, no forced retreat

These are explicitly placeholders and must be revisited during the later full automatic-combat/balance phase. They are not promoted to final game-design truth.

## Implemented
- CombatOutcome: VICTORY / DEFEAT / DRAW.
- CombatLossResult: initial troops, losses, remaining troops, and retreat-required flag.
- CombatResolution combines DEV-041 power result with both sides' loss results.
- CombatLossPolicy centralizes all temporary rates so replacement does not require rewriting CombatResolver.
- Losses use Math.round and are capped at the initial troop count.
- The existing deterministic scenario runner now prints losses, survivors, and retreat flags.
- Unit tests cover victory/defeat, draw, rounding, and non-negative survivors.

## Scope boundary
- The design's eventual permanent-loss / wounded / deserter split is not invented here.
- Retreat destination/path execution is not added here; this ticket only emits retreatRequired.
- Encirclement, blocked retreat, cavalry pursuit, morale collapse, and annihilation modifiers belong to later full combat work.
- ArmyState persistence is not mutated by the pure core resolver.

## Windows verification
1. Stop Paper and pull main.
2. Run dev-server\\quick-deploy.bat; build/tests must succeed.
3. Run .\\.tools\\gradle-9.7.1\\bin\\gradle.bat :combat-simulator:run.
4. For infantry 1000 vs spearmen 1000, expect red winner with losses 100/250, remaining 900/750, retreat false/true.
5. Equal infantry 1000 vs 1000 should show draw, losses 100/100, remaining 900/900, retreat false/false.
6. Start the existing Paper dev server and confirm normal boot plus /danta db status = 정상.

Do not mark execution-plan DEV-042 COMPLETE until representative verification passes.
