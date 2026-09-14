# Danta Server — PROJECT STATE
Updated: 2026-09-14
Checkpoint: Phase 2 functional systems after DEV-025

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
- Local development workflow: keep the existing dev-server folder and overlay PATCH zips; use dev-server/quick-deploy.bat for normal code changes.

## Verified tickets
- DEV-001 version lock: COMPLETE
- DEV-002 Gradle multi-module: COMPLETE
- DEV-003 Paper dev server: COMPLETE, real server boot verified
- DEV-004 bootstrap + /danta: COMPLETE, real server verified
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

## Important implementation decisions
- Internal IDs/enums remain English; player-facing GUI text is Korean through UiText.
- Strategic-point ownership changes go through TerritoryService and emit domain events.
- Important ownership changes trigger immediate snapshot flush.
- Runtime is server-running-time based; server downtime does not advance it.
- DB work is asynchronous; GameState remains authoritative in memory during play.
- Snapshot schema has evolved with backward compatibility through the current territory/edge model.
- The development server's downloaded server.jar/world/EULA/config are local runtime assets and should NOT be replaced by normal PATCH zips.

## Current test data (local dev server; not production content)
Known examples used during manual testing include nation `red`, nation `blue`, strategic point `farm_a`, strategic point `capital_red`, and edge `road_1`. Treat these as development fixtures only; exact local values live in the representative's PostgreSQL snapshot.

## Open items discovered during checkpoint
1. DEV-005 (Git/config/log basic structure) was not formally completed in ticket order. Do not silently mark it complete. Add repository/checkpoint hygiene before project complexity grows further.
2. Phase 2 execution plan also contains DEV-MAP-001 (10-point playable strategy map design) and DEV-MAP-002 (structure/template auto-placement pipeline). These remain PENDING. DEV-020~025 functional nation/territory systems are complete, but the full Phase 2 including map/building tickets is not yet complete.
3. Player-to-nation membership is not yet authoritative; /국가 currently supports selection/direct nation opening for development.
4. RuntimeScheduler task-queue persistence is not yet a general persisted queue; domain-specific movement persistence must satisfy DEV-033.

## Next execution order
Before Phase 3 army complexity grows:
1. DEV-005A: initialize Git/repository hygiene and .gitignore without changing runtime behavior.
2. DEV-MAP-001: define the 10-point logical test strategy map.
3. DEV-MAP-002: implement minimal structure/template placement pipeline sufficient for test-map construction.
4. DEV-030: Army domain.
5. Continue DEV-031 through DEV-036 per execution plan.

## Manual verification baseline
A checkpoint is considered healthy if:
- Paper server boots without fatal Danta errors.
- PostgreSQL reports READY.
- `/danta runtime status` advances only while server is running.
- `/국가` opens Korean nation GUI.
- `/지도` opens Korean strategic-point/edge GUI.
- Existing Nation/StrategicPoint/StrategicEdge data survives restart.

## Handoff rule for a new chat
Provide the latest full project source (or Git repository snapshot), this PROJECT-STATE.md, design v0.3, and execution plan v1.0. The next assistant must read PROJECT-STATE first, then inspect existing source before modifying a ticket. Never reconstruct old source from chat prose when the actual latest files are available.
