# DEV-MAP-003 Multiworld Explorers Guild Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a two-world foundation where a flat strategic mainland and a natural wilderness coexist, with travel controlled through an Explorers Guild and with Minecraft inventory items kept separate from national strategic resources.

**Architecture:** Add a focused Paper-side world subsystem rather than changing core game-state architecture. `DantaWorldService` creates/loads the two worlds, `ExplorerGuildTravelService` owns travel eligibility and destinations, and `ExplorerGuildBuilder` creates the initial travel structures. Existing nation/economy systems remain untouched so wilderness items cannot silently become strategic resources.

**Tech Stack:** Java 25, Paper 26.2 build 123, Gradle 9.7.1, JUnit 5, Bukkit/Paper WorldCreator API

**Spec:** `docs/superpowers/specs/2026-09-16-multiworld-explorers-guild-design.md`

## Global Constraints

- Do not install or download Java, Gradle, Paper, or PostgreSQL in ChatGPT runtime.
- Player-facing output is Korean by default.
- Minecraft inventory items must not mutate `StrategicResourceStockpile` during DEV-MAP-003 travel.
- Existing `dev-test-map.yml`, `MapStructurePlacer`, and `CanyonFortressBuilder` must remain available.
- Strategic main world uses flat generation; wilderness uses normal natural generation.
- Final wilderness border/reset/balance values are not fixed in this ticket.
- No arbitrary player `/warp`; normal travel is through Explorers Guild interaction points.

---

### Task 1: World-role configuration and pure travel policy

**Files:**
- Create: `paper-plugin/src/main/java/kr/danta/paper/world/WorldRole.java`
- Create: `paper-plugin/src/main/java/kr/danta/paper/world/DantaWorldConfig.java`
- Create: `paper-plugin/src/main/java/kr/danta/paper/world/ExplorerGuildTravelPolicy.java`
- Test: `paper-plugin/src/test/java/kr/danta/paper/world/ExplorerGuildTravelPolicyTest.java`

**Interfaces:**
- Produces: `WorldRole { STRATEGIC_MAIN, WILDERNESS }`
- Produces: `DantaWorldConfig.defaults()` with world names `danta_main` and `danta_wild`
- Produces: `ExplorerGuildTravelPolicy.canTravel(WorldRole from, TravelGate gate)` and `destinationRole(...)`

- [ ] **Step 1: Write failing policy tests**

```java
@Test
void mainlandGuildGateTravelsOnlyToWilderness() {
    var policy = new ExplorerGuildTravelPolicy();
    assertTrue(policy.canTravel(WorldRole.STRATEGIC_MAIN, TravelGate.EXPLORERS_GUILD));
    assertEquals(WorldRole.WILDERNESS,
            policy.destinationRole(WorldRole.STRATEGIC_MAIN, TravelGate.EXPLORERS_GUILD));
}

@Test
void wildernessReturnGateTravelsOnlyToMainland() {
    var policy = new ExplorerGuildTravelPolicy();
    assertTrue(policy.canTravel(WorldRole.WILDERNESS, TravelGate.WILDERNESS_RETURN));
    assertEquals(WorldRole.STRATEGIC_MAIN,
            policy.destinationRole(WorldRole.WILDERNESS, TravelGate.WILDERNESS_RETURN));
    assertFalse(policy.canTravel(WorldRole.WILDERNESS, TravelGate.EXPLORERS_GUILD));
}
```

- [ ] **Step 2: Run the targeted test and confirm failure**

Run on representative Windows PC through the normal Gradle test path once implementation is pulled:

```powershell
.\gradlew.bat :paper-plugin:test --tests "kr.danta.paper.world.ExplorerGuildTravelPolicyTest"
```

Expected: compile failure because the new world classes do not yet exist.

- [ ] **Step 3: Implement minimal world-role/config/policy classes**

```java
public enum WorldRole { STRATEGIC_MAIN, WILDERNESS }

public enum TravelGate { EXPLORERS_GUILD, WILDERNESS_RETURN }
```

`DantaWorldConfig.defaults()` must return stable names and development gate coordinates without assigning final world-border/reset values.

- [ ] **Step 4: Run policy tests**

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add paper-plugin/src/main/java/kr/danta/paper/world paper-plugin/src/test/java/kr/danta/paper/world
git commit -m "DEV-MAP-003: add world roles and travel policy"
```

### Task 2: Create/load flat strategic world and natural wilderness

**Files:**
- Create: `paper-plugin/src/main/java/kr/danta/paper/world/DantaWorldService.java`
- Modify: `paper-plugin/src/main/java/kr/danta/paper/DantaPlugin.java`
- Test: `paper-plugin/src/test/java/kr/danta/paper/world/DantaWorldConfigTest.java`

**Interfaces:**
- Consumes: `DantaWorldConfig`
- Produces: `DantaWorldService.initialize()`
- Produces: `DantaWorldService.world(WorldRole)` and `roleOf(World)`

- [ ] **Step 1: Add config tests for stable names and distinct roles**

```java
@Test
void defaultWorldNamesAreStableAndDistinct() {
    var config = DantaWorldConfig.defaults();
    assertEquals("danta_main", config.strategicWorldName());
    assertEquals("danta_wild", config.wildernessWorldName());
    assertNotEquals(config.strategicWorldName(), config.wildernessWorldName());
}
```

- [ ] **Step 2: Implement `DantaWorldService`**

Use `WorldCreator`:

```java
new WorldCreator(config.strategicWorldName())
        .type(WorldType.FLAT)
        .environment(World.Environment.NORMAL)
        .createWorld();

new WorldCreator(config.wildernessWorldName())
        .type(WorldType.NORMAL)
        .environment(World.Environment.NORMAL)
        .createWorld();
```

If an existing world is already loaded, reuse it instead of regenerating it. Fail with a clear server log if either world cannot be created or loaded.

- [ ] **Step 3: Wire initialization in `DantaPlugin.onEnable()` before travel listeners are registered**

Create `DantaWorldConfig.defaults()`, initialize `DantaWorldService`, and retain both as plugin fields.

- [ ] **Step 4: Run `quick-deploy.bat`**

Expected: automated tests/build succeed.

- [ ] **Step 5: Commit**

```bash
git add paper-plugin/src/main/java/kr/danta/paper/world/DantaWorldService.java paper-plugin/src/main/java/kr/danta/paper/DantaPlugin.java paper-plugin/src/test/java/kr/danta/paper/world/DantaWorldConfigTest.java
git commit -m "DEV-MAP-003: initialize strategic and wilderness worlds"
```

### Task 3: Build Explorers Guild and wilderness return outpost

**Files:**
- Create: `paper-plugin/src/main/java/kr/danta/paper/world/ExplorerGuildBuilder.java`
- Modify: `paper-plugin/src/main/java/kr/danta/paper/DantaPlugin.java`

**Interfaces:**
- Consumes: `DantaWorldConfig`, `DantaWorldService`
- Produces: `ExplorerGuildBuilder.buildMainGuild()` and `buildWildernessReturnOutpost()`
- Produces explicit admin command route `/danta world build-guild`

- [ ] **Step 1: Implement deterministic small structures**

Main guild: stone/wood expedition hall, visible interaction block, safe arrival pad. Wilderness outpost: smaller matching shelter and return interaction block. Both builders may explicitly load their target chunks because they are administrator setup operations.

- [ ] **Step 2: Add Korean admin command handling**

```text
/danta world build-guild
```

Success output:

```text
[단타] 탐험가 길드와 야생 귀환 거점을 생성했습니다.
```

Unauthorized output:

```text
탐험가 길드 설치 권한이 없습니다.
```

Reuse `danta.admin.map` unless a separate permission is necessary; do not create redundant permissions without need.

- [ ] **Step 3: Run `quick-deploy.bat`**

Expected: build succeeds.

- [ ] **Step 4: Commit**

```bash
git add paper-plugin/src/main/java/kr/danta/paper/world/ExplorerGuildBuilder.java paper-plugin/src/main/java/kr/danta/paper/DantaPlugin.java
git commit -m "DEV-MAP-003: add explorers guild structures"
```

### Task 4: Player interaction travel service

**Files:**
- Create: `paper-plugin/src/main/java/kr/danta/paper/world/ExplorerGuildTravelService.java`
- Create: `paper-plugin/src/main/java/kr/danta/paper/world/ExplorerGuildListener.java`
- Modify: `paper-plugin/src/main/java/kr/danta/paper/DantaPlugin.java`

**Interfaces:**
- Consumes: `DantaWorldService`, `DantaWorldConfig`, `ExplorerGuildTravelPolicy`
- Produces: safe bidirectional teleport only from configured interaction blocks

- [ ] **Step 1: Implement destination validation**

`ExplorerGuildTravelService` must verify:

```java
World destination = worldService.world(targetRole)
        .orElseThrow(() -> new IllegalStateException("destination world is not loaded"));
```

Calculate a safe arrival location on/above the configured guild/outpost pad. Do not teleport into solid blocks, lava, fire, or void.

- [ ] **Step 2: Implement `PlayerInteractEvent` listener**

On right-click of the configured gate interaction block:

- mainland guild block → wilderness outpost
- wilderness return block → mainland guild
- anything else → no action

Cancel the interaction only when the plugin actually handles the configured gate.

- [ ] **Step 3: Add Korean travel messages**

Outbound:

```text
탐험가 길드를 통해 야생 원정지로 이동했습니다.
```

Return:

```text
야생 원정을 마치고 전략 본토로 귀환했습니다.
```

Failure must use specific Korean reasons.

- [ ] **Step 4: Register listener after worlds are initialized**

- [ ] **Step 5: Run `quick-deploy.bat`**

Expected: build succeeds.

- [ ] **Step 6: Commit**

```bash
git add paper-plugin/src/main/java/kr/danta/paper/world/ExplorerGuildTravelService.java paper-plugin/src/main/java/kr/danta/paper/world/ExplorerGuildListener.java paper-plugin/src/main/java/kr/danta/paper/DantaPlugin.java
git commit -m "DEV-MAP-003: add explorers guild travel"
```

### Task 5: Guard the economic boundary and add admin diagnostics

**Files:**
- Modify: `paper-plugin/src/main/java/kr/danta/paper/DantaPlugin.java`
- Test: existing strategic-resource tests remain unchanged; add no item→strategic-resource converter
- Modify: `docs/PROJECT-STATE.md`

**Interfaces:**
- Produces: `/danta world status`
- Does not produce any path from Bukkit `ItemStack` to `StrategicResourceStockpile`

- [ ] **Step 1: Add world status command**

Example output:

```text
[단타 월드 상태]
전략 본토: danta_main - 로드됨
야생: danta_wild - 로드됨
자원경제: 야생 아이템과 국가 전략자원은 분리됨
```

- [ ] **Step 2: Search the DEV-MAP-003 diff for accidental resource conversion**

Verify no new code calls `StrategicResourceStockpile.deposit(...)`, `NationState.deposit(...)`, or equivalent from world-travel or guild interaction code.

- [ ] **Step 3: Update `PROJECT-STATE.md`**

Record DEV-MAP-003 as IMPLEMENTED pending live Windows verification, and record the design decisions:

- strategic flat mainland
- natural wilderness
- Explorers Guild-only normal travel
- personal Minecraft items separated from national strategic resources
- wilderness border/reset and guild contribution values unresolved

- [ ] **Step 4: Run full automated suite/deploy**

```powershell
dev-server\quick-deploy.bat
```

Expected: all tests and build pass.

- [ ] **Step 5: Commit**

```bash
git add paper-plugin/src/main/java/kr/danta/paper/DantaPlugin.java docs/PROJECT-STATE.md
git commit -m "docs: track DEV-MAP-003 multiworld implementation"
```

### Task 6: Windows Paper live verification

**Files:**
- No source modification unless verification finds a defect.

**Interfaces:**
- Validates the complete DEV-MAP-003 flow.

- [ ] **Step 1: Pull and deploy on representative Windows PC**

```powershell
git pull
dev-server\quick-deploy.bat
dev-server\start-dev.bat
```

- [ ] **Step 2: Confirm Paper creates/loads both worlds**

```text
danta world status
```

Expected: `danta_main`, `danta_wild` both loaded.

- [ ] **Step 3: Build travel structures**

```text
danta world build-guild
```

Expected: Korean success output and both structures visible.

- [ ] **Step 4: Verify bidirectional player travel**

Right-click mainland guild gate → wilderness. Right-click wilderness return gate → mainland. Inventory contents must remain with the player.

- [ ] **Step 5: Verify national strategic-resource separation**

Capture relevant strategic-resource values before and after collecting an ordinary wilderness item and returning. Values must not change because of the travel or item acquisition.

- [ ] **Step 6: Restart Paper and recheck both worlds**

Worlds and structures must remain present after restart without automatic destructive regeneration.

- [ ] **Step 7: Mark DEV-MAP-003 COMPLETE only after live verification succeeds**
