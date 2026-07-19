# StopESP

**Server-side anti-xray and anti-ESP for Paper, Folia, and Bukkit.** Hidden ores, chests, and players never leave your server, so a hacked client has nothing to draw on your screen.

Most anti-cheats watch how players behave and guess whether they are using X-ray or ESP. StopESP takes a different route: it just doesn't send the data. If a player has no real line of sight to a diamond vein or another player, their game never receives it. There is nothing for a cheat client to reveal, because the information was never there.

---

## What it does

**Anti-Xray.** Ores, chests, spawners, redstone, and anything else you list get replaced with ordinary stone (or netherrack, or end stone, picked automatically per dimension) until a player can actually see them. Walk up, mine into a wall, and the real blocks appear the moment they come into view. X-ray clients and cave-finder mods see a solid, boring world.

**Anti-ESP.** Players and mobs you have no line of sight to are hidden from your client entirely. Tracer and ESP cheats that draw glowing outlines through walls get nothing to outline. Optional extras: hide floating name tags of hidden players, hide already-invisible entities, and add specific mob types to always check.

**It also plugs the quiet leaks.** Chest-open sounds, block-break particles, and similar effects coming from spots you cannot see are cancelled, so nothing gives away a hidden room through the wall.

## Why server-side matters

A client-side or detection-based approach is always playing catch-up with the next cheat release. StopESP works at the level of what your server chooses to send. The block data and entity positions stay on the server until you legitimately earn the right to see them. That makes it effective against cheats it has never heard of.

The honest limit: this hides things you cannot see, not things you can. Once a player has real line of sight, they see the real block or entity, same as vanilla. StopESP stops the wallhack, not the person standing in front of the chest.

## Requirements

- **Minecraft 26.1.2 or newer**
- **Java 25** (required by Minecraft 26.1+)
- **Paper, Folia, or any Paper fork.** Folia's regional threading is detected and supported automatically.
- **[ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)** installed on the server

## Installation

1. Drop `ProtocolLib` into your `plugins/` folder.
2. Drop `StopESP-1.0.0.jar` into `plugins/`.
3. Start the server once to generate `config.yml`.
4. Edit `enabled-worlds` to match your world names, then run `/stopesp reload`.

By default StopESP only works in worlds you list, so worlds you leave out cost nothing.

## Commands

| Command | What it does |
|---|---|
| `/stopesp reload` | Reload the config |
| `/stopesp on` | Turn hiding on |
| `/stopesp off` | Turn hiding off and reveal everything |
| `/stopesp debug <player>` | Show what StopESP is tracking for a player |

## Permissions

| Node | Default | Meaning |
|---|---|---|
| `stopesp.admin` | op | Use the commands above |
| `stopesp.bypass` | false | Blocks and entities are never hidden from this player |

## Configuration

Everything lives in `config.yml`. The parts you will actually touch:

- `enabled-worlds`: the worlds StopESP runs in.
- `protected-blocks`: the full list of blocks to hide. Ores, containers, spawners, and redstone are covered out of the box; add or remove freely.
- `reveal-radius`: how close a player gets before hidden blocks are checked (with optional per-world overrides).
- `anti-esp`: a section of its own. Toggle player hiding, mob hiding, name-tag hiding, invisible-entity hiding, and a list of extra entity types.

Performance knobs (`scan-interval`, `visibility-buffer`, `frustum-culling`, `worker-threads`) have sensible defaults. Visibility work runs off the main thread on a worker pool, and disabled worlds are skipped before any work happens.

## Building from source

Requires JDK 25.

```bash
./gradlew build      # jar lands in build/libs/
./gradlew runServer  # boots a fresh Paper 26.1.2 server with the plugin loaded
```

`runServer` pulls the latest Paper build and ProtocolLib for you, so you can test in one command.

## Support

Found a bug or want a feature? Join the [Discord](https://discord.gg/msEkYDWpXM).

## License

Released under the [MIT License](LICENSE).
