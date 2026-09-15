# DEV-072A — Trait / Ability supplement

Status: IMPLEMENTED — Windows automated verification pending

## Why this supplement exists
Execution plan v1.0 assigns DEV-072 to Trait/Ability. The previously completed DEV-072 implemented troop synergy, which is useful general-affinity infrastructure but does not by itself satisfy the execution-plan scope. DEV-072A closes that gap without deleting the verified synergy work.

## Implemented
- GeneralTrait: validated internal trait identity.
- GeneralAbility: validated internal ability identity.
- GeneralState owns ordered, duplicate-safe trait and ability collections.
- Traits/abilities can be added, queried and removed by internal ID.
- Returned collections are immutable snapshots.
- Existing troop synergy remains separate rather than being falsely equated with every trait/ability.

## Deliberately not invented
The source documents establish that Trait/Ability exists but do not provide a final authoritative catalog or exact mechanics for every trait/ability. Therefore DEV-072A does not invent:
- a final trait catalog;
- a final ability catalog;
- attack/defense percentage values;
- activation probability;
- cooldown/duration;
- resource cost;
- grade/level/stat unlock thresholds;
- stacking priority;
- active-vs-passive classification where not explicitly defined.

Concrete entries should be data-driven when those design decisions are made.

## Relationship to troop synergy
GeneralTroopSynergy can later be used by a concrete trait/ability effect, but it remains an independent typed affinity contract. This avoids hard-coding every troop affinity as a trait before the final catalog is decided.

## Automated tests
Tests cover empty defaults, add/remove, duplicate IDs, immutable ordered snapshots and absence of invented effect fields.

## Windows verification
1. Stop Paper.
2. Pull main.
3. Run `dev-server\quick-deploy.bat`.
4. Confirm all automated tests pass and deployment completes.
5. Start `dev-server\start-dev.bat` and confirm normal Paper/Danta/PostgreSQL boot.

DEV-072A is core-only and introduces no player-facing command. Keep IMPLEMENTED until Windows verification succeeds.
