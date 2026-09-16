# DEV-115 Waypoint Combat Runtime Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Connect DEV-114 CombatAI decisions to visible Paper battlefield entities that move through authored tactical waypoints, attack enemies, retreat/flank/support visibly, and remain replaceable with future custom models.

**Architecture:** Keep DEV-114 core decisions unchanged and add a Paper-side execution layer. A small Paper-independent tactical routing model translates `CombatAiAction` into waypoint intent; Bukkit-specific runtime code owns entity spawn, local navigation, attacks, cleanup and the horse+rider cavalry composite. The development demo is isolated from strategic army counts so DEV-117 can later map logical force to live entities without rewriting DEV-115.

**Tech Stack:** Java 25, Gradle 9.7.1, Paper API 26.2 build 123, JUnit 5.13.4

**Spec:** `docs/superpowers/specs/2026-09-16-dev115-waypoint-combat-runtime-design.md`

## Global Constraints

- GitHub `Hushkisses/danta-server` `main` and `docs/PROJECT-STATE.md` are the source of truth before every task.
- Do not install or download Java, Gradle, Paper or PostgreSQL in the ChatGPT environment.
- The representative validates RED/GREEN on the existing Windows Paper development environment with `dev-server\quick-deploy.bat`.
- Core strategic combat rules and DEV-114 CombatAI decisions remain authoritative; DEV-115 adds execution only.
- All player-facing command output, warnings and rejection reasons default to Korean.
- Temporary vanilla entity choices are renderers only; `TroopType` must not be permanently coupled to a mob class.
- Cavalry is one logical combat unit represented by a managed horse+rider pair.
- Magic troops receive support positioning and harmless provisional visuals only; no final spell, mana, magic-stone cost or damage system is introduced.
- DEV-116 owns 40/60/80 AI benchmarking; DEV-117 owns logical troop-strength/wave-to-entity mapping.
- No arbitrary-world full A* pathfinder is introduced.

---

### Task 1: Tactical waypoint model and action-to-route intent

**Files:**
- Create: `paper-plugin/src/main/java/kr/danta/paper/combat/live/TacticalWaypoint.java`
- Create: `paper-plugin/src/main/java/kr/danta/paper/combat/live/TacticalRoute.java`
- Create: `paper-plugin/src/main/java/kr/danta/paper/combat/live/CombatMovementIntent.java`
- Create: `paper-plugin/src/main/java/kr/danta/paper/combat/live/CombatMovementPlanner.java`
- Test: `paper-plugin/src/test/java/kr/danta/paper/combat/live/CombatMovementPlannerTest.java`

**Interfaces:**
- Consumes: `kr.danta.core.combat.ai.CombatAiAction`
- Produces:
  - `record TacticalWaypoint(String id, double x, double y, double z)`
  - `record TacticalRoute(String id, List<TacticalWaypoint> waypoints)`
  - `record CombatMovementIntent(CombatAiAction action, TacticalRoute route)`
  - `CombatMovementPlanner.plan(CombatAiAction action, TacticalRouteSet routes)`
  - nested/public `TacticalRouteSet` carrying `hold`, `advance`, `retreat`, `screen`, `leftFlank`, `rightFlank`, `pursuit`, `support`

- [ ] **Step 1: Write the failing planner test**

```java
package kr.danta.paper.combat.live;

import kr.danta.core.combat.ai.CombatAiAction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CombatMovementPlannerTest {
    private static TacticalRoute route(String id, double x) {
        return new TacticalRoute(id, List.of(new TacticalWaypoint(id + "-1", x, 64, 0)));
    }

    private static CombatMovementPlanner.TacticalRouteSet routes() {
        return new CombatMovementPlanner.TacticalRouteSet(
                route("hold", 0), route("advance", 10), route("retreat", -10),
                route("screen", 4), route("left", 8), route("right", 8),
                route("pursuit", 14), route("support", -4));
    }

    @Test
    void mapsCombatAiActionsToAuthoredRoutes() {
        CombatMovementPlanner planner = new CombatMovementPlanner();
        assertEquals("hold", planner.plan(CombatAiAction.HOLD, routes()).route().id());
        assertEquals("advance", planner.plan(CombatAiAction.ADVANCE, routes()).route().id());
        assertEquals("advance", planner.plan(CombatAiAction.ENGAGE, routes()).route().id());
        assertEquals("retreat", planner.plan(CombatAiAction.RETREAT, routes()).route().id());
        assertEquals("screen", planner.plan(CombatAiAction.SCREEN, routes()).route().id());
        assertTrue(List.of("left", "right").contains(planner.plan(CombatAiAction.FLANK, routes()).route().id()));
        assertEquals("pursuit", planner.plan(CombatAiAction.PURSUE, routes()).route().id());
        assertEquals("support", planner.plan(CombatAiAction.SUPPORT, routes()).route().id());
    }
}
```

- [ ] **Step 2: Run Windows RED verification**

Run:

```powershell
git pull
dev-server\quick-deploy.bat
```

Expected: compile failure because the DEV-115 live-combat waypoint classes do not yet exist.

- [ ] **Step 3: Implement the minimal waypoint model and planner**

```java
public record TacticalWaypoint(String id, double x, double y, double z) {
    public TacticalWaypoint {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("waypoint id is blank");
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z))
            throw new IllegalArgumentException("waypoint coordinates must be finite");
    }
}
```

```java
public record TacticalRoute(String id, List<TacticalWaypoint> waypoints) {
    public TacticalRoute {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("route id is blank");
        waypoints = List.copyOf(waypoints);
        if (waypoints.isEmpty()) throw new IllegalArgumentException("route requires at least one waypoint");
    }
}
```

```java
public final class CombatMovementPlanner {
    public CombatMovementIntent plan(CombatAiAction action, TacticalRouteSet routes) {
        TacticalRoute route = switch (action) {
            case HOLD -> routes.hold();
            case ADVANCE, ENGAGE -> routes.advance();
            case RETREAT -> routes.retreat();
            case SCREEN -> routes.screen();
            case FLANK -> routes.leftFlank();
            case PURSUE -> routes.pursuit();
            case SUPPORT -> routes.support();
        };
        return new CombatMovementIntent(action, route);
    }

    public record TacticalRouteSet(
            TacticalRoute hold,
            TacticalRoute advance,
            TacticalRoute retreat,
            TacticalRoute screen,
            TacticalRoute leftFlank,
            TacticalRoute rightFlank,
            TacticalRoute pursuit,
            TacticalRoute support
    ) {}
}
```

The first implementation uses a deterministic left flank to keep tests deterministic; battlefield-side alternation can be added only if a later task proves it useful without changing this interface.

- [ ] **Step 4: Run Windows GREEN verification**

Run:

```powershell
git pull
dev-server\quick-deploy.bat
```

Expected: `CombatMovementPlannerTest` and all existing tests PASS.

- [ ] **Step 5: Commit**

```bash
git add paper-plugin/src/main/java/kr/danta/paper/combat/live paper-plugin/src/test/java/kr/danta/paper/combat/live/CombatMovementPlannerTest.java
git commit -m "feat: add DEV-115 tactical waypoint planner"
```

---

### Task 2: Side-safe target policy and live-unit state

**Files:**
- Create: `paper-plugin/src/main/java/kr/danta/paper/combat/live/CombatSide.java`
- Create: `paper-plugin/src/main/java/kr/danta/paper/combat/live/LiveCombatUnit.java`
- Create: `paper-plugin/src/main/java/kr/danta/paper/combat/live/CombatTargetPolicy.java`
- Test: `paper-plugin/src/test/java/kr/danta/paper/combat/live/CombatTargetPolicyTest.java`

**Interfaces:**
- Produces:
  - `enum CombatSide { RED, BLUE }`
  - `record LiveCombatUnit(UUID unitId, CombatSide side, TroopType troopType, UUID primaryEntityId, UUID mountEntityId, String visualProfileId)`
  - `boolean CombatTargetPolicy.mayTarget(LiveCombatUnit attacker, LiveCombatUnit candidate)`

- [ ] **Step 1: Write failing side-safety tests**

```java
@Test
void onlyOpposingTrackedUnitsAreEligibleTargets() {
    CombatTargetPolicy policy = new CombatTargetPolicy();
    LiveCombatUnit red = unit(CombatSide.RED, TroopType.INFANTRY);
    LiveCombatUnit redFriend = unit(CombatSide.RED, TroopType.ARCHERS);
    LiveCombatUnit blue = unit(CombatSide.BLUE, TroopType.SPEARMEN);

    assertFalse(policy.mayTarget(red, red));
    assertFalse(policy.mayTarget(red, redFriend));
    assertTrue(policy.mayTarget(red, blue));
}
```

- [ ] **Step 2: Run Windows RED verification**

Expected: missing `CombatSide`, `LiveCombatUnit` and `CombatTargetPolicy`.

- [ ] **Step 3: Implement immutable runtime identity and side policy**

```java
public enum CombatSide { RED, BLUE }
```

```java
public record LiveCombatUnit(
        UUID unitId,
        CombatSide side,
        TroopType troopType,
        UUID primaryEntityId,
        UUID mountEntityId,
        String visualProfileId
) {
    public boolean mounted() { return mountEntityId != null; }
}
```

```java
public final class CombatTargetPolicy {
    public boolean mayTarget(LiveCombatUnit attacker, LiveCombatUnit candidate) {
        if (attacker == null || candidate == null) return false;
        if (attacker.unitId().equals(candidate.unitId())) return false;
        return attacker.side() != candidate.side();
    }
}
```

- [ ] **Step 4: Run Windows GREEN verification**

Expected: new tests and regression suite PASS.

- [ ] **Step 5: Commit**

```bash
git add paper-plugin/src/main/java/kr/danta/paper/combat/live paper-plugin/src/test/java/kr/danta/paper/combat/live/CombatTargetPolicyTest.java
git commit -m "feat: add DEV-115 live unit target policy"
```

---

### Task 3: Replaceable visual profiles and cavalry composite contract

**Files:**
- Create: `paper-plugin/src/main/java/kr/danta/paper/combat/live/CombatUnitVisualProfile.java`
- Create: `paper-plugin/src/main/java/kr/danta/paper/combat/live/CombatUnitVisualCatalog.java`
- Create: `paper-plugin/src/main/java/kr/danta/paper/combat/live/CavalryCompositeState.java`
- Test: `paper-plugin/src/test/java/kr/danta/paper/combat/live/CombatUnitVisualCatalogTest.java`
- Test: `paper-plugin/src/test/java/kr/danta/paper/combat/live/CavalryCompositeStateTest.java`

**Interfaces:**
- `CombatUnitVisualProfile` stores `profileId`, `bodyEntityKey`, `mountEntityKey`, `displayName`, `mainHandMaterialKey` as strings so unit identity is not coupled to Bukkit classes.
- `CombatUnitVisualCatalog.profileFor(TroopType)` returns a renderer profile.
- `CavalryCompositeState` treats rider and horse as one logical lifecycle.

- [ ] **Step 1: Write tests proving renderer independence and cavalry lifecycle**

```java
@Test
void everyTroopTypeHasAReplaceableVisualProfile() {
    CombatUnitVisualCatalog catalog = CombatUnitVisualCatalog.developmentDefaults();
    for (TroopType type : TroopType.values()) {
        CombatUnitVisualProfile profile = catalog.profileFor(type);
        assertNotNull(profile);
        assertFalse(profile.profileId().isBlank());
    }
    assertNotEquals(TroopType.INFANTRY.name(), catalog.profileFor(TroopType.INFANTRY).profileId());
}
```

```java
@Test
void cavalryRetiresWhenEitherCompositeMemberIsGone() {
    CavalryCompositeState state = new CavalryCompositeState(UUID.randomUUID(), UUID.randomUUID());
    assertFalse(state.shouldRetire(true, true));
    assertTrue(state.shouldRetire(false, true));
    assertTrue(state.shouldRetire(true, false));
}
```

- [ ] **Step 2: Run Windows RED verification**

Expected: new visual/composite types missing.

- [ ] **Step 3: Implement string-keyed development renderer profiles**

Development defaults:

```java
INFANTRY -> new CombatUnitVisualProfile("dev_infantry", "HUSK", null, "보병", "IRON_SWORD")
SPEARMEN -> new CombatUnitVisualProfile("dev_spearmen", "HUSK", null, "창병", "TRIDENT")
ARCHERS -> new CombatUnitVisualProfile("dev_archers", "SKELETON", null, "궁병", "BOW")
CAVALRY -> new CombatUnitVisualProfile("dev_cavalry", "HUSK", "HORSE", "기병", "IRON_SWORD")
MAGIC -> new CombatUnitVisualProfile("dev_magic", "HUSK", null, "마법병", "BLAZE_ROD")
```

Do not expose these entity keys as domain truth; they exist only in `CombatUnitVisualCatalog`.

- [ ] **Step 4: Run Windows GREEN verification**

Expected: all new catalog/composite tests PASS.

- [ ] **Step 5: Commit**

```bash
git add paper-plugin/src/main/java/kr/danta/paper/combat/live paper-plugin/src/test/java/kr/danta/paper/combat/live
git commit -m "feat: add replaceable DEV-115 troop visuals"
```

---

### Task 4: Bukkit entity factory and tracked-unit registry

**Files:**
- Create: `paper-plugin/src/main/java/kr/danta/paper/combat/live/PaperCombatUnitFactory.java`
- Create: `paper-plugin/src/main/java/kr/danta/paper/combat/live/LiveCombatUnitRegistry.java`
- Test: `paper-plugin/src/test/java/kr/danta/paper/combat/live/LiveCombatUnitRegistryTest.java`

**Interfaces:**
- `LiveCombatUnitRegistry.register(LiveCombatUnit)`
- `Optional<LiveCombatUnit> byPrimaryEntity(UUID)`
- `Collection<LiveCombatUnit> units()`
- `void remove(UUID unitId)`
- `void clear()`
- `PaperCombatUnitFactory.spawn(World world, Location location, CombatSide side, TroopType troopType)` returns `SpawnedCombatUnit(LiveCombatUnit unit, LivingEntity primary, LivingEntity mount)`.

- [ ] **Step 1: Write failing registry tests**

```java
@Test
void registerLookupRemoveAndClearAreDeterministic() {
    LiveCombatUnitRegistry registry = new LiveCombatUnitRegistry();
    LiveCombatUnit unit = unit(CombatSide.RED, TroopType.INFANTRY);
    registry.register(unit);
    assertEquals(unit, registry.byPrimaryEntity(unit.primaryEntityId()).orElseThrow());
    registry.remove(unit.unitId());
    assertTrue(registry.units().isEmpty());
}
```

- [ ] **Step 2: Run Windows RED verification**

Expected: registry class missing.

- [ ] **Step 3: Implement registry and Paper spawn factory**

Factory requirements:

```java
EntityType bodyType = EntityType.valueOf(profile.bodyEntityKey());
LivingEntity body = (LivingEntity) world.spawnEntity(location, bodyType);
body.setPersistent(false);
body.setRemoveWhenFarAway(false);
body.customName(Component.text(sideLabel + " " + profile.displayName()));
body.setCustomNameVisible(true);
```

For cavalry:

```java
Horse horse = (Horse) world.spawnEntity(location, EntityType.HORSE);
LivingEntity rider = (LivingEntity) world.spawnEntity(location, EntityType.valueOf(profile.bodyEntityKey()));
horse.addPassenger(rider);
```

Apply development equipment through `Material.valueOf(profile.mainHandMaterialKey())`. Store persistent plugin metadata/PDC tag such as `danta_combat_demo=1` and side/unit id so cleanup can recognize owned demo entities after partial failures.

- [ ] **Step 4: Run Windows GREEN verification**

Expected: registry tests PASS and production compiles against Paper API.

- [ ] **Step 5: Commit**

```bash
git add paper-plugin/src/main/java/kr/danta/paper/combat/live paper-plugin/src/test/java/kr/danta/paper/combat/live/LiveCombatUnitRegistryTest.java
git commit -m "feat: spawn and track DEV-115 combat units"
```

---

### Task 5: Authored demo battlefield waypoint layout

**Files:**
- Create: `paper-plugin/src/main/java/kr/danta/paper/combat/live/DemoBattlefieldLayout.java`
- Test: `paper-plugin/src/test/java/kr/danta/paper/combat/live/DemoBattlefieldLayoutTest.java`

**Interfaces:**
- `DemoBattlefieldLayout.around(double originX, double originY, double originZ)`
- Produces side-specific `TacticalRouteSet`, spawn offsets and opposing formation anchors without Bukkit `Location` dependencies.

- [ ] **Step 1: Write failing geometry tests**

```java
@Test
void redAndBlueAdvanceTowardEachOtherWhileRetreatRoutesLeadAway() {
    DemoBattlefieldLayout layout = DemoBattlefieldLayout.around(100, 64, 100);
    double redAdvanceX = layout.routes(CombatSide.RED).advance().waypoints().getLast().x();
    double redRetreatX = layout.routes(CombatSide.RED).retreat().waypoints().getLast().x();
    double blueAdvanceX = layout.routes(CombatSide.BLUE).advance().waypoints().getLast().x();
    double blueRetreatX = layout.routes(CombatSide.BLUE).retreat().waypoints().getLast().x();
    assertTrue(redAdvanceX > redRetreatX);
    assertTrue(blueAdvanceX < blueRetreatX);
}
```

- [ ] **Step 2: Run Windows RED verification**

Expected: `DemoBattlefieldLayout` missing.

- [ ] **Step 3: Implement a compact deterministic layout**

Use local offsets around an origin, not a hardcoded world coordinate. Example fixture geometry:

```text
RED rear/support  -14
RED spawn         -12
RED screen         -5
center              0
BLUE screen         5
BLUE spawn          12
BLUE rear/support   14
flanks use Z ±8
```

Keep all distances as development fixtures in this class only so DEV-116 can tune them without touching CombatAI.

- [ ] **Step 4: Run Windows GREEN verification**

Expected: geometry tests PASS.

- [ ] **Step 5: Commit**

```bash
git add paper-plugin/src/main/java/kr/danta/paper/combat/live/DemoBattlefieldLayout.java paper-plugin/src/test/java/kr/danta/paper/combat/live/DemoBattlefieldLayoutTest.java
git commit -m "feat: add DEV-115 demo waypoint layout"
```

---

### Task 6: Combat observation builder and execution intent controller

**Files:**
- Create: `paper-plugin/src/main/java/kr/danta/paper/combat/live/LiveCombatObservationBuilder.java`
- Create: `paper-plugin/src/main/java/kr/danta/paper/combat/live/LiveCombatExecution.java`
- Create: `paper-plugin/src/main/java/kr/danta/paper/combat/live/LiveCombatController.java`
- Test: `paper-plugin/src/test/java/kr/danta/paper/combat/live/LiveCombatControllerTest.java`

**Interfaces:**
- Consumes existing `PaperCombatAiRuntime.decide(TroopType, TacticalSnapshot)`.
- `LiveCombatObservationBuilder` converts tracked unit positions/health/nearby hostile facts into existing `TacticalSnapshot` without changing DEV-114 core rules.
- `LiveCombatExecution` carries `CombatAiDecision`, movement route, selected hostile unit id.
- `LiveCombatController.decide(unit, battlefieldView)` returns execution intent only; entity motion is Task 7.

- [ ] **Step 1: Write failing role-visible execution tests using deterministic battlefield views**

Cover at minimum:

```java
assertEquals(CombatAiAction.SCREEN, controller.decide(redSpearman, cavalryThreatView).decision().action());
assertEquals(CombatAiAction.RETREAT, controller.decide(redArcher, closeThreatView).decision().action());
assertEquals(CombatAiAction.FLANK, controller.decide(redCavalry, exposedBacklineView).decision().action());
assertEquals(CombatAiAction.SUPPORT, controller.decide(redMagic, supportedRearView).decision().action());
```

- [ ] **Step 2: Run Windows RED verification**

Expected: missing live execution controller.

- [ ] **Step 3: Implement adapter-only controller**

Controller sequence:

```java
TacticalSnapshot snapshot = observationBuilder.build(unit, view);
CombatAiDecision decision = combatAiRuntime.decide(unit.troopType(), snapshot);
CombatMovementIntent movement = movementPlanner.plan(decision.action(), view.routes(unit.side()));
Optional<LiveCombatUnit> target = view.preferredTarget(unit, decision.preferredTargetType());
return new LiveCombatExecution(decision, movement, target.map(LiveCombatUnit::unitId).orElse(null));
```

Do not add new troop decision thresholds here; fix DEV-114 only if a failing correctness test requires it.

- [ ] **Step 4: Run Windows GREEN verification**

Expected: role-visible decision/route tests PASS.

- [ ] **Step 5: Commit**

```bash
git add paper-plugin/src/main/java/kr/danta/paper/combat/live paper-plugin/src/test/java/kr/danta/paper/combat/live/LiveCombatControllerTest.java
git commit -m "feat: bridge CombatAI into DEV-115 execution intents"
```

---

### Task 7: Paper movement, melee, ranged and magic-support execution

**Files:**
- Create: `paper-plugin/src/main/java/kr/danta/paper/combat/live/PaperCombatActionExecutor.java`
- Create: `paper-plugin/src/main/java/kr/danta/paper/combat/live/CombatEntityResolver.java`
- Create: `paper-plugin/src/main/java/kr/danta/paper/combat/live/CombatAttackPolicy.java`
- Test: `paper-plugin/src/test/java/kr/danta/paper/combat/live/CombatAttackPolicyTest.java`

**Interfaces:**
- `CombatAttackPolicy` determines attack mode/range from `TroopType` with clearly labeled development fixtures.
- `PaperCombatActionExecutor.apply(LiveCombatUnit unit, LiveCombatExecution execution, LiveCombatRuntime runtime)` moves toward the next waypoint and attacks only tracked hostile units.

- [ ] **Step 1: Write failing attack-mode tests**

```java
@Test
void attackModesMatchDevelopmentRoles() {
    CombatAttackPolicy policy = CombatAttackPolicy.developmentDefaults();
    assertEquals(AttackMode.MELEE, policy.forType(TroopType.INFANTRY).mode());
    assertEquals(AttackMode.MELEE, policy.forType(TroopType.SPEARMEN).mode());
    assertEquals(AttackMode.RANGED, policy.forType(TroopType.ARCHERS).mode());
    assertEquals(AttackMode.MELEE, policy.forType(TroopType.CAVALRY).mode());
    assertEquals(AttackMode.SUPPORT_VISUAL, policy.forType(TroopType.MAGIC).mode());
}
```

- [ ] **Step 2: Run Windows RED verification**

Expected: attack policy missing.

- [ ] **Step 3: Implement movement and attack executor**

Movement rules:

```java
Location goal = resolver.toLocation(world, execution.movement().route().waypoints().getFirst());
Vector direction = goal.toVector().subtract(body.getLocation().toVector()).normalize();
body.setVelocity(direction.multiply(policy.moveSpeed(unit.troopType())));
```

This is a short-anchor steering primitive, not global pathfinding. If the chosen Paper entity exposes safe native navigation that works on target 26.2, it may replace this local velocity step behind the same executor interface after live validation.

Attack rules:

```java
if (spec.mode() == AttackMode.MELEE && distance <= spec.range()) {
    target.damage(spec.damage(), attackerBody);
}
```

For archers, spawn an `Arrow` owned by the archer only when the selected target is a tracked hostile and the cooldown has elapsed. For magic `SUPPORT_VISUAL`, emit a harmless particle around the support unit/allied group; do not modify combat stats.

Use per-unit cooldown timestamps in runtime state rather than scheduling one Bukkit task per attack.

- [ ] **Step 4: Run Windows GREEN verification**

Expected: attack-policy tests and full suite PASS.

- [ ] **Step 5: Commit**

```bash
git add paper-plugin/src/main/java/kr/danta/paper/combat/live paper-plugin/src/test/java/kr/danta/paper/combat/live/CombatAttackPolicyTest.java
git commit -m "feat: execute DEV-115 movement and attacks"
```

---

### Task 8: Live demo runtime loop, death cleanup and cavalry orphan prevention

**Files:**
- Create: `paper-plugin/src/main/java/kr/danta/paper/combat/live/LiveCombatRuntime.java`
- Create: `paper-plugin/src/main/java/kr/danta/paper/combat/live/LiveCombatListener.java`
- Test: `paper-plugin/src/test/java/kr/danta/paper/combat/live/LiveCombatRuntimeStateTest.java`

**Interfaces:**
- `startDemo(World world, Location origin)`
- `stopDemo()`
- `tick()`
- `status()`
- `shutdown()`
- listener reports tracked entity death to runtime.

- [ ] **Step 1: Write failing runtime-state tests**

```java
@Test
void repeatedStartIsRejectedAndStopClearsTracking() {
    LiveCombatRuntimeState state = new LiveCombatRuntimeState();
    assertTrue(state.begin());
    assertFalse(state.begin());
    state.finish();
    assertFalse(state.active());
}
```

Also test composite retirement:

```java
@Test
void cavalryMemberLossRetiresWholeLogicalUnit() {
    // register horse+rider ids, mark one dead, assert retirement request contains both ids
}
```

- [ ] **Step 2: Run Windows RED verification**

Expected: live runtime state/lifecycle classes missing.

- [ ] **Step 3: Implement one shared Paper scheduler loop**

Runtime behavior:

```java
if (++movementTickCounter % movementIntervalTicks == 0) executeMovementForAll();
if (++decisionTickCounter % decisionIntervalTicks == 0) reevaluateCombatAiForAll();
cleanupDeadUnits();
```

Use one shared repeating task for the demo, not one task per unit. Spawn a small deterministic formation containing all five troop types on both RED and BLUE. Register `LiveCombatListener` for death cleanup. On cavalry rider or horse loss, remove the counterpart and retire the logical unit. `stopDemo()` and `shutdown()` remove every registered entity and cancel the runtime task.

- [ ] **Step 4: Run Windows GREEN verification**

Expected: runtime lifecycle tests and regressions PASS.

- [ ] **Step 5: Commit**

```bash
git add paper-plugin/src/main/java/kr/danta/paper/combat/live paper-plugin/src/test/java/kr/danta/paper/combat/live/LiveCombatRuntimeStateTest.java
git commit -m "feat: run and clean DEV-115 live combat demo"
```

---

### Task 9: `/danta combat-ai demo` command bridge and plugin lifecycle wiring

**Files:**
- Modify: `paper-plugin/src/main/java/kr/danta/paper/combat/ai/DantaCombatAiCommandBridge.java`
- Modify: `paper-plugin/src/main/java/kr/danta/paper/DantaPlugin.java`
- Test: `paper-plugin/src/test/java/kr/danta/paper/combat/ai/Dev115CombatAiDemoCommandTest.java`
- Test: `paper-plugin/src/test/java/kr/danta/paper/Dev115DantaPluginCombatRuntimeWiringTest.java`

**Interfaces:**
- Existing `profiles` and `decide` outputs remain unchanged.
- Demo commands:
  - `/danta combat-ai demo start`
  - `/danta combat-ai demo stop`
  - `/danta combat-ai demo status`
- Bukkit-dependent demo execution should be injected/called at `DantaPlugin` boundary rather than moving Bukkit types into the existing pure command parser.

- [ ] **Step 1: Write failing command parsing tests**

```java
@Test
void parsesDemoCommandsWithoutBreakingExistingValidationCommands() {
    DantaCombatAiCommandBridge bridge = new DantaCombatAiCommandBridge();
    assertTrue(bridge.execute(new String[]{"combat-ai", "profiles"}).handled());
    assertEquals(DantaCombatAiCommandBridge.DemoAction.START,
            bridge.parseDemoAction(new String[]{"combat-ai", "demo", "start"}).orElseThrow());
    assertEquals(DantaCombatAiCommandBridge.DemoAction.STOP,
            bridge.parseDemoAction(new String[]{"combat-ai", "demo", "stop"}).orElseThrow());
}
```

- [ ] **Step 2: Run Windows RED verification**

Expected: missing demo parser/wiring.

- [ ] **Step 3: Add demo parser and `DantaPlugin` runtime wiring**

`DantaPlugin.onEnable()`:

```java
liveCombatRuntime = new LiveCombatRuntime(this, new PaperCombatAiRuntime());
getServer().getPluginManager().registerEvents(new LiveCombatListener(liveCombatRuntime), this);
```

`DantaPlugin.onDisable()`:

```java
if (liveCombatRuntime != null) liveCombatRuntime.shutdown();
```

At the `/danta combat-ai` boundary, route demo actions before the text-only DEV-114 result path. `start` must require a player sender because the approved demo origin is around the invoking player's controlled location. Console start returns Korean guidance; stop/status may work from console.

Player-facing messages:

```text
§aCombatAI 전투 시연을 시작했습니다. 주변에 개발용 병력이 생성됩니다.
§aCombatAI 전투 시연을 종료하고 생성된 병력을 정리했습니다.
§6[CombatAI 전투 시연] §e실행 중 / 정지됨
§c전투 시연 시작은 게임 안의 플레이어만 사용할 수 있습니다.
§c이미 CombatAI 전투 시연이 실행 중입니다.
```

- [ ] **Step 4: Run Windows GREEN verification**

Run:

```powershell
git pull
dev-server\quick-deploy.bat
```

Expected: full automated suite PASS and plugin JAR builds/deploys.

- [ ] **Step 5: Commit**

```bash
git add paper-plugin/src/main/java/kr/danta/paper/DantaPlugin.java paper-plugin/src/main/java/kr/danta/paper/combat paper-plugin/src/test/java/kr/danta/paper
git commit -m "feat: expose DEV-115 live combat demo commands"
```

---

### Task 10: Windows/Paper live validation and corrective TDD loop

**Files:**
- Modify only files implicated by observed live defects.
- Add regression tests before each corrective production change where the defect is testable without Paper runtime.

**Interfaces:**
- Live commands remain the Task 9 interface.

- [ ] **Step 1: Build/deploy on the representative's Windows environment**

```powershell
git pull
dev-server\quick-deploy.bat
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: Restart the Paper development server**

If already running, enter:

```text
stop
```

Then from PowerShell:

```powershell
dev-server\start-dev.bat
```

- [ ] **Step 3: Start the visual demo from a safe open test area**

In Minecraft:

```text
/danta combat-ai demo start
```

Verify all ten logical demo units are visually identifiable as two sides containing 보병/창병/궁병/기병/마법병. Cavalry must visibly be horse+rider.

- [ ] **Step 4: Observe required DEV-114 action differences**

Visually verify:

```text
보병: 전진 후 근접 교전
창병: 전선/후방 사이에서 차단 위치를 취하고 기병 위협에 대응
궁병: 후방 거리 유지, 실제 화살 공격, 압박 시 후퇴
기병: 말+기수로 측면 우회 후 돌입/추격
마법병: 후방 지원 위치 유지, 지원 상태의 임시 시각 효과, 직접 압박 시 후퇴
```

Also verify enemy damage occurs and friendly units, players and unrelated mobs are not intentionally selected as targets.

- [ ] **Step 5: Verify cleanup and repeatability**

```text
/danta combat-ai demo status
/danta combat-ai demo stop
/danta combat-ai demo status
```

Verify all demo-owned entities disappear, including cavalry mounts/riders. Start and stop a second time to ensure no accumulation.

- [ ] **Step 6: If a live defect appears, add the narrowest regression test first**

Example for orphan cavalry:

```java
@Test
void deadRiderCleanupAlsoRequestsHorseRemoval() {
    // reproduce exact state observed live and assert both entity ids are retired
}
```

Run RED, make the minimal production fix, run GREEN, redeploy and repeat only the failed live scenario.

- [ ] **Step 7: Commit verified corrective changes**

```bash
git add <exact-files-changed>
git commit -m "fix: harden DEV-115 live combat runtime"
```

---

### Task 11: DEV-114 semantic audit and project-state reconciliation

**Files:**
- Search: all usages of `strongAgainst()` / `BasicTroopTypes`
- Modify only if test proves MAGIC's neutral self-reference is semantically unsafe.
- Modify: `docs/PROJECT-STATE.md`

**Interfaces:**
- Do not change combat balance merely to make naming prettier.
- DEV-114 becomes COMPLETE based on already-passed automated/live command validation.
- DEV-115 becomes COMPLETE only after Task 10 live visual validation succeeds.

- [ ] **Step 1: Search every `strongAgainst()` consumer**

Confirm whether any consumer treats `strongAgainst()` as semantic truth independently of the multiplier. If all consumers apply the `1.0` multiplier and therefore MAGIC remains mechanically neutral, document that no DEV-114 behavior change is required. If a consumer assumes the relationship itself means advantage, first write a failing regression test and replace the MAGIC representation with an explicit neutral-safe model that preserves existing four-way counters.

- [ ] **Step 2: Reconcile `docs/PROJECT-STATE.md` carefully**

Record at minimum:

```text
DEV-114 CombatAI: COMPLETE — five profiles, Paper adapter, Korean validation command, live command verification passed.
DEV-115 Waypoint Combat Runtime: COMPLETE — five visible troop roles, mounted cavalry, tactical waypoint movement, visible attacks, demo cleanup and Paper live validation passed.
DEV-MAP-003: COMPLETE.
DEV-MAP-004: COMPLETE, 48x48 modular terrain live verified.
DEV-MAP-004A: rejected/reverted; no current implementation.
DEV-112A: accepted live prototype; polish deferred.
DEV-113: COMPLETE including live capital sequence.
red_farm: restored to red after chronicle verification.
Use source-map capital id `red_capital`; do not perpetuate stale `capital_red` as source-controlled map truth.
```

Do not rewrite historical chronicle/event facts merely because the current local test state changed later.

- [ ] **Step 3: Commit state reconciliation**

```bash
git add docs/PROJECT-STATE.md
git commit -m "docs: reconcile DEV-114 and DEV-115 project state"
```

- [ ] **Step 4: Final regression build**

```powershell
git pull
dev-server\quick-deploy.bat
```

Expected: BUILD SUCCESSFUL. DEV-116 may then begin with the same fundamental execution model, measuring 40 concurrent logical combat units before attempting 60 or 80.
