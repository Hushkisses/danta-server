# DEV-076C — Strategic-point general assignment foundation

Status: IMPLEMENTED — Windows automated verification pending

## Product decisions fixed 2026-09-15
- General special abilities are not limited to direct combat effects; A/S abilities may affect the strategic point where the general is assigned.
- A strategic point may have at most one assigned general.
- Army-command assignment and strategic-point assignment are separate roles.
- Current rule: they are mutually exclusive; a general commanding an army does not automatically provide point administration effects.

## Implemented
- StrategicPoint optionally references one assigned general.
- GameState resolves a general's assigned point.
- PointGeneralAssignmentService supports assign/move/unassign.
- Assignment requires a point owned by the general's nation.
- Captive generals cannot be point-assigned.
- One general per point, one point per general.
- Army commanders cannot simultaneously be point-assigned.

## Deliberately unresolved
- Independent travel time/cost for moving a non-army general between strategic points.
- Exact internal-affairs effect types and numeric coefficients.
- Whether injury blocks point administration or merely reduces effects.
- What happens to an assigned general when point ownership changes.
- Snapshot persistence, to be integrated with authoritative general persistence.

## Important
This ticket establishes assignment state only. It does not yet apply production/tax/administrative modifiers. Those effects belong to the approved A/S ability definitions and should hook existing economy/administrative services rather than duplicate them.
