# DEV-119 Siege Result → Logical Army Merge Design

Date: 2026-09-18

## 1. Goal

DEV-119 connects live siege combat results back into authoritative strategic army state without losing troop-type-specific casualties.

The authoritative flow becomes:

ArmyState troop composition
→ DEV-117 logical-force/live-AI mapping
→ live combat
→ surviving live AI by troop type
→ projected logical survivors by troop type
→ ArmyState troop composition update
→ persisted snapshot.

## 2. Core decision

ArmyState will gain authoritative troop-type composition using TroopType keys.

Existing ArmyState.baseTroops remains temporarily for backward compatibility with existing services and snapshots, but it must represent the sum of authoritative troop composition once composition is present.

New code must not treat baseTroops as an independent second source of truth.

## 3. Legacy migration

Legacy snapshots contain only baseTroops and have no troop-type information.

When restoring a legacy army without troop composition:
- INFANTRY = legacy baseTroops
- SPEARMEN = 0
- ARCHERS = 0
- CAVALRY = 0
- MAGIC = 0

This is a compatibility migration only. It does not claim historical armies were actually all infantry.

The current snapshot schema must be incremented. Older schema versions remain readable.

## 4. ArmyState invariants

ArmyState exposes:
- troopCount(TroopType)
- troopComposition()
- totalTroops()
- replaceTroopComposition(Map<TroopType, Long>)

Rules:
- every troop count is >= 0
- missing troop types mean zero
- totalTroops is the exact sum of all troop types
- baseTroops remains synchronized to totalTroops
- overflow is rejected
- empty/zero composition is allowed; DEV-119 does not invent army disband rules

Existing constructors that only receive baseTroops remain supported and initialize the army as INFANTRY-only for compatibility.

A new composition-aware constructor/factory is added for new code.

## 5. Siege result merge

Create a Paper-independent DEV-119 merger in core.

Inputs:
- authoritative ArmyState
- the DEV-117 Mapping used when that army entered live combat
- surviving live AI counts by TroopType

Behavior:
1. Validate that the mapping logical counts match the army composition captured for the battle.
2. Use LogicalForceAiMappingPolicy.projectLogicalSurvivors(...) to calculate survivors per troop type.
3. Atomically replace the ArmyState troop composition with those survivors.
4. Return a result object containing before/after/losses per troop type and total loss.

The merger must not hard-code the temporary 100:1 ratio. It consumes the Mapping created by whatever configured DEV-117 policy is in use.

## 6. Live combat result contract

DEV-117 currently spawns representatives but does not retain enough mapping metadata to merge a specific army result back.

DEV-119 extends the live mapped battle session contract so the runtime retains, per side:
- source logical mapping
- live representative troop types
- surviving representative counts by troop type

The runtime exposes a result snapshot when the mapped battle ends or is explicitly resolved.

DEV-119 does not yet decide strategic siege ownership/result resolution; that remains the existing siege flow / later integration. Its responsibility is troop casualty merge.

## 7. Snapshot compatibility

ArmySnapshot gains troop composition data.

The snapshot schema version increments from the current version.

Serialization requirements:
- new snapshots write troop composition
- restore of new snapshots reconstructs exact troop composition
- restore of old snapshots migrates baseTroops to INFANTRY
- existing expedition supply and commander-related fields continue to work
- old snapshot readers are not required to read the new schema; only current code must read old schemas

## 8. Failure handling

Reject merge when:
- survivor AI count exceeds the spawned representative count
- mapping logical composition no longer matches the army battle-start composition
- negative counts are supplied
- arithmetic overflows

No partial ArmyState mutation is allowed on validation failure.

## 9. Testing

Core automated tests must cover:
- legacy ArmyState constructor migrates total troops to INFANTRY
- composition constructor preserves each troop type
- total/baseTroops synchronization
- invalid negative/overflow composition rejection
- DEV-119 per-type merge with asymmetric losses
- full-survival exact preservation
- zero-survival troop type
- stale/mismatched mapping rejection without mutation
- snapshot new-schema round trip
- legacy snapshot restore → INFANTRY-only migration

Paper tests must cover:
- mapped live session counts survivors by side and troop type
- result contract remains independent of the configured 100:1 value

## 10. Scope exclusions

DEV-119 does not:
- choose final live-AI mapping ratio
- implement army disbanding
- decide siege ownership transfer
- persist active siege/live-AI runtime state (DEV-120)
- finalize morale-stage conversion for DEV-118
- add player→nation authoritative membership
