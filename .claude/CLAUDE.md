# CLAUDE.md — Orbital Railgun Enhanced

> Comprehensive project context for AI-assisted development. Keep this file up to date when making structural changes.

---

## Project Identity

| Field | Value |
|---|---|
| **Name** | Orbital Railgun Enhanced |
| **Type** | Minecraft Fabric Mod (Java) |
| **Mod ID** | `orbital_railgun_enhanced` |
| **Maven Group** | `io.github.kingironman2011` |
| **Archives Base Name** | `orbital_railgun_enhanced` |
| **Current Version** | See `gradle.properties` → `mod_version` (was `1.3.8` at time of writing) |
| **License** | MIT |
| **Author** | KingIronMan2011 |
| **Fork of** | [Orbital Railgun](https://modrinth.com/mod/orbital-railgun) by Mishkis |
| **Repo** | https://github.com/KingIronMan2011/orbital-railgun-enhanced |

**Summary:** This mod adds an orbital strike weapon to Minecraft. Players hold right-click to aim and left-click to fire. After a ~35 second countdown (700 ticks), a cylindrical explosion (radius 24) destroys all blocks in a full-height column and all nearby entities take damage. Custom sound effects, GLSL screen shaders, and configurable server-side settings included.

---

## Supported Minecraft Versions

| Subproject | MC Versions Covered |
|---|---|
| `versions/1.19.2` | 1.19.1 · 1.19.2 |
| `versions/1.20.4` | 1.20 · 1.20.1 · 1.20.2 · 1.20.3 · 1.20.4 |
| `versions/1.20.6` | 1.20.6 |
| 1.20.5 | *(missing — not yet added)* |
| 1.21.x+ | *(missing — not yet added)* |

All three subprojects compile into a single **universal JAR** via the `mergeJars` Gradle task.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (toolchain), compiled to Java 17 bytecode |
| Build | Gradle (wrapper), Fabric Loom |
| Mod Loader | Fabric Loader |
| Fabric API | Yes (networking, events, registries) |
| Animation | GeckoLib 4 (GeckoLib 3 for 1.19.2 subproject) |
| Config (client) | owo-lib (`EnhancedConfigWrapper`) |
| Config (server) | Custom JSON via Gson (`ServerConfig.java`) |
| Shader pipeline | Satin API |
| Tests | JUnit Jupiter 5 |
| Linting | Checkstyle 10.12.5 |
| CI/CD | GitHub Actions (`build-and-release.yml`) |

---

## Repository Layout

```
orbital-railgun-enhanced/
├── .claude/
│   └── CLAUDE.md                        ← you are here
├── .github/
│   ├── copilot-instructions.md          ← ⚠ STALE — predates refactoring
│   ├── FUNDING.yml
│   └── workflows/
│       ├── build-and-release.yml        ← CI pipeline (see below)
│       └── codeql.yml
├── common/                              ← source included in ALL version builds via srcDirs
│   └── src/
│       ├── main/
│       │   ├── java/…/
│       │   │   ├── OrbitalRailgun.java          ← thin entry point; delegates to VersionAdapter
│       │   │   ├── compat/
│       │   │   │   ├── VersionAdapter.java       ← server-side interface
│       │   │   │   └── AdapterLoader.java        ← lazy Class.forName dispatcher
│       │   │   ├── config/ServerConfig.java
│       │   │   ├── item/IOrbitalRailgunItem.java ← marker interface for common instanceof checks
│       │   │   ├── listener/PlayerAreaListener.java
│       │   │   └── logger/SoundLogger.java
│       │   └── resources/
│       │       ├── orbital_railgun_enhanced.mixins.json
│       │       └── assets/orbital_railgun_enhanced/
│       │           ├── lang/              ← 15 language files
│       │           ├── sounds/            ← 3 .ogg files
│       │           ├── sounds.json
│       │           ├── icon.png
│       │           └── data/…/            ← damage_type/strike.json, recipes/
│       ├── client/
│       │   ├── java/…/client/
│       │   │   ├── OrbitalRailgunClient.java    ← thin entry point; holds CONFIG, delegates
│       │   │   ├── compat/
│       │   │   │   ├── ClientVersionAdapter.java
│       │   │   │   └── ClientAdapterLoader.java
│       │   │   ├── config/EnhancedConfig.java   ← owo-lib config definition
│       │   │   ├── mixin/MouseMixin.java         ← uses IOrbitalRailgunItem marker interface
│       │   │   └── utils/ModDetector.java
│       │   └── resources/
│       │       ├── orbital_railgun_enhanced.client.mixins.json  ← MouseMixin only
│       │       └── assets/orbital_railgun_enhanced/
│       │           ├── geo/item/, models/item/, textures/item/
│       │           └── shaders/post/ + program/   ← all GLSL
│       └── test/java/…/                 ← 4 JUnit test classes
│
├── versions/
│   ├── 1.19.2/
│   │   ├── build.gradle                 ← srcDirs include common/; 1.19.2 Yarn mappings
│   │   ├── gradle.properties
│   │   └── src/
│   │       ├── main/java/…/impl/v1192/  ← VersionAdapterImpl + all server-side 1.19.2 classes
│   │       ├── main/resources/fabric.mod.json
│   │       ├── client/java/…/client/impl/v1192/       ← ClientVersionAdapterImpl + client classes
│   │       ├── client/java/…/client/impl/v1192/mixin/ ← MinecraftClientMixin, AbstractClientPlayerEntity
│   │       └── client/resources/orbital_railgun_enhanced.client.v1192.mixins.json
│   ├── 1.20.4/                          ← same structure, impl/v1204
│   └── 1.20.6/                          ← same structure, impl/v1206; also has network/ payload records
│
├── config/checkstyle/checkstyle.xml
├── gradle/wrapper/
├── build.gradle                         ← root: buildAll, copyJars, mergeJars tasks
├── gradle.properties                    ← mod_version, maven_group, archives_base_name
├── settings.gradle                      ← subproject includes ⚠ has stale comment
├── CHANGELOG.md
├── CONTRIBUTING.md
└── README.md
```

---

## Architecture

### Key Design: Adapter Pattern + Lazy Class Loading

```
Runtime MC 1.19.2:
  OrbitalRailgun.onInitialize()
    → AdapterLoader.get()
    → Class.forName("…impl.v1192.VersionAdapterImpl")   ← loaded ✓
       Class.forName("…impl.v1204.VersionAdapterImpl")   ← never touched ✓
       Class.forName("…impl.v1206.VersionAdapterImpl")   ← never touched ✓
    → VersionAdapterImpl (v1192).initialize()
```

Java only classloads a class when it is actually referenced. By calling `Class.forName()` only for the matching version string, classes that reference absent APIs are never resolved on incompatible runtimes.

`AdapterLoader.resolvePackage(String mc)` (public static) maps the MC version string to a package suffix. Extend this method when adding new MC version subprojects.

### Common Entry Points

| Class | Responsibility |
|---|---|
| `OrbitalRailgun` | Declares `MOD_ID`, `LOGGER`, `RAILGUN_SOUND_DURATION_MS`; calls `AdapterLoader.get().initialize()` |
| `OrbitalRailgunClient` | Declares static `CONFIG` (`EnhancedConfigWrapper`); loads owo-lib config then calls `ClientAdapterLoader.get().initialize()` |

### Version-Specific Packages

| Version | Server package | Client package |
|---|---|---|
| 1.19.2 | `…impl.v1192` | `…client.impl.v1192` |
| 1.20.4 | `…impl.v1204` | `…client.impl.v1204` |
| 1.20.6 | `…impl.v1206` | `…client.impl.v1206` |

Unique FQNs across all three packages are required for `DuplicatesStrategy.EXCLUDE` merging to work correctly.

### Mixin JSON Split

| File | Location | Contains |
|---|---|---|
| `orbital_railgun_enhanced.client.mixins.json` | `common/src/client/resources/` | `MouseMixin` only |
| `orbital_railgun_enhanced.client.v1192.mixins.json` | `versions/1.19.2/src/client/resources/` | `MinecraftClientMixin`, `AbstractClientPlayerEntity` |
| `orbital_railgun_enhanced.client.v1204.mixins.json` | `versions/1.20.4/src/client/resources/` | same |
| `orbital_railgun_enhanced.client.v1206.mixins.json` | `versions/1.20.6/src/client/resources/` | same |

Each version's `fabric.mod.json` registers both mixin JSON files.

### Key Constants (all in `OrbitalRailgun.java` common)

| Constant | Value |
|---|---|
| `RAILGUN_SOUND_DURATION_MS` | `52992L` (~53 s) |
| Strike radius | 24 blocks (in `OrbitalRailgunStrikeManager`) |
| Pull starts | 400 ticks |
| Explosion fires | 700 ticks |

---

## Build System

### Common Commands

```bash
./gradlew build --no-daemon              # Full local build: all 3 versions + mergeJars
./gradlew :versions:1.20.6:build         # Single version
./gradlew mergeJars --no-daemon          # Merge only (JARs must already exist)
./gradlew testAll                        # All tests
./gradlew cleanAll --no-daemon           # Clean everything
```

### JAR Outputs

| Output | Path |
|---|---|
| Per-version | `versions/<mc>/build/libs/orbital_railgun_enhanced-<mc>-<ver>.jar` |
| Aggregated | `build/libs/` (via `copyJars`) |
| Universal merged | `build/libs/merged/orbital_railgun_enhanced-<ver>-all.jar` |

---

## CI/CD Pipeline

**Workflow:** `.github/workflows/build-and-release.yml`  
**Trigger:** Push to `main`/`master` touching `gradle.properties`, `versions/**`, `common/**`, `build.gradle`, `settings.gradle`, or the workflow file itself. Also `workflow_dispatch`.

**Jobs:**
1. **`check-version`** — reads `mod_version`; checks for tag `v{mod_version}`; sets `should-release` output
2. **`build` (matrix)** — builds 1.19.2 / 1.20.4 / 1.20.6 in parallel with JDK 21; uploads each JAR as an artifact (1-day retention)
3. **`merge-and-release`** — downloads all JARs; copies them to `versions/*/build/libs/`; runs `./gradlew mergeJars --no-daemon`; creates one GitHub Release tagged `v{mod_version}` with the universal `-all.jar` as primary asset

**To release:** Bump `mod_version` in `gradle.properties` and push to `main`.

---

## Adding a New MC Version

1. Create `versions/<new_mc>/` with `build.gradle` + `gradle.properties` (copy nearest, update mappings/deps)
2. Copy the `afterEvaluate { sourceSets { … } }` block from an existing version's `build.gradle` verbatim — the `rootProject.file('common/src/…')` entries are included automatically
3. Create `impl/v<VTAG>/` and `client/impl/v<VTAG>/` packages under the new version's `src/`
4. Implement `VersionAdapterImpl` and `ClientVersionAdapterImpl`
5. Move all other version-specific classes into those packages
6. Create `orbital_railgun_enhanced.client.v<VTAG>.mixins.json` and register it in `fabric.mod.json`
7. Add `include 'versions:<new_mc>'` to `settings.gradle`
8. Add the new package tag to `AdapterLoader.resolvePackage()` in common
9. `mergeJars` picks it up automatically

---

## Known Issues & Planned Improvements

The following issues have been identified but **not yet fixed**, ordered by priority.

### 🔴 Bugs

**1. Packet ID mismatch between common and adapters**
`OrbitalRailgun.java` (common) declares `SHOOT_PACKET_ID = new Identifier(MOD_ID, "shoot")` and `CLIENT_SYNC_PACKET_ID = new Identifier(MOD_ID, "client_sync")`. All three `VersionAdapterImpl` classes declare their own copies with different paths: `"shoot_packet"` and `"client_sync_packet"`. The common constants are never used (dead code) but would silently break packet routing if referenced. Should be removed from common or unified as the single source of truth.

**2. Unconditional `LOGGER.info` spam in the 1.20.6 adapter**
Several log calls in `VersionAdapterImpl` (v1206) fire on every shot regardless of `debugMode`:
- `"[NETWORK] Received PLAY_SOUND payload from {}"` — every shot
- `"[SOUND] Played {} to {} (distance {})"` — every player in range, every shot
- `"[SOUND] playRailgunSoundToPlayer called for {}"` — late-joiner catch-up
- `"[SOUND] Server played railgun shoot to {}"` — every nearby player

The v1192 and v1204 adapters guard all equivalent calls with `isDebugMode()`. The v1206 adapter needs the same treatment.

**3. Double entity iteration in v1206 SHOOT handler**
The `ShootPayload` receiver in `VersionAdapterImpl` (v1206) iterates `nearby.forEach(...)` twice in a row — once to play sounds and send `ClientSyncPayload`, and again for area state checks. These should be merged into a single pass.

### 🟡 Code Quality / Maintainability

**4. `handleAreaStateChange` and related helpers duplicated across all three adapters**
`handleAreaStateChange`, `playRailgunSoundToPlayer`, `stopAreaSoundsForPlayer`, and `stopAnimationForPlayer` contain nearly identical logic in all three adapters. Only the packet-send calls differ (PacketByteBuf vs typed payload). The shared logic should be extracted to a common helper, with only the send calls delegated via the adapter interface.

**5. `MOD_ID` redeclared in 9 places**
`OrbitalRailgunItems` and `SoundsRegistry` in each of the 3 version subprojects (6 files), plus all 3 `VersionAdapterImpl` classes. All should reference `OrbitalRailgun.MOD_ID`.

**6. `LOGGER` redeclared in all 3 `VersionAdapterImpl` classes**
Should use `OrbitalRailgun.LOGGER` (already `public static final`).

**7. `RAILGUN_SOUND_DURATION_MS` redeclared in all 3 `VersionAdapterImpl` classes**
Should reference `OrbitalRailgun.RAILGUN_SOUND_DURATION_MS`.

**8. Packet ID constants scattered across three classes with no single source of truth**
`PLAY_SOUND_PACKET_ID` appears in each `SoundsRegistry`, each `VersionAdapterImpl`, and `OrbitalRailgun` (common) — all with slightly different values or construction APIs. Should be centralised in `OrbitalRailgun` or a dedicated `PacketIds` class in common.

**9. Inconsistent `Identifier` construction within and across adapters**
In the v1204 adapter, `PLAY_SOUND_PACKET_ID = new Identifier(...)` but `SHOOT_PACKET_ID = Identifier.of(...)` in the same class. In `OrbitalRailgun.java` (common), `new Identifier(...)` is used for all packet IDs even though it is deprecated on 1.20.6+. These should be made consistent.

### 🟠 Build System

**10. `build.dependsOn copyJars` appears twice in `build.gradle`**
Once at `// Make build also copy JARs to root` and again at the `// Full local build…` comment after mergeJars. Also `build.dependsOn buildAll` (line ~18) is redundant since `copyJars` already depends on `buildAll`. The dependency graph needs to be cleaned up.

**11. `mergeJars` has no declared task inputs for incremental builds**
Gradle cannot determine whether `mergeJars` is up-to-date without declared `inputs.files(...)` pointing at the source JARs. If a version JAR changes without `copyJars` running, `mergeJars` won't re-run automatically.

### 🟢 Housekeeping

**12. 24 leftover empty directories from the refactor**
All three version subprojects still contain empty `handler/`, `item/`, `mixin/`, `rendering/`, `registry/`, and `utils/` directories left when classes were moved to `impl/vXXXX`. Also, the `impl/vXXXX/mixin/` directories on the server-side (main) are empty. These should be deleted.

**13. `settings.gradle` has a stale comment**
`"Currently using a single build compiled against 1.20.1, compatible with 1.20-1.20.4"` is completely wrong post-refactoring.

**14. `.github/copilot-instructions.md` is stale**
Predates `common/`, `impl.vXXXX` packages, and the adapter pattern entirely. Should be updated or replaced with a pointer to this file.

---

## Localization

15 language files in `common/src/main/resources/assets/orbital_railgun_enhanced/lang/`:
`ar_sa`, `de_de`, `en_us`, `es_es`, `fr_fr`, `hi_in`, `it_it`, `ja_jp`, `ko_kr`, `nl_nl`, `pl_pl`, `pt_br`, `ru_ru`, `sv_se`, `zh_cn`

When adding new translation keys, add to `en_us.json` first, then propagate to all other files.

---

## Photosensitivity Warning

This mod uses fast-moving lights, chromatic aberration, and intense shader effects. Flag any new visual additions with appropriate warnings in the README and Modrinth description.

---

*Last updated: 2026-05-07 — full repo re-discovery after JAR-merge architecture refactoring.*
