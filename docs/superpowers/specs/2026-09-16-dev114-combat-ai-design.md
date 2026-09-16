# DEV-114 CombatAI Design

Date: 2026-09-16
Status: APPROVED DIRECTION / IMPLEMENTATION PENDING

## 1. Goal

DEV-114 establishes the real-time battlefield AI foundation used by siege battles. The live battlefield must represent the same basic troop structure already present in core combat instead of introducing a separate Paper-only ruleset.

The implementation target is five combat troop families:

- infantry
- spearmen
- archers
- cavalry
- magic troops

Player-cast magic during siege remains a separate player system and is not implemented here. DEV-114 only provides the AI behavior foundation for magic troops as an army unit type.

## 2. Existing invariants to preserve

- Core combat remains authoritative for troop concepts and strategic combat rules.
- Paper code handles Minecraft entity/runtime integration only.
- DEV-113 siege progression remains independent from individual entity AI and receives battle results through the existing siege boundary.
- DEV-115 owns waypoint/path movement; DEV-114 must not duplicate a full pathfinder.
- DEV-116 owns 40-concurrent-AI performance benchmarking; DEV-114 should expose a structure suitable for that benchmark without prematurely optimizing around guessed numbers.
- Player-facing text remains Korean. Internal IDs/enums may remain English.

## 3. Troop model reconciliation

Current core defines INFANTRY, SPEARMEN, ARCHERS, CAVALRY. DEV-114 extends the shared troop model with MAGIC rather than inventing a Paper-only magic type.

Existing BasicTroopTypes definitions continue to provide strategic roles/counter data. MAGIC is introduced conservatively as a support-oriented role. Exact damage, resource cost, spell list, cooldowns and mana-stone consumption are not finalized in DEV-114.

## 4. AI architecture

### 4.1 Paper-independent decision layer

Introduce a small `combat.ai` decision layer in core. It accepts a battlefield observation and returns a desired tactical action. It must not know Bukkit entities, locations or scheduler APIs.

Primary concepts:

- `CombatAiProfile`: INFANTRY, SPEARMEN, ARCHERS, CAVALRY, MAGIC
- `CombatAiObservation`: self state plus abstract nearby battlefield facts
- `CombatAiAction`: HOLD, ADVANCE, ENGAGE, RETREAT, SCREEN, FLANK, PURSUE, SUPPORT
- `CombatAiDecision`: action plus target preference / distance intent where needed
- `CombatAiController`: selects a decision for a profile

The observation should remain minimal: distance to nearest hostile, hostile role/type if known, whether frontline support exists, whether backline is threatened, whether target is retreating, and whether the unit is below a health/survival threshold. It should not encode block-by-block navigation.

### 4.2 Paper runtime adapter

Add a Paper-side combat AI runtime that maps Minecraft entities and battlefield state to `CombatAiObservation`, asks the core controller for a decision, then applies only high-level entity intents.

DEV-114 may use simple direct target assignment / short-range movement primitives for live validation. Long-distance navigation and route-following are deliberately deferred to DEV-115.

## 5. Initial behavior rules

The rules are deterministic foundations, not final balance values.

### Infantry

- primary frontline unit
- advances toward nearby enemy frontline
- engages in melee when in range
- holds rather than chasing far beyond the battle line
- retreats only under explicit survival/retreat condition

### Spearmen

- frontline screening unit
- prioritizes cavalry threats when visible
- otherwise protects friendly ranged/support units and fights as frontline
- does not independently perform deep pursuit

### Archers

- maintains separation from enemy frontline
- attacks from the backline when protected
- falls back if a hostile closes into unsafe range
- does not intentionally enter melee while a retreat lane exists

### Cavalry

- mobile/flanking unit
- prefers exposed ranged/support targets
- may flank rather than collide with a stable enemy frontline
- pursues retreating enemies when allowed
- avoids charging an obvious spear screen when an alternate target exists

### Magic troops

- support-oriented backline unit
- prefers staying behind the frontline
- chooses SUPPORT while a valid allied combat group exists
- falls back when directly threatened
- DEV-114 does not define concrete spells; SUPPORT is a semantic hook for the future magic system

## 6. Relationship to player magic

Two distinct systems are preserved:

1. `MAGIC` troop AI: an army unit controlled by combat AI.
2. Player magic: abilities cast by players who personally participate in siege battles.

Player magic effects, skill catalog, cooldowns, mana/magic-stone costs and targeting UI belong to the later magic phase. The Paper combat runtime should eventually allow both systems to affect the same live battlefield without treating a player as a magic troop entity.

## 7. Testing

Core tests first:

- infantry chooses frontline engagement instead of flank/pursuit
- spearmen prioritize cavalry threat
- archers maintain/fall back from unsafe close range
- cavalry prefers exposed backline and respects spear-screen preference
- magic troops choose support and retreat when directly threatened
- deterministic decisions for identical observations

Paper tests should verify only adapter behavior that can be tested without a live server. Live Paper validation should then confirm representative entities exhibit the expected five role patterns.

## 8. Non-goals for DEV-114

- full waypoint/pathfinding system (DEV-115)
- final entity count/performance tuning (DEV-116)
- wave-to-logical-force mapping (DEV-117)
- player incapacitation/re-entry (DEV-118)
- concrete magic spell catalog/effects
- final troop balance numbers
- final mob models/skins/animations

## 9. Definition of Done

DEV-114 is complete when:

- shared troop model includes MAGIC without breaking existing combat tests/snapshots where applicable
- five troop AI profiles return distinct deterministic tactical decisions
- Paper runtime can instantiate/feed the five AI profiles without duplicating strategic combat rules
- representative Paper live validation confirms each profile follows its intended battlefield role
- no DEV-115 pathfinding or later-phase magic features are prematurely embedded
