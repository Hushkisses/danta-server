# Danta Server — PROJECT STATE
Updated: 2026-09-15
Checkpoint: DEV-098 COMPLETE; DEV-100/DEV-101 IMPLEMENTED awaiting Windows verification

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
- DEV-MAP-002 structure/template placement pipeline: COMPLETE, live server verified
- DEV-030 Army domain: COMPLETE, Windows build and restart recovery verified
- DEV-031 ArmyOrder/Route: COMPLETE, Windows build and Paper command checks verified
- DEV-031.1 player-facing Korean output: COMPLETE, Windows build and Paper command checks verified
- DEV-032 movement-time calculation: COMPLETE, Windows build and Paper command checks verified
- DEV-033 runtime movement scheduling/restart recovery: COMPLETE, Windows arrival and restart-mid-movement recovery verified
- DEV-034 sequential operation queue: COMPLETE, Windows multi-leg and restart-mid-route recovery verified
- DEV-035 advance-stop conditions: COMPLETE, Windows major-point stop and restart persistence verified
- DEV-036 minimal army GUI: COMPLETE, Windows client GUI verified
- DEV-040 basic troop types: COMPLETE, Windows build/test and Paper boot verified
- DEV-041 CombatResolver v0: COMPLETE, Windows build/test and Paper boot verified
- Execution-plan DEV-042 loss/retreat minimum model: COMPLETE, Windows simulator values verified
- DEV-043 NPC basic garrison: COMPLETE, Windows build/test and Paper boot verified
- DEV-044 combat-to-occupation: COMPLETE, Windows build/test and Paper boot verified
- DEV-045 combat report v0: COMPLETE, live Paper Korean report output verified
- DEV-050 treasury/personal-wallet separation: COMPLETE, Windows contribution/rejection/restart recovery verified
- DEV-051 strategic resources: COMPLETE, Windows five-resource display/set/restart recovery verified
- DEV-052 EconomyTick: COMPLETE, Windows runtime boundary/pause/restart behavior verified
- DEV-053 strategic-point production: COMPLETE, Windows exact production/restart persistence verified
- DEV-054 local stockpile/supply isolation: COMPLETE, Windows isolation/reconnection/restart persistence verified
- DEV-055 army food/supply consumption baseline: COMPLETE, Windows automated + stationed/moving EconomyTick verification passed
- DEV-056 expedition supply selection: COMPLETE, Windows automated/Paper/restart verification passed
- DEV-057 overextension/administrative demand v0: COMPLETE, Windows automated/Paper overextension + GOLD revenue verification passed
- DEV-060 CombatResolver v1: COMPLETE, Windows automated tests/build and Paper boot verified
- DEV-061 counter/frontline/backline/mobile: COMPLETE, Windows automated tests/build and Paper boot verified
- DEV-062 morale/retreat/pursuit: COMPLETE, Windows automated tests/build and Paper boot verified
- DEV-063 terrain/battlefield capacity: COMPLETE, Windows automated tests/build and Paper boot verified
- DEV-064 supply/isolation/retreat-route: COMPLETE, Windows automated tests/build and Paper boot verified
- DEV-065 repeated combat simulation: COMPLETE, Windows automated tests/build and Paper boot verified
- DEV-066 CSV combat balance report: COMPLETE, Windows automated tests/build and Paper boot verified after quote-escaping compile fix
- DEV-070 General F-S + Lv1-10: COMPLETE, Windows automated tests/build and Paper boot verified
- DEV-071 General stats baseline: COMPLETE, Windows automated tests/build and Paper boot verified; NOTE execution plan names the four axes 통솔/무력/지략/병참 while current implementation followed design v0.3 wording 통솔/무력/지력/정치. Reconciliation remains open.
- DEV-072 troop synergy foundation: COMPLETE, Windows automated tests/build and Paper boot verified; NOTE execution-plan DEV-072 is Trait/Ability, so this work is retained as a reusable sub-foundation and DEV-072A must complete the missing Trait/Ability scope.
- DEV-076 initial elite general data/acquisition-persistence foundation: COMPLETE, A×7/S×3 catalog + Paper restart recovery + duplicate-acquisition Korean rejection verified.
- DEV-080 Facility I~III / slots: COMPLETE, Windows quick-deploy automated tests/build and Paper boot verified.
- DEV-081 construction Runtime Scheduler: COMPLETE, Windows automated tests/build and live Paper construction/restart recovery verified.
- DEV-083 ResearchDefinition data loader: COMPLETE, Windows quick-deploy automated tests/build verified.
- DEV-084 ResearchState/Queue: COMPLETE. Windows quick-deploy, Paper boot, FIFO reservation, runtime pause, restart-mid-research recovery, automatic next-research start, completion persistence and Snapshot v14 recovery verified. dev084_alpha/beta remain verification-only provisional definitions, not final content.
- DEV-085 four-field/prerequisite eligibility: COMPLETE, Windows quick-deploy automated tests/build verified. Prerequisites are explicit ResearchDefinition data; queued/active research does not satisfy them.
- DEV-086 doctrine slots 2→max3: COMPLETE, Windows quick-deploy automated tests/build verified. National doctrine capacity defaults to 2 and may expand to max 3; slot-3 unlock condition and doctrine-change balance remain unresolved by design.
- DEV-087 major-point-loss high-research deactivation: COMPLETE, Windows quick-deploy automated tests/build verified. Completed research remains learned; requiredMajorPointType effects derive active/inactive state from authoritative point ownership and reactivate on recovery.
- DEV-088 development sample research tree: COMPLETE, Windows quick-deploy automated tests/build and Paper boot verified. 4 fields × Tier 1-5 sample remains provisional development content, not final season balance.
- DEV-090 friendly/alliance/war relationship state: COMPLETE, Windows quick-deploy/Paper command/restart persistence verified. Symmetric bilateral state, Korean dev output, Snapshot v16 with v1-v15 compatibility.
- DEV-091 support participation/direct-alliance automatic war entry: COMPLETE, Windows quick-deploy/Paper war declaration/list verification passed. Direct alliance declaration rejection verified. Specific Korean diplomacy rejection messages were added after live verification exposed the generic fallback.
- DEV-092 passage/supply rights: COMPLETE, Windows quick-deploy/Paper access verification passed for NEUTRAL/FRIENDLY/ALLIANCE, and improved Korean alliance-war rejection verified.
- DEV-093 NPC Nation/StrategicAI state machine: COMPLETE, Windows quick-deploy/Paper lifecycle, invalid-transition, duplicate-register and unregister verification passed.
- DEV-094 NPC alliance/subjugation/annexation: COMPLETE, Windows live verification passed: alliance/subjugation preserved NPC territory and annexation transferred all 7 red-owned strategic points to blue through TerritoryService.
- DEV-095 capital fall -> vassal: COMPLETE. Windows live verification passed: capital-control prerequisite rejection is specific Korean text; successful vassalization preserves remaining territory; Snapshot schema v17 restart recovery preserved red -> blue vassal relation, 10 strategic points, and nation states. During verification, snapshot restore-order bugs were fixed and empty GameState snapshot overwrite protection + previous-active backup were added.
- DEV-097 independence war: COMPLETE. Windows quick-deploy/Paper/restart verification passed. Verified minimum-subordination lock and specific Korean rejection, own-capital prerequisite, explicit player declaration, active-war duplicate rejection, restart persistence of active defense timer/vassal state/capital ownership, successful release to independent nation while preserving territory, immediate failure on capital loss, and provisional 30m redeclare cooldown with specific Korean rejection. Design direction: independence is an optional nation objective/quest unlocked by conditions rather than an automatic war; eligible players choose whether/when to declare. Provisional values remain configurable and are not final balance. DEV-098 third-country independence support remains separate.

## Implemented tickets awaiting live verification
- DEV-101 verification hotfix: IMPLEMENTED. Live boundary test exposed that manually setting diplomacy back to NEUTRAL did not remove the existing WarService record, allowing a second war for the same nation pair. WarService now rejects any new war (either direction, including independence declaration) while an active war exists between the pair. Added automated same-direction/reverse-direction regression tests and specific Korean rejection text. Added admin `/danta diplomacy war-end <전쟁-id>` using WarService.endWar so live/test cleanup removes the authoritative war record and opposing WAR relations together. Windows quick-deploy/Paper verification pending.
- DEV-101 war/capital-siege phase unlock: IMPLEMENTED from design v0.3 pacing. Rules: player-vs-player war locked before 4h; general conflict allowed 4h+; major-point battle/siege unlocked at 8h; capital siege unlocked at 20h; new major-point battle declarations locked at 48h; all new actions locked at 50h while already-running battles are intentionally not cancelled. Existing `/danta diplomacy war` is now gated by the 4h rule. Major/capital siege gates are domain-ready for Phase 11 SiegeInstance integration rather than inventing a duplicate siege system now. Korean rejection messages and automated boundary tests added. Windows quick-deploy/Paper verification pending.
- DEV-100 SeasonPhase: IMPLEMENTED. Execution plan v1.0 has no DEV-099; Phase 10 begins at DEV-100. Added runtime-derived phases 0~8h 초반 / 8~20h 확장기 / 20~38h 쟁패기 / 38~50h 최종전 / 50h+ 시즌 종료. The service reads existing RuntimeClockService only and adds no second clock or persistence. `/danta season` exposes Korean status. Automated boundary tests added; Windows quick-deploy/Paper verification pending.
- DEV-098B official third-country independence-war participation: COMPLETE. Windows quick-deploy/Paper live verification passed: pre-declaration join rejected; declaration created a WarService war ID; green joined red's independence side; duplicate and overlord joins were rejected with specific Korean reasons; successful independence removed the active war and restored red↔blue and green↔blue to NEUTRAL.
- DEV-098A third-country pre-independence material support: COMPLETE. Windows quick-deploy/Paper live verification passed. Verified green -> red treasury GOLD transfer (1000→800 / 382→582), FOOD transfer (500→400 / 100→200), IRON transfer (100→80 / 60→80), vassal relation remained red -> blue, existing failed-war cooldown remained unchanged, and support did not auto-declare or bypass independence. Overlord support, insufficient treasury, insufficient strategic resource, and self-support were all rejected with specific Korean reasons. DEV-098B post-declaration official military support remains to implement using existing DEV-091 WarService participation rules rather than duplicating war logic.
- DEV-096 tribute/subordination restrictions: COMPLETE. Windows quick-deploy/Paper live verification passed: vassal state recovered, provisional 15% treasury-revenue tribute status displayed with personal wallets excluded, overlord passage/vassal supply denial worked, and vassal alliance + ordinary war against overlord were rejected with specific Korean reasons. DEV-097 independence timing/war remains separate.
- DEV-082 facility world appearance sync: IMPLEMENTED; vanilla/Paper NBT templates, pendingVisualSync on unloaded chunks, chunk-load retry, construction/snapshot-restore reconciliation. Windows quick-deploy and Paper boot verified. Live NBT placement/upgrade verification is deferred until representative requests/authors building NBT assets; do not mark COMPLETE before that verification.

## Important implementation decisions
- All player-facing text (GUI, chat messages, warnings, rejection reasons, and command feedback) defaults to Korean.
- Internal IDs/enums/log diagnostics remain English; new features must add `UiText` mappings before exposing domain values to players.
- Strategic-point ownership changes go through TerritoryService and emit domain events.
- Important ownership changes trigger immediate snapshot flush.
- Runtime is server-running-time based; server downtime does not advance it.
- DB work is asynchronous; GameState remains authoritative in memory during play.
- Snapshot schema v12 adds owned general state after local strategic-resource stockpiles; legacy v1-v11 snapshots remain readable.
- Downloaded server.jar/world/EULA/runtime plugin state are local assets and are not replaced by normal source updates.
- Git excludes DB credentials and generated runtime state; .gitattributes defines line-ending policy.
- DEV-MAP-001 logical map data lives in `paper-plugin/src/main/resources/maps/dev-test-map.yml`.
- DEV-030 intentionally stores a single base-troop count; troop-type composition remains DEV-040.
- DEV-033 persists active one-leg movement orders and runtime deadlines; DEV-034 extends snapshot schema to v7 for remaining sequential destinations. Legacy v1-v6 snapshots remain readable.
- DEV-034 intentionally uses explicit adjacent waypoints; automatic shortest/safe/stealth pathfinding remains a later expansion of the broader design.
- DEV-035 fully wires major-point automatic stopping. Combat/supply stop hooks are typed now but wait for DEV-040+ combat and DEV-055+ supply state before live integration.
- DEV-032 route modifiers: ROAD x0.90, MOUNTAIN_PASS x1.20, FOREST_PATH x1.10, CANYON x1.15, SEA_ROUTE x0.90, LANDING_ROUTE x1.25; PLAIN/COASTAL neutral. Future authoritative commander/research/supply modifiers layer onto the calculator instead of being guessed now.

## Current test data (local dev server only)
Known examples include nation red, nation blue, strategic point farm_a, strategic point capital_red, and edge road_1. Exact local values live in the representative's PostgreSQL snapshot and are not source-controlled.

## Open items
1. Player-to-nation membership is not yet authoritative; /국가 currently supports development selection/direct opening.
2. RuntimeScheduler task-queue persistence is not yet a general persisted queue; domain-specific movement persistence must satisfy DEV-033.
3. General balance values unresolved by design v0.3: stat upper range, F/D/C/B/A/S base-stat distribution, Lv1-10 stat growth, exact command/martial/strategy/logistics effect coefficients, and fixed/random/selectable growth method.
4. General persistence foundation is wired in DEV-076F snapshot schema v12, preserving legacy v1-v11 reads. Live acquisition content/commands and restart verification remain before DEV-076 completion.
5. General troop synergy unresolved by design: assignment per general, single vs multiple affinities, effect category/magnitude/scaling, origin layer (innate/trait/unique/equipment), and future magic-support synergy.
6. Source alignment corrected on DEV-074 re-check: design v0.3 and execution plan use 통솔/무력/지략/병참; GeneralStats now matches command/martial/strategy/logistics. Earlier intelligence/politics wording was an implementation mistake.
7. Execution-plan DEV-072 requires Trait/Ability. Existing DEV-072 troop synergy is retained as a reusable affinity foundation; DEV-072A supplies the domain contract. Product rule fixed 2026-09-15: only A/S generals may possess special traits/abilities; F/D/C/B provide only ordinary combat-stat bonuses. Exact A/S ability catalog/effects remain unresolved.
8. Execution-plan DEV-073 is 군단 지휘관 배치/이동, not equipment slots.
9. Design v0.3 defines general equipment slots (weapon/armor/treasure); this remains a required later feature, but the current execution plan does not assign it DEV-073. Do not lose or misnumber it.
10. DEV-073 does not define independent unassigned-general travel time/cost; no teleport/travel queue was invented. Commander/general snapshot persistence must be added together when general persistence becomes authoritative.
11. DEV-074 injury balance remains unresolved: injury/severe-injury probabilities, exact recovery durations, stat/grade/equipment modifiers, and whether injured generals are unavailable or command with penalties.
12. DEV-075 final prisoner rules remain unresolved by v0.3: exact detention cap within the provisional 1.5–2 runtime-hour range, capture probability, ransom formula, exchange transaction/UX, and automatic repatriation-vs-escape determination. Captivity persistence + expiry scheduler must be added with authoritative general persistence.
13. DEV-076 initial elite roster composition fixed 2026-09-15: exactly 10 generals = A grade 7 + S grade 3. DEV-076D supplies PROVISIONAL identities/names/levels/stats/trait+ability IDs for iteration and Windows quick-deploy/Paper boot verification passed. These remain mutable balance/content values. Initial ownership/acquisition placement remains unresolved. F/D/C/B are outside this initial elite roster and have no special abilities.
14. Product rule fixed 2026-09-15: A/S special abilities may be combat, internal-affairs/strategic-point, or mixed. A strategic point may have at most one assigned general. Army command and point assignment are mutually exclusive; army presence does not automatically grant point administration effects.
15. Point-general assignment unresolved details: independent general travel time/cost, exact civil-effect categories/coefficients, injury interaction, ownership-loss behavior, and snapshot persistence.

## Next execution order
1. DEV-082 remains IMPLEMENTED with live NBT asset verification deferred; remind the representative when actual building NBT authoring/modification begins.
2. DEV-100 season display and DEV-101 4h boundary passed live verification, but duplicate-war hotfix awaits Windows verification. Pull/build, then end both existing test wars with `/danta diplomacy war-end <id>`, confirm `wars`=0, declare one red-blue war, manually set relation neutral, confirm a second declaration is rejected with the specific Korean duplicate-war reason, then end the remaining war. Restore runtime from the test 4h value to the pre-test ~25h16m baseline before marking DEV-100/101 COMPLETE. Major/capital siege gate integration waits for DEV-110+.

## Automated verification baseline
- DEV-TEST-001: `dev-server/quick-deploy.bat` now runs the Gradle `test` task before Paper JAR deployment; failed automated tests block deploy.
- Core-domain DEV tests should be added whenever behavior can be verified without a Minecraft client.
- Live Paper/client checks remain for integration, restart persistence, GUI, and player-visible behavior that unit tests cannot prove.

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
