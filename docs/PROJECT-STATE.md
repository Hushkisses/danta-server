# Danta Server — PROJECT STATE
Updated: 2026-09-15
Checkpoint: DEV-072 COMPLETE but execution-plan alignment correction required before DEV-073; DEV-072A Trait/Ability supplement next

## Source of truth
- Game design: Minecraft 단타 서버 기획서 v0.3
- Execution plan: 개발실행계획 v1.0
- This file records the implementation/test state that differs from or supplements those documents.

## Fixed development environment
- Paper target: 26.2 build 123 (project lock from DEV-001)
- Java/JDK: 25
- Build: Gradle multi-module
- DB: PostgreSQL
- Modules: core / paper-plugin / combat-simulator
- Local workflow: preserve the existing dev-server runtime folder; use dev-server/quick-deploy.bat for normal code changes.

## Verified tickets
- DEV-001 version lock: COMPLETE
- DEV-002 Gradle multi-module: COMPLETE
- DEV-003 Paper dev server: COMPLETE, real server boot verified
- DEV-004 bootstrap + /danta: COMPLETE, real server verified
- DEV-005A Git/config/log baseline: COMPLETE
- DEV-010 RuntimeClockService: COMPLETE
- DEV-011 runtime persistence: COMPLETE
- DEV-012 runtime dev commands: COMPLETE
- DEV-013 GameState: COMPLETE
- DEV-014 Domain Event Bus: COMPLETE
- DEV-015 Runtime Scheduler: COMPLETE
- DEV-016 PostgreSQL/async repository: COMPLETE, READY/ping/put/get verified
- DEV-017 snapshot/important flush: COMPLETE, restart recovery verified
- DEV-020 Nation: COMPLETE, restart restore verified
- DEV-021 StrategicPoint: COMPLETE, restart restore verified
- DEV-022 StrategicEdge: COMPLETE, real server verified
- DEV-023 territory/ownership event path: COMPLETE, validation and transfer verified
- DEV-024 /국가 minimal GUI: COMPLETE, real client verified
- DEV-024.1 Korean UI display layer: COMPLETE
- DEV-025 /지도 minimal GUI: COMPLETE, real client verified
- DEV-MAP-001 10-point logical test map: COMPLETE
- DEV-MAP-002 structure/template placement pipeline: COMPLETE, live server verified
- DEV-030 Army domain: COMPLETE, Windows build and restart recovery verified
- DEV-031 ArmyOrder/Route: COMPLETE, Windows build and Paper command checks verified
- DEV-031.1 player-facing Korean output: COMPLETE, Windows build and Paper command checks verified
- DEV-032 movement-time calculation: COMPLETE, Windows build and Paper command checks verified
- DEV-033 runtime movement scheduling/restart recovery: COMPLETE, Windows arrival and restart-mid-movement recovery verified
- DEV-034 sequential operation queue: COMPLETE, Windows multi-leg and restart-mid-route recovery verified
- DEV-035 advance-stop conditions: COMPLETE, Windows major-point stop and restart persistence verified
- DEV-036 minimal army GUI: COMPLETE, Windows client GUI verified
- DEV-040 basic troop types: COMPLETE, Windows build/test and Paper boot verified
- DEV-041 CombatResolver v0: COMPLETE, Windows build/test and Paper boot verified
- Execution-plan DEV-042 loss/retreat minimum model: COMPLETE, Windows simulator values verified
- DEV-043 NPC basic garrison: COMPLETE, Windows build/test and Paper boot verified
- DEV-044 combat-to-occupation: COMPLETE, Windows build/test and Paper boot verified
- DEV-045 combat report v0: COMPLETE, live Paper Korean report output verified
- DEV-050 treasury/personal-wallet separation: COMPLETE, Windows contribution/rejection/restart recovery verified
- DEV-051 strategic resources: COMPLETE, Windows five-resource display/set/restart recovery verified
- DEV-052 EconomyTick: COMPLETE, Windows runtime boundary/pause/restart behavior verified
- DEV-053 strategic-point production: COMPLETE, Windows exact production/restart persistence verified
- DEV-054 local stockpile/supply isolation: COMPLETE, Windows isolation/reconnection/restart persistence verified
- DEV-055 army food/supply consumption baseline: COMPLETE, Windows automated + stationed/moving EconomyTick verification passed
- DEV-056 expedition supply selection: COMPLETE, Windows automated/Paper/restart verification passed
- DEV-057 overextension/administrative demand v0: COMPLETE, Windows automated/Paper overextension + GOLD revenue verification passed
- DEV-060 CombatResolver v1: COMPLETE, Windows automated tests/build and Paper boot verified
- DEV-061 counter/frontline/backline/mobile: COMPLETE, Windows automated tests/build and Paper boot verified
- DEV-062 morale/retreat/pursuit: COMPLETE, Windows automated tests/build and Paper boot verified
- DEV-063 terrain/battlefield capacity: COMPLETE, Windows automated tests/build and Paper boot verified
- DEV-064 supply/isolation/retreat-route: COMPLETE, Windows automated tests/build and Paper boot verified
- DEV-065 repeated combat simulation: COMPLETE, Windows automated tests/build and Paper boot verified
- DEV-066 CSV combat balance report: COMPLETE, Windows automated tests/build and Paper boot verified after quote-escaping compile fix
- DEV-070 General F-S + Lv1-10: COMPLETE, Windows automated tests/build and Paper boot verified
- DEV-071 General stats baseline: COMPLETE, Windows automated tests/build and Paper boot verified; NOTE execution plan names the four axes 통솔/무력/지략/병참 while current implementation followed design v0.3 wording 통솔/무력/지력/정치. Reconciliation remains open.
- DEV-072 troop synergy foundation: COMPLETE, Windows automated tests/build and Paper boot verified; NOTE execution-plan DEV-072 is Trait/Ability, so this work is retained as a reusable sub-foundation and DEV-072A must complete the missing Trait/Ability scope.

## Implemented tickets awaiting live verification
- None

## Important implementation decisions
- All player-facing text (GUI, chat messages, warnings, rejection reasons, and command feedback) defaults to Korean.
- Internal IDs/enums/log diagnostics remain English; new features must add `UiText` mappings before exposing domain values to players.
- Strategic-point ownership changes go through TerritoryService and emit domain events.
- Important ownership changes trigger immediate snapshot flush.
- Runtime is server-running-time based; server downtime does not advance it.
- DB work is asynchronous; GameState remains authoritative in memory during play.
- Snapshot schema v10 adds local strategic-resource stockpiles after national stockpiles; legacy v1-v9 snapshots remain readable.
- Downloaded server.jar/world/EULA/runtime plugin state are local assets and are not replaced by normal source updates.
- Git excludes DB credentials and generated runtime state; .gitattributes defines line-ending policy.
- DEV-MAP-001 logical map data lives in `paper-plugin/src/main/resources/maps/dev-test-map.yml`.
- DEV-030 intentionally stores a single base-troop count; troop-type composition remains DEV-040.
- DEV-033 persists active one-leg movement orders and runtime deadlines; DEV-034 extends snapshot schema to v7 for remaining sequential destinations. Legacy v1-v6 snapshots remain readable.
- DEV-034 intentionally uses explicit adjacent waypoints; automatic shortest/safe/stealth pathfinding remains a later expansion of the broader design.
- DEV-035 fully wires major-point automatic stopping. Combat/supply stop hooks are typed now but wait for DEV-040+ combat and DEV-055+ supply state before live integration.
- DEV-032 route modifiers: ROAD x0.90, MOUNTAIN_PASS x1.20, FOREST_PATH x1.10, CANYON x1.15, SEA_ROUTE x0.90, LANDING_ROUTE x1.25; PLAIN/COASTAL neutral. Future authoritative commander/research/supply modifiers layer onto the calculator instead of being guessed now.

## Current test data (local dev server only)
Known examples include nation red, nation blue, strategic point farm_a, strategic point capital_red, and edge road_1. Exact local values live in the representative's PostgreSQL snapshot and are not source-controlled.

## Open items
1. Player-to-nation membership is not yet authoritative; /국가 currently supports development selection/direct opening.
2. RuntimeScheduler task-queue persistence is not yet a general persisted queue; domain-specific movement persistence must satisfy DEV-033.
3. General balance values unresolved by design v0.3: stat upper range, F-S base-stat distribution, Lv1-10 stat growth, exact command/martial/intelligence/politics effect coefficients, and fixed/random/selectable growth method.
4. General persistence is not yet wired into snapshot state; add it when generals first become persistent player-owned season state, preserving legacy snapshot compatibility.
5. General troop synergy unresolved by design: assignment per general, single vs multiple affinities, effect category/magnitude/scaling, origin layer (innate/trait/unique/equipment), and future magic-support synergy.
6. Execution-plan alignment correction: DEV-071 is named 통솔/무력/지략/병참, while current GeneralStats followed design v0.3 terminology 통솔/무력/지력/정치. Do not silently rename/remove either interpretation; reconcile against the source documents before final player-facing/stat-effect integration.
7. Execution-plan DEV-072 requires Trait/Ability. Existing DEV-072 troop synergy is retained as a reusable affinity foundation, but does not by itself satisfy the full Trait/Ability ticket; complete as DEV-072A before DEV-073.
8. Execution-plan DEV-073 is 군단 지휘관 배치/이동, not equipment slots.
9. Design v0.3 defines general equipment slots (weapon/armor/treasure); this remains a required later feature, but the current execution plan does not assign it DEV-073. Do not lose or misnumber it.

## Next execution order
1. DEV-072A Trait/Ability supplement and source-alignment documentation
2. DEV-073 army commander assignment/movement
3. Later ticket: general equipment slots (weapon/armor/treasure), ticket number to be assigned without colliding with execution plan

## Automated verification baseline
- DEV-TEST-001: `dev-server/quick-deploy.bat` now runs the Gradle `test` task before Paper JAR deployment; failed automated tests block deploy.
- Core-domain DEV tests should be added whenever behavior can be verified without a Minecraft client.
- Live Paper/client checks remain for integration, restart persistence, GUI, and player-visible behavior that unit tests cannot prove.

## Manual verification baseline
A checkpoint is healthy if:
- Paper server boots without fatal Danta errors.
- PostgreSQL reports READY.
- /danta runtime status advances only while server is running.
- /국가 opens Korean nation GUI.
- /지도 opens Korean strategic-point/edge GUI.
- Nation/StrategicPoint/StrategicEdge data survives restart.

## Handoff rule for a new chat
Use the linked GitHub repository as the latest source. Read this PROJECT-STATE first, inspect existing implementation before modifying a ticket, and use design v0.3 + execution plan v1.0 for product intent. Do not reconstruct old source from chat prose when current repository files are available.
