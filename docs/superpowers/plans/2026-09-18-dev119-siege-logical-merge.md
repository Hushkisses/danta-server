# DEV-119 Siege Logical Merge Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Persist authoritative troop-type composition in armies and merge live siege AI casualties back into that composition without losing per-type losses.

**Architecture:** ArmyState becomes the authoritative owner of a TroopType→count composition while keeping baseTroops synchronized for compatibility. DEV-117 Mapping remains the projection contract; DEV-119 adds a core merge service and a live-result snapshot contract. Snapshot schema advances to v21 and legacy army rows migrate baseTroops to INFANTRY.

**Tech Stack:** Java 25, Gradle 9.7.1, JUnit 5, Paper 26.2 build 123.

**Spec:** `docs/superpowers/specs/2026-09-18-dev119-siege-logical-merge-design.md`

## Global Constraints

- All player-facing text remains Korean.
- Do not hard-code DEV-117's provisional 100:1 ratio inside DEV-119 merge logic.
- Legacy snapshot schemas v1-v20 remain readable.
- No partial ArmyState mutation on failed merge validation.
- Active siege/live runtime persistence remains DEV-120 scope.
- Do not invent army-disband behavior.

---

### Task 1: Authoritative Army Troop Composition

**Files:**
- Modify: `core/src/main/java/kr/danta/core/army/ArmyState.java`
- Test: `core/src/test/java/kr/danta/core/army/Dev119ArmyTroopCompositionTest.java`

**Interfaces:**
- Produces: `troopCount(TroopType)`, `troopComposition()`, `totalTroops()`, `replaceTroopComposition(Map<TroopType,Long>)`.
- Existing baseTroops-only constructors remain source compatible and initialize INFANTRY-only composition.

- [ ] Write tests for legacy INFANTRY migration, per-type composition, synchronized total/baseTroops, negative rejection, and overflow rejection.
- [ ] Verify the tests fail because composition APIs do not exist.
- [ ] Implement composition storage and atomic validation/replacement.
- [ ] Verify the task tests pass.

### Task 2: DEV-119 Core Casualty Merge

**Files:**
- Create: `core/src/main/java/kr/danta/core/combat/LogicalArmyCasualtyMergeService.java`
- Create: `core/src/main/java/kr/danta/core/combat/LogicalArmyCasualtyMergeResult.java`
- Test: `core/src/test/java/kr/danta/core/combat/Dev119LogicalArmyCasualtyMergeTest.java`

**Interfaces:**
- Consumes: `ArmyState`, `LogicalForceAiMappingPolicy`, `LogicalForceAiMappingPolicy.Mapping`, survivor counts by `TroopType`.
- Produces: before/after/losses maps and total loss.

- [ ] Write tests for asymmetric per-type loss, full survival, zero survival, stale mapping rejection, and no mutation on validation failure.
- [ ] Verify tests fail because merge service does not exist.
- [ ] Implement validation-first atomic merge.
- [ ] Verify task tests pass.

### Task 3: Snapshot v21 Troop Composition

**Files:**
- Modify: `core/src/main/java/kr/danta/core/snapshot/ArmySnapshot.java`
- Modify: `core/src/main/java/kr/danta/core/snapshot/GameSnapshot.java`
- Modify: `core/src/main/java/kr/danta/core/snapshot/GameSnapshotCodec.java`
- Modify: `paper-plugin/src/main/java/kr/danta/paper/persistence/SnapshotService.java`
- Test: `core/src/test/java/kr/danta/core/snapshot/Dev119ArmyCompositionSnapshotTest.java`

**Interfaces:**
- New ArmySnapshot includes troop composition.
- v21 army rows serialize composition.
- v1-v20 army rows decode as INFANTRY=baseTroops.

- [ ] Write round-trip and legacy-v20 migration tests.
- [ ] Verify tests fail against schema v20/current army codec.
- [ ] Increment CURRENT_SCHEMA to 21 and extend army codec while accepting 5/7-field legacy rows.
- [ ] Capture/restore exact troop composition in SnapshotService.
- [ ] Verify task tests pass.

### Task 4: Live Mapped Battle Result Contract

**Files:**
- Create: `paper-plugin/src/main/java/kr/danta/paper/combat/live/LiveMappedBattleResult.java`
- Modify: `paper-plugin/src/main/java/kr/danta/paper/combat/live/LiveCombatRuntime.java`
- Modify: `paper-plugin/src/main/java/kr/danta/paper/combat/live/PaperLiveCombatHooks.java`
- Test: `paper-plugin/src/test/java/kr/danta/paper/combat/live/Dev119LiveMappedBattleResultTest.java`

**Interfaces:**
- Runtime retains per-side DEV-117 Mapping for a mapped battle.
- Runtime exposes a snapshot containing initial mapping and surviving representative counts by troop type.
- Result collection must not depend on a 100:1 constant.

- [ ] Write tests for side/type survivor aggregation and ratio independence.
- [ ] Verify tests fail because result contract does not exist.
- [ ] Implement mapped-session metadata and result snapshot.
- [ ] Verify task tests pass.

### Task 5: Documentation State

**Files:**
- Modify: `docs/PROJECT-STATE.md`

- [ ] Record DEV-119 as IMPLEMENTED pending Windows GREEN/live verification.
- [ ] Record schema v21 and legacy INFANTRY migration.
- [ ] Keep DEV-120 as next target after DEV-119 validation.
