# DEV-076F — General acquisition boundary + persistence foundation

Status: COMPLETE — automated build/tests + Paper restart recovery + Korean rejection output verified

## Implemented
- GeneralAcquisitionService converts an unowned catalog GeneralDefinition into nation-owned GeneralState only after the calling content system has resolved eligibility/payment/reward.
- Unique general IDs cannot be acquired twice.
- GeneralSnapshot schema captures ownership, grade/level/stats, traits/abilities, army-command or point assignment, injury/recovery and captivity runtime state.
- Snapshot schema advances to v12; v1-v11 remain readable with an empty general list.
- SnapshotService captures/restores generals and restores commander/point assignments after armies/points/generals exist.

## Deliberately not invented
- recruitment GOLD price;
- spawn/reveal schedule;
- which A/S general comes from recruitment, raid, event, achievement or NPC minor state;
- nation starting ownership;
- final acquisition cap enforcement (design mentions early/late targets, but exact phase/cap policy is not yet authoritative).

Acquisition is therefore a common domain boundary, not a fake recruitment/raid implementation.

## Live verification support
Development-only admin commands now expose catalog acquisition, general inspection, strategic-point assignment and army-command assignment. All state-changing commands immediately request an important snapshot flush. This is verification tooling, not the final player recruitment UI/content.

## Final live verification
- ironwall restored after restart with owner red, A/Lv4 stats and forest_crossing assignment.
- duplicate acquisition remained blocked.
- representative verified the specific Korean duplicate-acquisition rejection after rebuild.
