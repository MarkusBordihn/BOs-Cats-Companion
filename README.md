# 🐱 Cats – Tameable Cat Companions for Hytale

[![🎮 Use Hytale Creator Code Kaworru](https://img.shields.io/badge/%20Use%20Hytale%20Creator%20Code-Kaworru-orange)](https://hytale.com/)
⚠️ **BETA VERSION**  This is a beta version, several features are still work in progress.

## 📖 Overview

Bring tameable cat companions into your Hytale world.

This plugin adds multiple cat breeds that can be tamed, named, and commanded.
Cats can follow you on your adventures or stay at home as a cozy companion.
Each cat supports multiple behavior states and animations, making them feel alive and responsive.

## ✅ Current Features

### Working Features

* Seven cat breeds: Black Cat, Calico, Gray Tabby, Orange Tabby, Siamese, Tuxedo, Kitten (
  experimental)
* Taming system using fish
* Automatic cat naming with unique names for each cat
* Full command-based interaction via `/cat`
* Custom cat names with `/cat name`
* Cat ownership limits configurable via permissions
* Multiple behavior states: sitting, following, waiting, wandering, sleeping, playing, searching
* Matching animations for each behavior
* Cat sounds
* Cat Bed item for assigned sleeping spots
* Item consumption for taming and feeding (items are consumed from the player's inventory)
* Persistent component data for owner + state (component CODECs)

### How to Get a Cat

#### Option 1: Manual Spawning (Recommended for Testing)

Use the NPC spawn command to create cats:

```unix
/npc spawn Cats_Black
/npc spawn Cats_Calico
/npc spawn Cats_GrayTabby
/npc spawn Cats_OrangeTabby
/npc spawn Cats_Siamese
/npc spawn Cats_Tuxedo
/npc spawn Cats_Kitten
```

#### Option 2: Spawn Eggs

Each breed also has a spawn egg:

* `Egg_Spawner_Cats_Black` – Black Cat
* `Egg_Spawner_Cats_Calico` – Calico Cat
* `Egg_Spawner_Cats_GrayTabby` – Gray Tabby
* `Egg_Spawner_Cats_OrangeTabby` – Orange Tabby
* `Egg_Spawner_Cats_Siamese` – Siamese Cat
* `Egg_Spawner_Cats_Tuxedo` – Tuxedo Cat
* `Egg_Spawner_Cats_Kitten` – Kitten (experimental)

### How to Tame a Cat

1. **Switch to Survival or Adventure mode**
   ⚠️ Cats cannot be tamed while in Creative mode! They will ignore all interaction attempts.

2. Obtain fish
   Supported types: Raw Fish, Grilled Fish, Salmon, Catfish, Trout, Pike, Bluegill, Minnow

3. Spawn a wild cat using `/npc spawn Cats_Black` (or other breeds) or spawn eggs

4. Approach the wild cat with fish in your hand
   The cat will notice you're holding food

5. Press F (interact key) on the cat while holding fish
   The cat will enter a taming animation and become yours (and the used food gets consumed)

6. Success! The cat is now tamed, automatically named, and will follow you
   Your new companion receives a unique name like "Whiskers", "Luna", or "Shadow"

**Important Interaction Rules:**

* **Wrong items** – Offering non-food items to wild cats may upset them (*hiss!*)
* **Feeding** – Tamed cats can be fed fish to keep them happy (fish gets consumed)
* **Petting** – Press F (interact) with your tamed cat using an empty hand to pet them
* **Creative mode block** – Players in Creative mode cannot tame or interact with cats to prevent
  exploits

### Cat Furniture

#### Cat Bed

Craft a Cat Bed to provide your cats with a cozy sleeping spot:

**Crafting Recipe (at Furniture Bench):**

* 4x Wood Planks (any type)
* 2x White Wool
* 3x Fiber

Cat Beds can be assigned to your tamed cats using the `/cat bed` command.
Your cat will find and sleep in the nearest available bed.

### Available Commands

#### General Commands

* `/cat info` – Show detailed cat information (works on any cat)
* `/cat list` – List all cats owned by a player
* `/cat owner` – Admin command to change ownership

#### Tamed Cat Commands

Commands work by looking at your tamed cat or by providing its entity ID:

* `/cat bed` – Send the cat to the nearest available cat bed
* `/cat follow` – Make the cat follow you
* `/cat name <name>` – Set a custom name
* `/cat play` – Enable playful behavior
* `/cat release` – Release your cat back to the wild
* `/cat search` – Send the cat roaming and hunting
* `/cat sit` – Make the cat sit and stay
* `/cat sleep` – Put the cat to sleep
* `/cat wait` – Stop following and wait in place
* `/cat wander` – Allow free roaming

**Tip:** For best results, look directly at your cat when using commands.

## 🔐 Permissions & LuckPerms

The plugin includes built-in support for both Hytale's permission system and LuckPerms.

**LuckPerms Integration:**
If you have LuckPerms installed, the plugin will automatically detect it on startup and provide
helpful tips and permission examples in the server console.
This makes it easier to configure cat ownership limits and command permissions for your players.

**Default Cat Limit:**
Players can own up to 16 cats by default. Server admins can adjust this limit using permissions.

## ⚠️ Known Limitations

### Important Notes

* **No natural spawning**
  Cats do not spawn naturally yet. Use spawn commands or spawn eggs.

* **No UI menu**
  The interactive menu is temporarily disabled and will return in a later version.

## 🚧 Planned Features

Planned improvements and additions:

* Natural cat spawning in forests and plains
* Interactive UI menu
* Cat breeding and kittens
* Accessories such as collars and bells
* Toys and interactive items
* Additional cat breeds
* Cat progression and special abilities

## 🐛 Known Issues

* Some states/substates may reset after server restart
* Wild cats may occasionally get stuck while approaching players holding fish
* Some animation transitions are not yet smooth
* Pathfinding still needs refinement

Enjoy your new feline companions. 🐱

*This plugin is under active development. Updates and improvements are added regularly.*
