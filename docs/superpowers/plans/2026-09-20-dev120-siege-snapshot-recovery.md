# DEV-120 Siege Snapshot Recovery Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans or subagent-driven-development.

**Goal:** Persist and restore logical siege, reservation, progression and commander-participant state across restart while never serializing live entities.

**Architecture:** Add restore APIs to core siege services and a compact SiegeRuntimeSnapshot aggregate to GameSnapshot v22. SnapshotService binds the core siege services and DantaSiegeRuntime; the Paper runtime exports/imports progress and participant state and marks unfinished encounters as resume-required.

**Tech Stack:** Java 25, Gradle 9.7.1, JUnit 5, Paper 26.2 build 123, PostgreSQL snapshot repository.

**Spec:** docs/superpowers/specs/2026-09-20-dev120-siege-snapshot-recovery-design.md

## Global Constraints
- Keep v1-v21 snapshot compatibility.
- Do not serialize live Bukkit/Paper entities.
- Preserve wall-clock reservation instants.
- Restored eliminated commanders cannot re-enter the same siege.
- Existing DEV-111 2h/30m timing remains provisional.
- Player-facing output remains Korean.

### Task 1: Core siege restore contracts
- Add tests for SiegeInstance and reservation restoration.
- Add explicit restore/clear/list APIs without replaying lifecycle transitions.

### Task 2: Snapshot v22 model/codec
- Add SiegeInstanceSnapshot, SiegeReservationSnapshot, SiegeProgressSnapshot, SiegeParticipantSnapshot, SiegeMoraleSnapshot and SiegeRuntimeSnapshot.
- Advance GameSnapshot to v22.
- Encode/decode the aggregate as a final field.
- Verify v21 decodes to empty siege state.

### Task 3: Paper progression/participant export-import
- Add exact-stage restore to SiegeProgress/PaperSiegeProgressRuntime.
- Add participant registry snapshot/restore APIs.
- Add DantaSiegeRuntime export/import and resume-required status.

### Task 4: SnapshotService and plugin wiring
- Bind SiegeService, SiegeReservationService and DantaSiegeRuntime.
- Capture/apply v22 siege state.
- Bootstrap DantaSiegeRuntime explicitly and idempotently from DantaPlugin.
- Use existing DEV-111 provisional policy for the service.

### Task 5: Project state
- Record DEV-120 IMPLEMENTED pending Windows quick-deploy/restart verification.
