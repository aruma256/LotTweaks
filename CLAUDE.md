# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

LotTweaks is a Minecraft mod that adds productivity tweaks for builders in Creative mode. The repository contains two implementations:
- `fabric/` - Fabric mod loader (Minecraft 1.21.8) **← 現在の主な作業対象**
- `forge/` - Forge mod loader (Minecraft 1.21, mod version 2.2.5)

## Development Environment

This is a Windows native environment. Run Git and Gradle directly:
```cmd
cd fabric
.\gradlew build
git status
```

## Build Commands

### Fabric (primary)
```bash
cd fabric
./gradlew build      # Full build
./gradlew test       # Run tests
./gradlew jar        # Build JAR only
```

### Forge
```bash
cd forge
./gradlew build
```

Both use Java 21 and official Mojang mappings.

## Testing

Tests exist only for the Fabric version:
```bash
cd fabric
./gradlew test
```

Tests use JUnit Jupiter 5.9.2 with Minecraft Bootstrap for game environment initialization. See `fabric/src/test/java/com/github/aruma256/lottweaks/LotTweaksTest.java` for examples.

CI runs on GitHub Actions (Ubuntu 24.04, Java 21) for all branches.

## Architecture

### Fabric Source Structure (split environment)
- `src/main/` - Server-side code (package: `com.github.aruma256.lottweaks`)
  - `LotTweaks.java` - Main entry point (`ModInitializer`), config constants
  - `network/` - Packet handling (`ModNetwork.java`), connection listeners
- `src/client/` - Client-side code
  - `LotTweaksClient.java` - Client entry point (`ClientModInitializer`), keybinding registration
  - `keybinding/` - Keybinding classes (V=SmartPick, R=Palette, G=ReplaceBlock, U=ReachExtension)
  - `palette/` - Item group cycling system with component support:
    - `ItemPalette.java` - Main palette logic with 2-stage lookup (exact match → fallback)
    - `ItemState.java` - Component-aware ItemStack wrapper (HashMap key compatible)
    - `ItemGroupsConfigLoader.java` - JSON config read/write with data-driven registry support
    - `ConfigMigrator.java` - Legacy text format → JSON migration
    - `PaletteConfigManager.java` - Config lifecycle management
    - `ItemGroupParser.java` - Item ID string parsing
  - `event/` - Custom event system (scroll, hotbar render, block outline)
  - `mixin/client/` - Mixins for hooking into Minecraft client
  - `render/` - Rendering utilities (ItemStackRenderer, HudTextRenderer, SelectionBoxRenderer)
- `src/test/` - JUnit tests (84 tests)

### Forge Source Structure
- `src/main/java/com/github/lotqwerty/lottweaks/` - All code in single source set
- Uses Forge annotations (`@Mod`) and event bus instead of Fabric's interfaces

### Key Differences Between Fabric and Forge
| Aspect | Fabric | Forge |
|--------|--------|-------|
| Package | `com.github.aruma256.lottweaks` | `com.github.lotqwerty.lottweaks` |
| Entry point | `ModInitializer` interface | `@Mod` annotation |
| Config | Static fields in `CONFIG` class | `ForgeConfigSpec` builders |
| Events | Custom event bus + Fabric API | MinecraftForge event bus |
| Mixins | Separate client mixins | Not used |

### Core Features Implementation
- **Item Palette** (`palette/`): Manages groups of related items (e.g., stone variants, enchanted bows) that users can cycle through
  - **Config file**: `LotTweaks-ItemGroups.json` (JSON format with component support)
  - **Legacy migration**: Old text files (`LotTweaks-BlockGroups.txt`, `LotTweaks-BlockGroups2.txt`) are auto-migrated and backed up
  - **Component support**: Items with enchantments, custom names, etc. can be registered as distinct entries
  - **2-stage lookup**: Exact component match first, then fallback to base item (backward compatible)
- **Extended Reach** (`AdjustRangeHelper.java`): Server-side reach distance modification
- **Keybinding System** (`keybinding/KeyBase.java`): Base class with press/release/double-tap detection
- **Network** (`ModNetwork.java`, `ModNetworkClient.java`): Packet handling for ReplaceBlock, ReachExtension, Handshake

### Mixin Targets (Fabric client)
- `MouseScrollRedirector` - Intercept scroll for item cycling
- `HotbarRendererHook` - Custom hotbar overlay
- `VanillaPickInvoker` - Hook pick block functionality
- `DrawShapeOutlineInjector` - Custom block outline rendering
