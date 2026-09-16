# DEV-115 Waypoint Combat Runtime Design

Date: 2026-09-16
Status: APPROVED DIRECTION / IMPLEMENTATION PENDING

## 1. Goal

DEV-115 connects the DEV-114 CombatAI decision layer to visible Minecraft battlefield behavior.

Completion target:

- spawn visible battlefield troop entities for all five troop families
- move them through tactical waypoints
- execute visible melee/ranged combat and damage
- translate DEV-114 actions into visible movement/engagement behavior
- keep troop identity and combat logic independent from temporary vanilla entity appearance
- keep logical-force-to-entity-count mapping out of scope for DEV-117

The five troop families are:

- infantry
- spearmen
- archers
- cavalry
- magic troops

## 2. Existing invariants to preserve

- DEV-114 core CombatAI remains the authoritative tactical decision source.
- Paper code owns Bukkit/Paper entity creation, movement, target assignment and visual effects.
- Strategic combat rules remain in core; DEV-115 must not create a second Paper-only combat ruleset.
- DEV-113 siege progression remains independent from individual entity implementation.
- DEV-116 owns concurrent-AI performance benchmarking, beginning at 40 logical combat units and scaling only after measurement.
- DEV-117 owns mapping between logical troop strength/waves and live AI entity count.
- Player-facing command feedback remains Korean.
- Final balance values, model assets and magic spell catalog remain provisional/out of scope.

## 3. Runtime architecture

DEV-115 introduces a Paper-side live combat runtime layered on top of DEV-114.

Recommended responsibility split:

### 3.1 Combat unit runtime state

A Paper-side runtime object represents one live tactical combat unit. It stores only execution state needed for the live battlefield, for example:

- runtime unit id
- side/team id
- `TroopType`
- primary Bukkit entity id
- optional mount entity id for cavalry
- current tactical action from DEV-114
- current waypoint/route intent
- current hostile target id if applicable
- visual profile id

This state must not become the strategic source of truth for army size, ownership or persistent campaign state.

### 3.2 CombatAI execution adapter

A controller samples battlefield facts at a reduced cadence, feeds them into the existing `PaperCombatAiRuntime`, then translates the returned `CombatAiAction` into live intent.

Actions map to execution intents as follows:

- `HOLD`: stop deliberate advance and remain near the current tactical position
- `ADVANCE`: move toward the next forward waypoint or hostile line
- `ENGAGE`: close to attack range and attack the selected hostile
- `RETREAT`: move toward a designated friendly retreat waypoint and disengage when possible
- `SCREEN`: occupy a protective/intercept waypoint between threat and friendly backline
- `FLANK`: follow a side waypoint route instead of directly crossing the enemy frontline
- `PURSUE`: follow a retreating hostile with a pursuit route/target
- `SUPPORT`: remain in a rear support position near a valid allied combat group

DEV-115 does not change the decision rules themselves unless a correctness defect is demonstrated through tests.

### 3.3 Waypoint layer

Waypoint movement is tactical and battlefield-authored, not a new global shortest-path system.

A battlefield route is composed from a small set of named tactical anchors such as:

- forward/frontline waypoint
- left flank waypoint(s)
- right flank waypoint(s)
- backline/support waypoint
- retreat waypoint
- pursuit/target approach waypoint

The runtime selects among these routes according to CombatAI actions. Local Minecraft navigation between nearby anchors may use Paper/vanilla navigation primitives, but units must not independently perform expensive full-battlefield path searches every tick.

The route model should be data-oriented enough that later siege maps can supply different waypoint layouts without changing CombatAI code.

## 4. Hybrid navigation strategy

DEV-115 uses a hybrid movement model.

- Danta determines the tactical destination and route.
- The vanilla/Paper entity system handles short local movement toward the next anchor where practical.
- Tactical decisions are reevaluated at a lower frequency than movement ticks.
- Units do not continuously compute new long-distance block paths.
- A stuck/invalid local movement condition may advance to a simple recovery behavior such as retrying the anchor or choosing a nearby safe offset; it must not introduce a heavy general pathfinder.

This design is intended to make DEV-116 measurement meaningful instead of spending the entire AI budget on repeated target/path recomputation.

## 5. Entity and visual abstraction

Troop type must not be permanently coupled to a specific vanilla mob class.

Introduce a visual/profile abstraction that describes the current temporary renderer for a troop type, including at minimum:

- visual profile id
- body entity type
- equipment/loadout intent
- visible Korean role label or team marker for development validation
- optional mount profile for cavalry

The combat controller interacts with troop/runtime abstractions, not hard-coded assumptions that `INFANTRY == ZOMBIE` or `ARCHERS == SKELETON`.

This allows the representative to later replace temporary vanilla visuals with custom modeled assets without rewriting CombatAI or waypoint behavior. A future model system may use a resource pack, CustomModelData/data components, Display entities, ModelEngine-style integration or another selected renderer. DEV-115 does not choose or require a final modeling technology.

## 6. Initial temporary troop representations

These are development renderers only and may change later.

### Infantry

- humanoid hostile-mob body suitable for Paper navigation
- melee weapon visual
- frontline movement and melee execution

### Spearmen

- humanoid hostile-mob body
- visually distinct polearm/spear-like equipment placeholder
- uses SCREEN/ENGAGE positioning to intercept cavalry or protect backline

### Archers

- ranged-capable humanoid body, initially suitable for bow combat
- maintains distance where route layout allows
- retreats to a rear waypoint when threatened
- performs visible ranged attacks rather than walking into deliberate melee

### Cavalry

- one logical combat unit represented by two Bukkit entities: horse + rider
- horse and rider are managed as one runtime unit
- rider death/mount death must not leave an unmanaged orphan entity
- FLANK and PURSUE should visibly use cavalry mobility
- exact mount speed/damage values remain provisional fixtures until balance work

### Magic troops

- ordinary humanoid body rather than Evoker AI
- DEV-114 `SUPPORT` controls support positioning
- direct threat triggers retreat behavior
- a lightweight visual effect may indicate support activity for development validation
- no finalized spell, mana, magic-stone cost, cooldown or damage effect is implemented in DEV-115

## 7. Teams and target safety

Live demo units need explicit side identity so they do not attack allies.

The runtime must:

- assign every spawned combat unit to a side
- select hostile targets only from opposing sides
- avoid targeting players by default in the development demo
- avoid targeting unrelated world mobs by default
- remove/cleanup all spawned demo units through an explicit command and on plugin disable where feasible

Development demo behavior must be isolated enough that it does not become a general mob-griefing source on the server.

## 8. Attack execution

DEV-115 provides visible execution, not final damage balance.

### Melee

Infantry, spearmen and cavalry may use controlled vanilla/Paper melee execution when within suitable range. Damage numbers are provisional implementation fixtures and must not be presented as final balance.

### Ranged

Archers visibly fire at hostile units while maintaining intended separation. The implementation should prefer ordinary Paper/vanilla projectile behavior where this does not compromise side safety.

### Magic support

Magic troops do not receive a final combat spell in DEV-115. Their SUPPORT state is represented through position and a harmless or clearly provisional visual indication. Concrete buffs/debuffs/damage spells belong to the later magic system.

## 9. Decision cadence and movement cadence

CombatAI tactical reevaluation and entity movement must be separated.

- movement/local navigation may update frequently enough for smooth Minecraft motion
- battlefield observation and DEV-114 decision reevaluation should occur at a lower configurable cadence
- target selection should not be recomputed every server tick unless an immediate safety condition requires it

No final cadence number is fixed by the design document. Implementation may use conservative development defaults and keep them easy to change for DEV-116 benchmarking.

## 10. Development demo command

Provide an explicit development validation entry under `/danta combat-ai`, conceptually:

- `/danta combat-ai demo start`
- `/danta combat-ai demo stop`
- optional `/danta combat-ai demo status`

Exact subcommand naming may follow the existing command bridge structure if a more consistent variant is found during implementation.

The demo should create a small deterministic opposing formation near a known test location or the invoking player's controlled test location, without tying the demo to strategic army counts.

The representative must be able to visually confirm:

- infantry advancing/engaging
- spearmen screening/intercepting
- archers remaining behind the line and falling back from pressure
- cavalry using mounted flank/pursuit movement
- magic troops maintaining support position and retreating when threatened
- actual visible attacks and health/damage interaction

## 11. Testing strategy

Use TDD for independently testable behavior.

### Paper-independent/adapter tests

Prefer deterministic tests for:

- CombatAiAction -> movement intent mapping
- route selection for advance/flank/retreat/support
- side-based target eligibility
- cavalry composite-unit lifecycle state
- visual profile mapping remains independent from `TroopType` logic
- cleanup registry behavior

### Paper boundary tests

Test command routing and non-Bukkit portions without requiring a live server where possible.

### Live Windows/Paper validation

Final DEV-115 verification requires the representative's existing Paper 26.2 build 123 server. The ChatGPT environment must not install a replacement Paper/Java/Gradle environment.

Live validation must confirm visual movement, attacks, mounted cavalry behavior, cleanup and Korean command feedback.

## 12. Failure and cleanup behavior

The demo runtime should fail safely:

- missing world/test location: reject with Korean message rather than raw exception
- invalid spawn: clean already-created entities from that start attempt
- plugin disable/reload: best-effort removal of registered demo entities
- dead entities: remove them from runtime tracking
- cavalry rider/mount loss: clean or retire the composite unit consistently
- repeated `demo start`: either reject while active or cleanly replace the prior demo; do not silently accumulate armies

## 13. Non-goals

DEV-115 does not include:

- strategic army troop count -> live entity count mapping (DEV-117)
- wave system or reinforcement mapping (DEV-117)
- 40/60/80 entity benchmark conclusions (DEV-116)
- final performance tuning before measurement
- full arbitrary-world A* pathfinding
- player incapacitation/re-entry (DEV-118)
- siege result merge into logical strategic state (DEV-119)
- siege snapshot/recovery (DEV-120)
- final custom models/skins/animations
- final troop damage, speed, range or cooldown balance
- player magic or final magic-troop spell system

## 14. Definition of Done

DEV-115 is complete when all of the following are true:

- a Paper-side runtime can spawn and track visible combat units for all five troop types
- cavalry is represented as a managed horse+rider composite
- temporary vanilla visual choices are separated from troop identity so custom models can replace them later
- authored tactical waypoints/routes exist for advance, flank, retreat and support movement
- DEV-114 tactical actions visibly translate into movement/combat behavior
- infantry/spearmen/cavalry can visibly perform melee execution
- archers can visibly perform ranged execution while respecting backline/fallback behavior
- magic troops visibly maintain support positioning without introducing a premature final spell system
- opposing sides damage enemies but do not intentionally attack allies, players or unrelated mobs in the development demo
- demo start/stop cleanup works without accumulating unmanaged entities
- automated tests/build pass on the representative's Windows environment
- live Paper validation confirms the expected role differences and visible combat

DEV-116 begins only after this behavior is stable enough to benchmark without changing the fundamental execution model during the measurement itself.
