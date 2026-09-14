# Danta Server — PROJECT STATE
Updated: 2026-09-14
Checkpoint: DEV-MAP-002 structure/template pipeline implemented

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
- DEV-MAP-002 structure/template placement pipeline: IMPLEMENTED, live test pending

## Important implementation decisions
- Internal IDs/enums remain English; player-facing GUI text is Korean through UiText.
- Strategic-point ownership changes go through TerritoryService and emit domain events.
- Important ownership changes trigger immediate snapshot flush.
- Runtime is server-running-time based; server downtime does not advance it.
- DB work is asynchronous; GameState remains authoritative in memory during play.
- Snapshot schema remains backward-compatible through the current territory/edge model.
- Downloaded server.jar/world/EULA/runtime plugin state are local assets and are not replaced by normal source updates.
- Git excludes DB credentials and generated runtime state; .gitattributes defines line-ending policy.
- DEV-MAP-001 logical map data lives in `paper-plugin/src/main/resources/maps/dev-test-map.yml`.

## Current test data (local dev server only)
Known examples include nation red, nation blue, strategic point farm_a, strategic point capital_red, and edge road_1. Exact local values live in the representative's PostgreSQL snapshot and are not source-controlled.

## Open items
1. DEV-MAP-002 requires representative-side live server verification.
2. Player-to-nation membership is not yet authoritative; /국가 currently supports development selection/direct opening.
3. RuntimeScheduler task-queue persistence is not yet a general persisted queue; domain-specific movement persistence must satisfy DEV-033.

## Next execution order
1. Verify DEV-MAP-002 live on the representative's dev server.
2. DEV-030 Army domain
3. DEV-031 ArmyOrder/Route
4. DEV-032 movement-time calculation
5. DEV-033 runtime movement scheduling/restart recovery
6. DEV-034 sequential operation queue
7. DEV-035 advance-stop conditions
8. DEV-036 army GUI

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
