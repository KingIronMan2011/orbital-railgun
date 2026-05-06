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

**Summary:** This mod adds an orbital strike weapon to Minecraft. Players hold right-click to aim and left-click to fire. After a ~35 second countdown (700 ticks), a cylindrical explosion (radius 24) destroys all blocks in a full-height column, and all nearby entities take damage. The weapon comes with custom sound effects, GLSL screen shaders (chromatic aberration, strike beam), and configurable server-side settings.

---

## Supported Minecraft Versions

| MC Version | Build Subproject | Notes |
|---|---|---|
| 1.19.1–1.19.2 | `versions/1.19.2` | Full support |
| 1.20–1.20.4 | `versions/1.20.4` | Full support |
| 1.20.6 | `versions/1.20.6` | Full support |
| 1.20.5 | _(missing)_ | TODO: add & verify |
| 1.21.x | _(missing)_ | TODO: add & verify |

Each version subproject is fully self-contained (own `build.gradle`, `gradle.properties`, `src/`).

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (toolchain), compiled to Java 17 bytecode |
| Build | Gradle (wrapper), Fabric Loom |
| Mod Loader | Fabric Loader |
| Fabric API | Yes (networking, events, registries) |
| Animation | GeckoLib 4 |
| Config (client) | owo-lib (provides Mod Menu screen) |
| Config (server) | Custom JSON via Gson (`ServerConfig.java`) |
| Shader pipeline | Satin API |
| Tests | JUnit Jupiter 5 (unit + integration) |
| Linting | Checkstyle 10.12.5 |
| CI/CD | GitHub Actions (`build-and-release.yml`) |

### Key Dependencies (1.20.6 subproject as reference)

```
fabric-api:          0.100.8+1.20.6
geckolib-fabric:     4.5.4
satin:               1.18.0
owo-lib:             0.12.9+1.20.5
iris:                1.7.2+1.20.6  (compileOnly — shader compat)
gson:                2.10.1
```

---

## Repository Layout

```
orbital-railgun-enhanced/
├── .claude/
│   └── CLAUDE.md                   ← you are here
├── .github/
│   ├── copilot-instructions.md     ← legacy AI instructions (superseded by this file)
│   ├── FUNDING.yml
│   └── workflows/
│       ├── build-and-release.yml   ← CI: build matrix + auto-release per MC version
│       └── codeql.yml
├── config/
│   └── checkstyle/
│       └── checkstyle.xml
├── gradle/wrapper/
├── versions/
│   ├── 1.19.2/                     ← MC 1.19.1–1.19.2 subproject
│   ├── 1.20.4/                     ← MC 1.20–1.20.4 subproject
│   └── 1.20.6/                     ← MC 1.20.6 subproject
├── build.gradle                    ← root aggregator (no source here)
├── gradle.properties               ← mod_version, maven_group, archives_base_name
├── settings.gradle                 ← subproject includes
├── CHANGELOG.md
├── CONTRIBUTING.md
├── TODO.md
└── README.md
```

### Version Subproject Layout (identical structure per MC version)

```
versions/<mc_version>/
├── build.gradle
├── gradle.properties               ← minecraft_version, yarn_mappings, dep versions
└── src/
    ├── main/                       ← server-side + shared
    │   ├── java/io/github/kingironman2011/orbital_railgun_enhanced/
    │   │   ├── OrbitalRailgun.java             ← ModInitializer entrypoint
    │   │   ├── config/ServerConfig.java         ← server-side JSON config
    │   │   ├── item/OrbitalRailgunItem.java     ← item logic + GeoItem
    │   │   ├── item/OrbitalRailgunItems.java    ← item registration
    │   │   ├── registry/SoundsRegistry.java     ← sound event registration
    │   │   ├── registry/CommandRegistry.java    ← /ore and /orbitalrailgun commands
    │   │   ├── utils/OrbitalRailgunStrikeManager.java ← strike tick + explosion
    │   │   ├── listener/PlayerAreaListener.java ← area/range detection for sounds
    │   │   ├── logger/SoundLogger.java          ← debug logging helper
    │   │   └── network/                         ← packet payload records
    │   │       ├── PlaySoundPayload.java
    │   │       ├── ShootPayload.java
    │   │       ├── ClientSyncPayload.java
    │   │       ├── StopAreaSoundPayload.java
    │   │       └── StopAnimationPayload.java
    │   └── resources/
    │       ├── fabric.mod.json
    │       ├── orbital_railgun_enhanced.mixins.json
    │       └── assets/orbital_railgun_enhanced/
    │           ├── lang/                        ← 15 language files
    │           ├── sounds/                      ← equip.ogg, railgun-shoot.ogg, scope-on.ogg
    │           ├── sounds.json
    │           ├── icon.png
    │           └── data/orbital_railgun_enhanced/
    │               ├── damage_type/strike.json
    │               └── recipes/orbital_railgun.json
    ├── client/                      ← client-only
    │   ├── java/io/github/kingironman2011/orbital_railgun_enhanced/client/
    │   │   ├── OrbitalRailgunClient.java        ← ClientModInitializer
    │   │   ├── config/EnhancedConfig.java       ← owo-lib config (volume sliders, toggles)
    │   │   ├── item/OrbitalRailgunRenderer.java ← GeckoLib renderer
    │   │   ├── rendering/
    │   │   │   ├── AbstractOrbitalRailgunShader.java
    │   │   │   ├── OrbitalRailgunShader.java    ← world-space shader (strike beam)
    │   │   │   └── OrbitalRailgunGuiShader.java ← GUI overlay shader
    │   │   ├── handler/SoundsHandler.java       ← client sound playback
    │   │   ├── mixin/
    │   │   │   ├── MouseMixin.java              ← intercepts mouse for aiming
    │   │   │   ├── MinecraftClientMixin.java
    │   │   │   └── AbstractClientPlayerEntity.java
    │   │   └── utils/ModDetector.java           ← checks for optional mods (Iris, SPR, etc.)
    │   └── resources/
    │       ├── orbital_railgun_enhanced.client.mixins.json
    │       └── assets/orbital_railgun_enhanced/
    │           ├── animations/orbital_railgun.animation.json
    │           ├── geo/item/orbital_railgun.geo.json
    │           ├── models/item/orbital_railgun.json
    │           ├── textures/item/orbital_railgun.png
    │           └── shaders/
    │               ├── post/                    ← Satin post-processing chain
    │               │   ├── orbital_railgun_enhanced.json
    │               │   └── orbital_railgun_enhanced_gui.json
    │               └── program/                 ← GLSL programs
    │                   ├── chromatic_abjuration.fsh / .json
    │                   ├── strike.fsh / .vsh / .json
    │                   └── gui.fsh / .vsh / .json
    └── test/
        └── java/…/
            ├── config/ServerConfigTest.java
            ├── integration/ModIntegrationTest.java
            ├── serialization/SerializationTest.java
            └── util/StrikeMathTest.java
```

---

## Core Architecture

### Strike Lifecycle

```
Player right-clicks       → UseAction.BOW (scope-in animation + aiming)
Player left-clicks        → OrbitalRailgunClient sends ShootPayload (C2S)
Server receives ShootPayload
  → OrbitalRailgunItem.shoot() sets cooldown
  → OrbitalRailgunStrikeManager.activeStrikes.put(blockPos + nearby entities, tick)
  → Sends ClientSyncPayload (S2C) to players in range
OrbitalRailgunStrikeManager.tick() runs every server tick
  → age >= 400 ticks: begin gravitational pull (entities dragged toward impact)
  → age >= 700 ticks: explosion fires
      → explode() sets all blocks in circular column (radius 24, full height) to AIR
      → entities in radius take configurable strike damage
Sound system: PlaySoundPayload (C2S) → server checks PlayerAreaListener range
  → plays railgun-shoot.ogg to all players within soundRange (default 500 blocks)
  → PlayerAreaListener tracks enter/leave events for late-joining players
```

### Key Constants

| Constant | Value | Location |
|---|---|---|
| Strike radius | 24 blocks | `OrbitalRailgunStrikeManager.RADIUS` |
| Pull effect starts | 400 ticks (20s) | `OrbitalRailgunStrikeManager.tick()` |
| Explosion fires | 700 ticks (35s) | `OrbitalRailgunStrikeManager.tick()` |
| Default sound range | 500 blocks | `ServerConfig.soundRange` |
| Default strike damage | 20.0f HP | `ServerConfig.strikeDamage` |
| Default cooldown | 100 ticks (5s) | `ServerConfig.cooldownTicks` |
| Sound duration | 52992 ms (~53s) | `OrbitalRailgun.RAILGUN_SOUND_DURATION_MS` |

### Network Packets

| Packet | Direction | Purpose |
|---|---|---|
| `PlaySoundPayload` | C2S | Client requests server to broadcast railgun sound |
| `ShootPayload` | C2S | Client notifies server of shot (blockPos + itemStack) |
| `ClientSyncPayload` | S2C | Server syncs strike to client (blockPos for shader) |
| `StopAreaSoundPayload` | S2C | Server tells client to stop loop sounds |
| `StopAnimationPayload` | S2C | Server tells client to stop strike animation |

All payloads are registered via `PayloadTypeRegistry` in `OrbitalRailgun.onInitialize()`.

### Configuration

**Server config** → `config/orbital-railgun-enhanced-server-config.json`
Managed by `ServerConfig.java` (plain Gson, auto-saved on every setter call):

| Field | Default | Description |
|---|---|---|
| `debugMode` | `false` | Verbose server log output |
| `soundRange` | `500.0` | Radius (blocks) for sound/area detection |
| `strikeDamage` | `20.0` | HP damage dealt to entities on impact |
| `cooldownTicks` | `100` | Ticks before player can fire again |
| `maxActiveStrikes` | `10` | Max concurrent active strikes |
| `enableParticles` | `true` | Toggle particle effects |

**Client config** → `config/orbital-railgun-enhanced.json5`
Managed by `EnhancedConfig.java` via owo-lib. Accessible in Mod Menu:
- Volume sliders for each sound (scope, shoot, equip) — range 0.0–1.0
- Toggle individual sounds on/off

### Commands

| Command | Permission | Description |
|---|---|---|
| `/ore` | OP | Short alias for orbital railgun commands |
| `/orbitalrailgun` | OP | Full command namespace |

See `CommandRegistry.java` for subcommands (debug toggle, config setters, etc.).

---

## Build System

### Requirements

- **Java 21** (toolchain; compiles to Java 17 bytecode — do NOT use Java 17 to run Gradle)
- Network access to: Fabric Maven, Modrinth Maven, GeckoLib Cloudsmith, Ladysnake Maven, Wispforest Maven

### Common Commands

```bash
# Build all MC versions
./gradlew build --no-daemon

# Build specific version
./gradlew :versions:1.20.6:build --no-daemon
./gradlew :versions:1.20.4:build --no-daemon
./gradlew :versions:1.19.2:build --no-daemon

# Run tests (all versions)
./gradlew testAll

# Run tests for specific version
./gradlew :versions:1.20.6:test

# Checkstyle lint (per version)
./gradlew :versions:1.20.6:checkstyleMain :versions:1.20.6:checkstyleClient

# Clean everything
./gradlew clean --no-daemon

# Copy built JARs to root build/libs/
./gradlew copyJars
```

Output JARs:
- Per version: `versions/<mc_version>/build/libs/orbital_railgun_enhanced-<mod_version>-<mc_version>.jar`
- Aggregated: `build/libs/`

### Common Build Issues

| Problem | Fix |
|---|---|
| `DownloadException: Failed to download` | Network blocked Mojang asset servers — ensure internet access or use cached Loom |
| `Previous process has disowned the lock` | `rm -rf ~/.gradle/caches/fabric-loom` then retry |
| Wrong Java version | Set `JAVA_HOME` to Java 21 installation |

---

## CI/CD — GitHub Actions

**Workflow:** `.github/workflows/build-and-release.yml`

**Trigger:** Push to `main`/`master` with changes in `gradle.properties`, `versions/**`, `build.gradle`, `settings.gradle`, or the workflow file itself. Also: `workflow_dispatch`.

**Logic:**
1. `check-version` job reads `mod_version` from `gradle.properties`.
2. For each `versions/*/` subproject, checks if git tag `v<mod_version>-<mc_version>` already exists.
3. Builds a dynamic matrix of only the versions **without** an existing tag.
4. `build-and-release` runs in parallel for each needed version:
   - Builds with JDK 21 (Temurin)
   - Finds the built JAR
   - Creates a GitHub Release tagged `v<mod_version>-<mc_version>`
   - Uploads JAR as release asset

**To trigger a new release:** Bump `mod_version` in `gradle.properties` and push to `main`. All three version subprojects will be built and released automatically.

---

## Development Patterns

### Adding a New Sound

1. Add `.ogg` file to `versions/<mc_version>/src/main/resources/assets/orbital_railgun_enhanced/sounds/`
2. Register the `SoundEvent` in `SoundsRegistry.java`
3. Add subtitle key to all `lang/*.json` files
4. Add entry to `sounds.json`
5. Wire up playback in `SoundsHandler.java` (client) or directly via `player.playSound()` (server)
6. Optionally add owo-lib volume slider to `EnhancedConfig.java`

### Adding a New Config Option (Server-side)

1. Add private field + getter/setter to `ServerConfig.java`
2. Call `saveConfig()` inside the setter
3. Read via `ServerConfig.INSTANCE.get<Option>()` wherever needed
4. Expose via a `/orbitalrailgun` subcommand in `CommandRegistry.java` (optional)

### Adding a New MC Version Subproject

1. `mkdir -p versions/<new_mc_version>/src/{main,client,test}/java/...`
2. Copy & adjust `build.gradle` and `gradle.properties` from the nearest version
3. Add source code (copy from nearest version, update API differences)
4. Add `include 'versions:<new_mc_version>'` to `settings.gradle`
5. Add version range mapping to the `mc-support` step in `build-and-release.yml`
6. Update the supported versions table in `README.md`

### Modifying Strike Behavior

- **Timing/physics:** `OrbitalRailgunStrikeManager.tick()` — pull starts at 400 ticks, impact at 700
- **Explosion shape:** `OrbitalRailgunStrikeManager.explode()` — circular mask, full-height column
- **Item usage/cooldown:** `OrbitalRailgunItem.use()` and `OrbitalRailgunItem.shoot()`
- **Damage:** `OrbitalRailgunStrikeManager.tick()` → configurable via `ServerConfig.strikeDamage`

### Adding Shader Effects

- Shader programs go in `src/client/resources/assets/orbital_railgun_enhanced/shaders/program/` (`.fsh`, `.vsh`, `.json`)
- Post-processing chain defined in `shaders/post/*.json` (Satin API)
- Java binding classes extend `AbstractOrbitalRailgunShader`
- Activate/deactivate shaders in `OrbitalRailgunClient.java` in response to `ClientSyncPayload`

---

## Code Style

- **4-space indentation** (enforced by Checkstyle)
- **Checkstyle** runs on `main` and `client` source sets; violations are shown but do NOT fail the build (`ignoreFailures = true`)
- Checkstyle config: `config/checkstyle/checkstyle.xml`
- Follow existing patterns in the codebase for imports and class organization
- Server-only code lives in `src/main/`; client-only code lives in `src/client/` — do not mix environments

---

## Testing

Tests live in `versions/<mc_version>/src/test/java/…`:

| Test Class | What it tests |
|---|---|
| `StrikeMathTest` | Radius/distance math, mask generation, pull magnitude formula |
| `ServerConfigTest` | Config load/save/defaults |
| `SerializationTest` | Network payload serialization |
| `ModIntegrationTest` | Smoke test — server lifecycle |

Run with: `./gradlew :versions:<mc_version>:test`

---

## Localization

Language files: `src/main/resources/assets/orbital_railgun_enhanced/lang/<locale>.json`

Currently supported (15 languages):
`ar_sa`, `de_de`, `en_us`, `es_es`, `fr_fr`, `hi_in`, `it_it`, `ja_jp`, `ko_kr`, `nl_nl`, `pl_pl`, `pt_br`, `ru_ru`, `sv_se`, `zh_cn`

Machine translations are acceptable as a starting point. Community contributions will refine them. When adding new translation keys, always add to `en_us.json` first, then propagate to other files.

---

## Photosensitivity Warning

This mod uses fast-moving lights, chromatic aberration, and intense shader effects. Flag any new visual additions with appropriate warnings in the README and Modrinth description.

---

*Last updated: 2026-05-06 — generated by Claude from full repo inspection.*
