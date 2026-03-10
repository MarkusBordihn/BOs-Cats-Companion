# 🐱 Cats – Tameable Cat Companions for Hytale

[![CurseForge](https://cf.way2muchnoise.eu/title/1430961.svg)](https://www.curseforge.com/hytale/mods/cats)
[![CurseForge Downloads](https://cf.way2muchnoise.eu/full_1430961_downloads.svg)](https://www.curseforge.com/hytale/mods/cats)
[![🎮 Use Hytale Creator Code Kaworru](https://img.shields.io/badge/%20Use%20Hytale%20Creator%20Code-Kaworru-orange)](https://hytale.com/)

[![Report an Issue](https://img.shields.io/badge/Report%20Issue%20%2F%20Bug%20%2F%20Feature%20Request-grey?logo=github)](https://github.com/MarkusBordihn/BOs-Cats-Companion/issues)
[![Open Issues](https://img.shields.io/github/issues/MarkusBordihn/BOs-Cats-Companion?logo=github&color=red)](https://github.com/MarkusBordihn/BOs-Cats-Companion/issues?q=is%3Aopen)
[![Closed Issues](https://img.shields.io/github/issues-closed/MarkusBordihn/BOs-Cats-Companion?logo=github)](https://github.com/MarkusBordihn/BOs-Cats-Companion/issues?q=is%3Aclosed)

⚠️ **BETA VERSION**  This is a beta version, several features are still work in progress.

> **Important update note:**
> Always remove all old versions of this plugin from your mods folder before updating, to avoid
> issues like double saving or server startup errors!

## 📖 Overview

Bring tameable cat companions into your Hytale world.

This plugin adds multiple cat breeds that can be tamed, named, and commanded.
Cats can follow you on your adventures or stay at home as a cozy companion.
Each cat supports multiple behavior states and animations, making them feel alive and responsive.

## Introduction & Overview Video (English)

<span><iframe width="788" height="443" src="https://www.youtube.com/embed/RbwMXmpuRPM" frameborder="0" allowfullscreen="allowfullscreen"></iframe></span>

## ✅ Current Features

### Working Features

* Eight cat breeds: Black Cat, Calico, Gray Tabby, Longhaired Russian Blue, Orange Tabby, Siamese,
  Tuxedo, Kitten (experimental)
* Natural spawning across all zones (Zone 1-4) with environment-specific distributions
* Trust-based taming system: feed a wild cat multiple times to slowly gain its trust
* Each cat can only be owned by one player (single owner)
* Automatic unique cat names on taming
* Personality system: each cat has a unique personality that influences happiness gain
* Happiness/mood system: five mood levels (Miserable to Ecstatic) with slow decay
* Gift system: happy cats occasionally bring the owner a gift when petted (30-min cooldown)
* Multiple behavior states: sitting, following, waiting, wandering, sleeping, playing, searching
* Matching animations and sounds for each behavior
* Action Wheel UI: press F with empty hand to open a radial command menu
* Cat Carrier item for transporting tamed cats
* Cat Bed item for assigned sleeping spots
* Cat Yarn Ball toy for playing with cats
* Cat Treats item for an extra happiness boost
* Despawn and respawn system: save your cat and respawn it later with `/cat despawn` / `/cat spawn`
* Cat ownership limits configurable via permissions
* Server-side config for cat limit, spawn settings, and damage protection (`config/cats/`)
* Persistent cat data storage (`worlds/default/resources/CatsData.json`)

### How to Get a Cat

**Natural Spawning:** Cats spawn naturally across all zones (Zone 1-4) with breed distributions
based on climate. Simply explore and you'll find them!

**Manual Spawning:** Use `/npc spawn Cats_<Breed>` (e.g., `/npc spawn Cats_Black`,
`/npc spawn Cats_Siamese`)

**Spawn Eggs:** Each breed has a spawn egg: `Egg_Spawner_Cats_<Breed>` (e.g.,
`Egg_Spawner_Cats_Calico`)

### How to Tame a Cat

1. Get fish (Raw Fish, Grilled Fish, Salmon, Catfish, Trout, Pike, Bluegill, or Minnow)
2. Hold fish and approach a wild cat
3. Feed it multiple times to gain its trust (wrong items will cause it to reject you)
4. Once tamed, your cat gets an automatic unique name and belongs to you alone

**Tips:**

* Enable "Allow NPC Detection" in Creative Mode Quick Settings (TAB) to interact in Creative
* Tamed cats can be fed fish to heal and boost happiness
* Use empty hand to pet your cat (press F)
* Higher quality fish increases taming chance per feeding

### Cat Furniture

#### Cat Carrier

Craft a Cat Carrier to pick up and transport your tamed cats:

**Crafting Recipe (at Farming Workbench):**

* 2x Wood Planks (any type)
* 1x Leather
* 1x White Wool
* 1x Red Wool

Use the carrier on your tamed cat (press F) to pick it up. The carrier icon changes to show it is
occupied.
Right-click on the ground to release the cat at a new location.

#### Cat Bed

Craft a Cat Bed to provide your cats with a cozy sleeping spot:

**Crafting Recipe (at Furniture Bench):**

* 4x Wood Planks (any type)
* 2x White Wool
* 3x Fiber

Cat Beds can be assigned to your tamed cats using the `/cat bed` command.
Your cat will find and sleep in the nearest available bed.

#### Cat Yarn Ball

A playful toy for your cats! Craft a Cat Yarn Ball to interact and play with your tamed cats:

**Crafting Recipe (at Farmers Workbench):**

* 2x Fiber

Interact with your tamed cat while holding the Cat Yarn Ball to trigger a playful animation.
Your cat will show affection with heart particles and happy sounds.

#### Cat Treats

A special treat that gives a bigger happiness boost than regular fish.

**Crafting Recipe (at Farming Bench):**

* 1x Raw Fish
* 1x Fibre

### How to Interact with your Cat

Press **F** with an empty hand on your tamed cat to open the **Action Wheel**. From there you can
follow, pet, play, sleep, wander, send to bed, or rename your cat. The available options adapt to
the cat's current state.

### Available Commands

* `/cat reload` - Reload all config files without server restart

#### General Commands

* `/cat info` - Show detailed cat information (works on any cat)
* `/cat list` - List all cats owned by a player
* `/cat owner` - Admin command to change ownership
* `/cat spawn` - Spawn a previously despawned cat
* `/cat despawn` - Despawn a cat (saves state, can be respawned later)

#### Tamed Cat Commands

Commands work by looking at your tamed cat or by providing its entity ID:

* `/cat bed` - Send the cat to the nearest available cat bed
* `/cat follow` - Make the cat follow you
* `/cat name <name>` - Set a custom name
* `/cat play` - Enable playful behavior
* `/cat pounce [target]` - Send a nearby cat to pounce at a target (15-block range)
* `/cat release` - Release your cat back to the wild
* `/cat search` - Send the cat roaming and hunting
* `/cat sit` - Make the cat sit and stay
* `/cat sleep` - Put the cat to sleep
* `/cat wait` - Stop following and wait in place
* `/cat wander` - Allow free roaming

**Tip:** For best results, look directly at your cat when using commands.

## ⚙️ Configuration

Config files are created in `config/cats/` on first startup and can be edited while the server is
running:

* `general.cfg` - Default cat limit per player (default: 16)
* `protection.cfg` - damage filters: owner, other players (friendly fire), mobs, environment,
  projectiles (defaults: players protected, mobs/environment/projectiles deal damage)
* `spawn.cfg` - Enable/disable natural spawning and adjust spawn weight multiplier

Apply changes with `/cat reload` - no restart required.

## 🔐 Permissions

The plugin supports both Hytale's permission system and LuckPerms.

**Default Cat Limit:** Players can own up to 16 cats. Admins can adjust this using permissions like
`markusbordihn.cats.limit.8` or `markusbordihn.cats.limit.unlimited`.

For detailed permission configuration, see the [Permissions Documentation](PERMISSIONS.md).

## 🚧 Planned Features

* Cat breeding and kittens
* Accessories such as collars and bells
* More toys and interactive items
* Additional cat breeds
* Cat progression and special abilities

## 🗃️ Data Storage

Cat data is automatically saved to `worlds/default/resources/CatsData.json`.
This includes:

* Cat UUID and owner information
* Cat type, name, and current state
* Last known position and spawn status

Backup this file to preserve your cats when moving worlds.

## 🐛 Known Issues

* Some animation transitions are not yet smooth
* Pathfinding still needs refinement

## 🔗 Related Plugins

**🐶 Dogs Companion**

Looking for loyal combat companions? Check out the Dogs Companion plugin! While cats are perfect for
peaceful companionship and decoration, dogs are designed for active gameplay with combat support,
guarding abilities, and adventure features.

👉 [Download Dogs Companion](https://www.curseforge.com/hytale/mods/dogs-companion)

## 🛠️ For Developers

Want to build or modify this plugin? Check out the [Development Guide](DEVELOPMENT.md) for setup
instructions, build tasks, and contribution guidelines.

## 📜 License

**This project is open source under the MIT License.**

⚠️ **Important:** The license applies **only to the source code** in this repository.

**Assets are excluded from the license:**

* 3D models (`.bbmodel` files)
* Textures and images
* Sounds and music
* Animations
* Other creative/artistic content

**These assets may not be redistributed, modified, or used in other projects without permission.**

For the full license text, see [LICENSE.md](LICENSE.md).

---

Enjoy your new feline companions. 🐱

*This plugin is under active development. Updates and improvements are added regularly.*
