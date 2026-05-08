# Copilot Instructions — Orbital Railgun Enhanced

> **The authoritative project guide is `.claude/CLAUDE.md`** in the repository root.
> Read that file first before making any changes. It contains the full architecture,
> build system, adapter pattern, per-version package layout, and known issues.

## Quick Reference

**Mod ID:** `orbital_railgun_enhanced`  
**Build:** `./gradlew build --no-daemon` (requires Java 21)  
**Universal JAR output:** `build/libs/merged/orbital_railgun_enhanced-<ver>-all.jar`

### Architecture in one line

Three version subprojects (1.19.2 / 1.20.4 / 1.20.6) each extend a shared `common/`
source tree via Gradle `srcDirs`. Version-specific code lives in `impl.vXXXX` packages
and is loaded at runtime by `AdapterLoader` via lazy `Class.forName()`. A `mergeJars`
Gradle task produces the single universal JAR.

### Key packages

| Location | Contents |
|---|---|
| `common/src/main/java/…` | `OrbitalRailgun`, `AbstractVersionAdapter`, `AdapterLoader`, `ServerConfig`, `PlayerAreaListener`, `SoundLogger`, `IOrbitalRailgunItem` |
| `common/src/client/java/…` | `OrbitalRailgunClient` (holds `CONFIG`), `ClientAdapterLoader`, `MouseMixin`, `ModDetector`, `EnhancedConfig` |
| `versions/<mc>/src/main/java/…/impl/vXXXX/` | `VersionAdapterImpl`, `OrbitalRailgunItem`, `OrbitalRailgunItems`, `SoundsRegistry`, `CommandRegistry`, `OrbitalRailgunStrikeManager` |
| `versions/<mc>/src/client/java/…/client/impl/vXXXX/` | `ClientVersionAdapterImpl`, renderers, shaders, `SoundsHandler`, mixins |

### Adding a new MC version

See the "Adding a New MC Version" section in `.claude/CLAUDE.md` for step-by-step instructions.
