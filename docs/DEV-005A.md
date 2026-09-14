# DEV-005A — Git / config / log baseline

## Goal
Close the previously skipped DEV-005 baseline without changing gameplay behavior.

## Completed
- GitHub repository connected and initial project checkpoint pushed.
- `.gitignore` excludes local server runtime data, worlds, downloaded Paper JAR, build caches, tool caches, and DB secrets.
- `.gitattributes` fixes cross-platform line-ending policy to avoid noisy LF/CRLF warnings and diffs.
- `docs/PROJECT-STATE.md` is the handoff source of truth for ticket/test state.
- Development server configuration files that are useful for reproducibility remain versioned.
- Runtime/generated plugin state and local credentials remain untracked.

## Local-only / never commit
- `dev-server/server.jar`
- `dev-server/world/`, `world_nether/`, `world_the_end/`
- `dev-server/plugins/DantaServer/database.properties`
- `dev-server/plugins/DantaServer/runtime.properties`
- `dev-server/.paper/`
- `.tools/`, `.gradle/`, `**/build/`

## Logging baseline
Paper/server logs are local runtime artifacts under `dev-server/logs/` and are intentionally ignored. Danta currently uses Paper's plugin logger for structured subsystem messages such as database readiness, snapshot recovery, and territory events. Domain-specific debug categories will be expanded in Phase 13.

## Definition of Done
- Repository can be cloned without secrets or local runtime state.
- Build/server scripts and reproducible config are tracked.
- Normal Windows development does not require committing generated server files.
- Latest implementation state is recorded in `docs/PROJECT-STATE.md`.

Status: COMPLETE
