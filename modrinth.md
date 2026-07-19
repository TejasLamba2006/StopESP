![banner](https://cdn.modrinth.com/data/cached_images/4d0ad1c0fad24d7ec8805343660281049681a7a5.png)

# StopESP

### Stop X-ray and ESP cheats by never sending them the data

Your diamonds are gone before you place a single trap. Someone with X-ray walks a mineshaft and finds every vein you buried. Someone with an ESP client sees your team through the walls of your base. StopESP shuts both down, and it does it without trying to guess who is cheating.

Here is the trick: **your server never sends what a player cannot see.** No hidden ores, no chests behind walls, no players across the map. A cheat client can only draw what the server tells it about, and StopESP tells it about nothing it has not legitimately earned. Walk up to that diamond, and it appears like normal. Stay across the cave, and to the cheater it looks like plain stone.

![chest showcase](https://cdn.modrinth.com/data/cached_images/dd68a2a61aa02c8cedaee7e0ec1f7abfc30f39b6.gif)

## Why players love it

**It works on cheats it has never seen.** Most anti-cheats watch behavior and try to catch a hacker in the act, always one step behind the next release. StopESP does not play that game. It hides the information itself, so a brand new X-ray mod is just as blind as an old one.

**Your legit players never notice it.** Mine normally, explore normally, fight normally. The real blocks and real players show up the instant you can actually see them, exactly like vanilla. No lag on the player's end, no weird delays, no false bans on your regulars.

**It hides more than blocks.** StopESP also covers players and mobs. Tracer cheats that draw glowing lines to everyone on the map get nothing to point at. You can even hide the floating name tags of players you cannot see, so no one reads a username through a wall.

**It closes the sneaky leaks too.** The sound of a chest opening, the particles from a block breaking behind a wall, the little tells that give away a hidden room. StopESP cancels the ones coming from places you cannot see.

![skelly diamond](https://cdn.modrinth.com/data/cached_images/c24868804821fafadc08e8039e8bd8119fa6fe23.gif)

## What it hides out of the box

- All ores, including deepslate variants and ancient debris
- Chests, barrels, shulker boxes, ender chests
- Spawners
- Redstone gear, hoppers, pistons, and more
- Workstations, beehives, sculk, and other valuables
- Other players and mobs across the map

Do not like the list? Change it. Every block and entity is yours to add or remove in a plain, commented config file.

## Honest about the limits

StopESP hides what a player should not be able to see. It does not stop what they can. Once someone has a real line of sight to a chest or a teammate, they see it, the same as any vanilla player would. This is a wallhack blocker, not a magic shield, and it is the strongest kind of protection there is against X-ray and ESP because there is simply nothing left to leak.

## Works with your setup

- **Minecraft 26.1.2 and newer**
- **Paper, Folia, and Paper forks.** Folia support is automatic, no extra setup.
- **Needs ProtocolLib** (a free, standard library most servers already run)
- Runs the heavy work off your main thread, and skips any world you do not turn on, so performance stays clean

## Setup takes two minutes

1. Install ProtocolLib
2. Drop StopESP in your plugins folder
3. Start up, list your worlds in the config, and run `/stopesp reload`

That is it. Ops get the full command set for turning it on and off and checking what it is doing.

## Need help?

Questions, bugs, or a feature you want? Come say hi on [Discord](https://discord.gg/msEkYDWpXM). Real support, from the person who made it.
