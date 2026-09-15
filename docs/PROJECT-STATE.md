# Danta Server — PROJECT STATE
Updated: 2026-09-15
Checkpoint: DEV-045 combat report v0 implemented; awaiting Windows build/test verification

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

## Implemented tickets awaiting live verification
- DEV-045 combat report v0: IMPLEMENTED
  - Projects CombatResolution into result/initial/loss/remaining values for both sides.
  - Player-facing formatter is Korean: 승리/패배/무승부, 전투 전/손실/잔존.
  - No history DB, GUI, broadcast, or Discord scope is invented beyond the execution-plan requirement.
  - Awaiting representative's Windows build/test and normal Paper boot verification.

## Important implementation decisions
- All player-facing text (GUI, chat messages, warnings, rejection reasons, and command feedback) defaults to Korean.
- Internal IDs/enums/log diagnostics remain English; new features must add `UiText` mappings before exposing domain values to players.
- Strategic-point ownership changes go through TerritoryService and emit domain events.
- Important ownership changes trigger immediate snapshot flush.
- Runtime is server-running-time based; server downtime does not advance it.
- DB work is asynchronous; GameState remains authoritative in memory during play.
- Snapshot schema remains backward-compatible through the current army model.
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

## Next execution order
1. DEV-045 Windows build/test verification
2. DEV-050 treasury/personal-wallet separation

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
