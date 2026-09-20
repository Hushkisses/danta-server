# DEV-120 Siege Snapshot / Recovery Design

Date: 2026-09-20

## Goal

Persist and recover logical siege state across server restarts without serializing live Paper entities.

## Persisted state

Snapshot schema v22 stores one SiegeRuntimeSnapshot aggregate containing:
- SiegeInstance logical lifecycle: siegeId, pointId, attackerNationId, defenderNationId, phase, result.
- DEV-111 wall-clock reservation: siegeId, scheduledAt epoch millis, confirmedAt epoch millis nullable.
- DEV-113 progress bridge: pointId, engagement profile, current stage, resumeRequired.
- DEV-118 participants: pointId, player UUID, side, eliminated, previous game mode name nullable.
- DEV-118 accumulated morale penalty by point and side.

## Recovery rules

- v1-v21 snapshots decode with an empty siege runtime snapshot.
- Core SiegeInstance and SiegeReservationService support explicit restore paths that preserve phase/result/timestamps without replaying transitions.
- Paper progress restores the exact completed stage.
- An unfinished battle stage restores with resumeRequired=true. No live mob, projectile, location, health, or pathfinding state is serialized.
- A restored eliminated participant remains barred from the same siege. On login during an active restored siege, spectator restriction is re-applied.
- When the siege completes or is cleared, participant restrictions and saved original game modes are released.

## Live battle boundary

A restart never attempts to reconstruct an in-progress Minecraft entity fight. The authoritative logical stage survives; the current encounter is recreated from the start of that stage when resumed.

DEV-119 casualty results that have already been merged into ArmyState are naturally durable through snapshot v21+ army composition. DEV-120 adds an important-flush hook after siege-runtime mutations; exact future automatic merge orchestration remains outside this ticket.

## Production wiring

DantaPlugin owns:
- SiegeService
- SiegeReservationService using the existing DEV-111 provisional 2h lead / 30m confirmation policy
- DantaSiegeRuntime

SnapshotService binds these services and the Paper runtime, captures them on every normal/important snapshot, and restores them after nations/strategic points exist.

The DEV-111 policy remains provisional and is not final balance.

## Snapshot encoding

GameSnapshot.CURRENT_SCHEMA becomes 22.
Schema v22 adds one final encoded field for SiegeRuntimeSnapshot.
Schemas v1-v21 remain readable and produce SiegeRuntimeSnapshot.empty().

## Tests

Cover:
- SiegeInstance restore for CREATED/SCHEDULED/ACTIVE/RESOLVED/CANCELLED.
- Reservation timestamps round-trip.
- Progress exact-stage restore and resumeRequired.
- Participant eliminated state + side + morale restore.
- v22 codec round-trip.
- v21 decode gives empty siege state.
- SnapshotService capture/apply restores core and Paper siege state.
