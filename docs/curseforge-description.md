# LotTweaks

LotTweaks is a Minecraft mod that adds quality-of-life tools for Creative-mode builders. Reach across an entire build with one keystroke, cycle between related blocks (stone → granite → diorite) with the scroll wheel, replace blocks in place, and pick distant blocks at a glance.

## Features

### Reach Extension

Break, place, and pick blocks up to 100+ blocks away. Hold the key (default: **U**) and scroll the mouse wheel to adjust the range on the fly.

> Both client and server need this mod installed.

![Reach Extension](https://media.forgecdn.net/attachments/320/173/adjust_range.gif)

### Palette

Hold the key (default: **R**) and scroll to swap the held item for the next one in the same item group — for example, stone → granite → diorite, or oak planks → birch planks. Double-tap to switch between palette groups.

Item groups are defined in `LotTweaks-ItemGroups.json` and can be edited freely. Items with components such as enchantments or custom names are supported, so an enchanted bow or a renamed item can be registered as its own palette entry.

> Client-side only.

![Palette](https://media.forgecdn.net/attachments/422/643/rotation.gif)

### Replace Block

Press the key (default: **G**) while looking at a block to replace it with the item in your main hand.

> Both client and server need this mod installed.

### Smart Pick

Press the key (default: **V**) to pick the block you are looking at, even when it is well beyond the vanilla pick range. Long-press the key and scroll to pick from your recent pick-block history instead.

> Client-side only.

## Default Keybindings

| Key | Action | Required side |
| --- | --- | --- |
| **V** | Smart Pick | Client |
| **R** | Palette | Client |
| **G** | Replace Block | Client + Server |
| **U** | Reach Extension | Client + Server |

All keys can be rebound from the standard Minecraft Controls screen.

## Commands

All commands are client-side.

- `/lottweaks add` — Register the items in your hotbar as a new entry in the **primary** palette group.
- `/lottweaks add 2` — Register the items in your hotbar as a new entry in the **secondary** palette group.
- `/lottweaks reload` — Reload item groups from `LotTweaks-ItemGroups.json`.

## Configuration

Item groups are stored in `LotTweaks-ItemGroups.json` inside your `config/` directory. The file is created with sensible defaults the first time you launch the game.

**To customize:**

1. Edit `LotTweaks-ItemGroups.json` directly, or use `/lottweaks add`.
2. Run `/lottweaks reload` to apply the changes.
3. If the file contains errors, the offending lines are reported in chat and in the log.

**To reset to defaults:**

1. Delete `LotTweaks-ItemGroups.json`.
2. Restart Minecraft. A fresh default config will be regenerated.

**Migrating from older versions:**

Older text-based configs (`LotTweaks-BlockGroups.txt` and `LotTweaks-BlockGroups2.txt`) are automatically migrated to the new JSON format on first launch.

## FAQ

**Reach Extension or Replace Block isn't working.**
Make sure the same version of LotTweaks is installed on the server. These two features require both sides.

**Does this mod work with blocks added by other mods?**
Probably yes for standard blocks, but blocks from other mods are not officially supported.

**Can I share a customized item-groups config with others?**
Yes — feel free to distribute your `LotTweaks-ItemGroups.json`.

## Recent Changes

- **2.4.x** — Items with components (enchantments, custom names, etc.) can now be registered as distinct palette entries. Configs are stored as `LotTweaks-ItemGroups.json` (legacy `.txt` configs are auto-migrated).
- **2.3.7** — Internal rename to clearer feature names: `ExPick` → **Smart Pick**, `Rotate` → **Palette**, `AdjustRange` → **Reach Extension**, `Replace` → **Replace Block**. No functional changes.
- **2.3.2** — Fixed Smart Pick not working for distant blocks.
- **2.3.0** — Migrated to the reach-entity-attribute library to reduce conflicts with other mods.
- **2.2.2** — Added option `SHOW_BLOCKCONFIG_ERROR_LOG_TO_CHAT`.
- **2.2.1** — Added pick-from-history to Smart Pick. 2.2.2+ clients can connect to 2.2.1 servers.

## Discord

[![Join the LotTweaks Discord](https://discordapp.com/api/guilds/930102060525957162/widget.png?style=banner2)](https://discord.gg/sEvqhB86mM)

## Disclaimer

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
