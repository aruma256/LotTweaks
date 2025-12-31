# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

LotTweaks is a Minecraft mod that adds productivity tweaks for builders in Creative mode. The repository contains two implementations:
- `fabric/` - Fabric mod loader (Minecraft 1.21.8, mod version 2.3.6) **← 現在の主な作業対象**
- `forge/` - Forge mod loader (Minecraft 1.21, mod version 2.2.5)

## Development Environment

This repository is on a Windows filesystem accessed from WSL. Git and Gradle must be run via PowerShell:
```bash
powershell.exe -Command "cd fabric; ./gradlew build"
powershell.exe -Command "git status"
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
  - `network/` - Packet handling, connection listeners
- `src/client/` - Client-side code
  - `LotTweaksClient.java` - Client entry point (`ClientModInitializer`), keybinding registration
  - `keys/` - Keybinding classes (V=ExPick, R=Rotate, G=Replace, U=AdjustRange)
  - `event/` - Custom event system (scroll, hotbar render, chat, block outline)
  - `mixin/client/` - Mixins for hooking into Minecraft client
  - `renderer/` - Rendering utilities
  - `RotationHelper.java` - Block group rotation system (~425 lines, core feature)
- `src/test/` - JUnit tests

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
- **Block Group Rotation** (`RotationHelper.java`): Manages chains of related blocks (e.g., stone variants) that users can cycle through. Config files: `LotTweaks-BlockGroups.txt`, `LotTweaks-BlockGroups2.txt`
- **Extended Reach** (`AdjustRangeHelper.java`): Server-side reach distance modification
- **Keybinding System** (`keys/LTKeyBase.java`): Base class with press/release/double-tap detection

### Mixin Targets (Fabric client)
- `MouseScrollRedirector` - Intercept scroll for item cycling
- `HotbarRendererHook` - Custom hotbar overlay
- `VanillaPickInvoker` - Hook pick block functionality
- `DrawShapeOutlineInjector` - Custom block outline rendering
