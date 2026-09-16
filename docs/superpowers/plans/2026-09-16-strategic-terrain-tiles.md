# DEV-MAP-004 Strategic Terrain Tiles Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a reusable terrain-tile layout and deterministic Paper builder for the flat strategic mainland.

**Architecture:** Keep logical tile layout data separate from Bukkit rendering. Pure records/enums validate layout and orientation; `StrategicTerrainBuilder` renders only into `danta_main`. `DantaWorldRuntime` exposes admin-only status/build commands.

**Tech Stack:** Java 25, Paper 26.2 build 123, Gradle 9.7.1, JUnit 5

**Spec:** `docs/superpowers/specs/2026-09-16-strategic-terrain-tiles-design.md`

## Global Constraints

- Do not install/download Java, Gradle, Paper, or PostgreSQL in ChatGPT runtime.
- Player-facing text is Korean.
- Do not touch wilderness resource/economy behavior.
- Sample tile size/height is development-only, not final balance.
- Terrain generation is explicit admin setup, never destructive automatic startup work.

---

### Task 1: Pure terrain layout model

**Files:**
- Create `TerrainTileType.java`
- Create `TerrainTileSpec.java`
- Create `TerrainLayout.java`
- Create `DevTerrainLayouts.java`
- Test `TerrainLayoutTest.java`

- [ ] Define five base terrain types and quarter-turn orientation.
- [ ] Reject rotations outside 0..3 and duplicate tile coordinates.
- [ ] Provide a deterministic sample layout containing all five types.
- [ ] Verify with JUnit through the normal Windows quick-deploy test path.

### Task 2: Strategic mainland renderer

**Files:**
- Create `StrategicTerrainBuilder.java`

- [ ] Resolve `danta_main` from `DantaWorldService`.
- [ ] Render flat base, forest, mountain ridge, river channel, and road strip deterministically.
- [ ] Respect orientation for directional terrain.
- [ ] Load only chunks needed for explicit admin build.

### Task 3: Admin integration

**Files:**
- Modify `DantaWorldRuntime.java`

- [ ] Add `/danta world terrain-status`.
- [ ] Add OP/admin `/danta world build-terrain` using existing `danta.admin.map`.
- [ ] Keep existing world/guild commands unchanged.
- [ ] Use specific Korean success/failure output.

### Task 4: State documentation and Windows verification

**Files:**
- Modify `docs/PROJECT-STATE.md`

- [ ] Mark DEV-MAP-003 COMPLETE from representative live verification.
- [ ] Mark DEV-MAP-004 IMPLEMENTED pending Windows verification.
- [ ] Run `git pull` and `dev-server\quick-deploy.bat` on representative PC.
- [ ] Run `danta world terrain-status` then `danta world build-terrain`.
- [ ] Visually verify river/mountain/forest/road in `danta_main` and persistence after restart.
- [ ] Mark COMPLETE only after that live verification.
