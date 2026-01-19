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

### Corporate Network Configuration

If running tests behind a corporate proxy with authentication, use a local Python proxy to handle auth:

```bash
# Create and run proxy script (extract proxy from environment variables)
cat > /tmp/proxy.py << 'EOF'
#!/usr/bin/env python3
import socket, threading, os, base64, select
from urllib.parse import urlparse

LOCAL_PORT = 3128
UPSTREAM = os.environ.get('https_proxy') or os.environ.get('HTTPS_PROXY')

def get_upstream():
    p = urlparse(UPSTREAM)
    return p.hostname, p.port, p.username or '', p.password or ''

def handle(client):
    try:
        req = b''
        while b'\r\n\r\n' not in req:
            data = client.recv(4096)
            if not data: return
            req += data
        target = req.split(b'\r\n')[0].split()[1].decode()
        host, port = (target.split(':') + ['443'])[:2]
        proxy_host, proxy_port, user, pwd = get_upstream()
        auth = base64.b64encode(f"{user}:{pwd}".encode()).decode()
        upstream = socket.socket()
        upstream.connect((proxy_host, int(proxy_port)))
        upstream.send(f"CONNECT {host}:{port} HTTP/1.1\r\nProxy-Authorization: Basic {auth}\r\n\r\n".encode())
        resp = b''
        while b'\r\n\r\n' not in resp:
            resp += upstream.recv(4096)
        if b'200' in resp.split(b'\r\n')[0]:
            client.send(b'HTTP/1.1 200 Connection Established\r\n\r\n')
            for s in [client, upstream]: s.setblocking(False)
            while True:
                r, _, _ = select.select([client, upstream], [], [], 30)
                if not r: break
                for s in r:
                    data = s.recv(8192)
                    if not data: return
                    (upstream if s is client else client).sendall(data)
    except: pass
    finally:
        try: client.close()
        except: pass

if __name__ == '__main__':
    srv = socket.socket()
    srv.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    srv.bind(('127.0.0.1', LOCAL_PORT))
    srv.listen(10)
    print(f"Local proxy on 127.0.0.1:{LOCAL_PORT}")
    while True:
        c, _ = srv.accept()
        threading.Thread(target=handle, args=(c,), daemon=True).start()
EOF
python3 /tmp/proxy.py &

# Configure Gradle to use local proxy
cat >> ~/.gradle/gradle.properties << 'EOF'
systemProp.https.proxyHost=127.0.0.1
systemProp.https.proxyPort=3128
systemProp.http.proxyHost=127.0.0.1
systemProp.http.proxyPort=3128
EOF

# Now run tests
cd fabric
./gradlew test
```

**Why Python proxy?** Gradle's native proxy settings have authentication limitations with complex credentials (JWT tokens). A local Python proxy handles this by extracting credentials from environment variables (`https_proxy`/`HTTPS_PROXY`) automatically.

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
