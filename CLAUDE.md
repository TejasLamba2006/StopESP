# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

`ZentraAntiESP` — a Paper/Folia Minecraft plugin (API 1.21) that defeats client-side
cheats by hiding what a player shouldn't legitimately be able to see:

- **AntiXray** — hides configured blocks (ores, chests, spawners, redstone…) behind fake
  blocks until the player has real line-of-sight to them. Also suppresses sound/particle/
  world-event packets that would leak hidden-block positions.
- **AntiESP** — hides players/mobs the viewer has no line-of-sight to, so entity-tracer
  cheats see nothing.

## Build & run — READ FIRST

This is an **extracted/decompiled source tree, not a buildable project**:

- No `pom.xml` / `build.gradle`, no git, no `src/` — Java sources live directly under
  `dev/zDelay/ZAntiESP/`, mirroring how they sit inside the packaged jar.
- **`module/AntiXrayModule.java` and `module/AntiEspModule.java` do not exist here**, yet
  `ZentraAntiESP.java` imports and instantiates both (they wire a manager + listeners +
  `AntiXrayPacketListener.register()` together). Any compile will fail until they're
  recreated. `Module.java` is the interface they implement; `Module.java.part` is a stray
  duplicate.
- To make it buildable: add a Gradle/Maven project with the Paper API 1.21 and ProtocolLib
  as `compileOnly` deps, move sources under `src/main/java/`, and put `plugin.yml` +
  `config.yml` under `src/main/resources/`. There is no test harness — verification means
  loading the jar on a Paper/Folia 1.21 server with ProtocolLib.

`plugin.yml`: hard-depends on ProtocolLib, `folia-supported: true`, one command
`/zentraantiesp <reload|on|off|debug <player>>` (perm `zentraantiesp.admin`). Players with
`zentraantiesp.bypass` are never hidden from.

## Architecture

Everything routes through one config object and one platform-abstraction helper:

- **`util/Scheduler`** — the Folia/Bukkit seam. Detects Folia at class-load via reflection
  and dispatches every scheduled task to the right scheduler (global / async / per-entity /
  per-region). **Never call `Bukkit.getScheduler()` directly** — always go through this, or
  the plugin breaks under Folia's region threading. Returns a `Scheduler.Task` (cancel handle).
- **`manager/DeepShieldConfig`** — loads/reloads `config.yml`, owns the protected-block
  `EnumSet`, per-world reveal-radius overrides, and the enabled-worlds set. `isEnabledWorld()`
  is the cheap gate checked everywhere before doing work (disabled worlds = zero cost).

### AntiXray data flow (`manager/AntiXrayManager`)
The performance-critical core. Understanding this is most of understanding the plugin.

1. **Indexing** — each loaded chunk is snapshotted off-thread into a `ChunkData`: two
   `BitSet`s (protected blocks, occluding blocks) indexed by `relY<<8 | x<<4 | z`, plus a
   map of original `BlockData`. Kept in `chunkMap: world → (chunkKey → ChunkData)`.
2. **Scan loop** — a `runTimer` every `scan-interval` ticks snapshots each eligible player's
   eye location, then hands each to a fixed worker pool. A per-player `AtomicBoolean processing`
   guards against overlapping scans.
3. **Visibility (async, per player)** — for protected blocks within reveal-radius: skip
   buried blocks (`isBuried`), optional frustum cull, then a DDA voxel raycast
   (`isPathClearAsync`) against the occluding `BitSet` for line-of-sight. Directly-visible
   blocks are flood-filled to neighbors so exposed veins reveal together. A `visibility-buffer`
   counter debounces hiding to stop flicker.
4. **Apply** — the diff (reveal → real BlockData, hide → fake material) is sent via
   `player.sendMultiBlockChange`. `getFakeMaterial` picks per-dimension (Stone/Deepslate/
   Netherrack/End Stone) unless `fake-block-mode` overrides.
5. **`AntiXrayPacketListener`** (ProtocolLib) — on `MAP_CHUNK` re-hides blocks in freshly-sent
   chunks; cancels `NAMED_SOUND_EFFECT` / `WORLD_EVENT` (break/extinguish) / `WORLD_PARTICLES`
   packets originating from positions the player can't see, closing side-channel leaks.

Block keys are packed into a `long` (`x&0x3FFFFFF | (z&0x3FFFFFF)<<26 | (y&0xFFF)<<52`) — the
sign-extension unpacking in the flood-fill mirrors this exact layout; keep them in sync.

### AntiESP data flow (`manager/AntiEspManager`)
Simpler and fully on the main/region thread. A timer iterates nearby entities per player,
raycasts LoS via `world.rayTraceBlocks` (treating `TransparentBlocks` as see-through), and
toggles `player.hideEntity` / `showEntity`. `hiddenEntities: viewer → (entityId → entity)`
tracks state so entities are re-shown on leave/quit/disable.

### Lifecycle & cleanup
`ZentraAntiESP.onEnable` builds both modules (see missing-classes note above). Listeners feed
the managers: chunk load/unload → (re)index, block place/break → incremental index update +
`revealInstant`, player quit → `cleanupPlayer`. On disable / `/zentraantiesp off`, managers
reveal everything and shut the worker pool down — always restore client state on teardown,
never leave fake blocks or hidden entities stranded.

## Conventions
- Decompiled style: expect `String.valueOf(ChatColor.X)` concatenations, `varN` locals, and
  `(double)0.5F` literals. Match surrounding style when editing; don't mass-reformat.
- Any new per-tick or per-block work must be gated by `isEnabledWorld` and the bypass perm,
  and scheduled through `Scheduler` — the whole design assumes disabled worlds cost nothing
  and heavy work runs off the region thread.
