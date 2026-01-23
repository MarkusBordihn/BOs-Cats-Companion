# 🐱 Cats – Tameable Cat Companions for Hytale

[![Hytale](https://img.shields.io/badge/Hytale-Plugin-blue)](https://hytale.com)

> ⚠️ **EARLY ALPHA – MVP VERSION**
> This is a minimal viable product (MVP).
> Interaction is currently command-based only. Several features are still work in progress.

## 📖 Overview

Bring tameable cat companions into your Hytale world.

This plugin adds five different cat breeds that can be tamed, named, and commanded.
Cats can follow you on your adventures or stay at home as a cozy companion.
Each cat supports multiple behavior states and animations, making them feel alive and responsive.

## ✅ Current Features

### Working Features

* Five cat breeds: Black Cat, Calico, Gray Tabby, Orange Tabby, Siamese
* Taming system using fish
* Full command-based interaction via `/cat`
* Custom cat names
* Multiple behavior states: sitting, following, waiting, wandering, sleeping, playing, searching
* Matching animations for each behavior
* Owner tracking for each tamed cat

### How to Get a Cat

#### Option 1: Manual Spawning (Recommended for Testing)

Use the NPC spawn command to create cats:

```
/npc spawn Cat_Black
/npc spawn Cat_Calico
/npc spawn Cat_GrayTabby
/npc spawn Cat_OrangeTabby
/npc spawn Cat_Siamese
```

#### Option 2: Spawn Eggs

Each breed also has a spawn egg:

* `Egg_Spawner_Cat_Black` – Black Cat
* `Egg_Spawner_Cat_Calico` – Calico Cat
* `Egg_Spawner_Cat_GrayTabby` – Gray Tabby
* `Egg_Spawner_Cat_OrangeTabby` – Orange Tabby
* `Egg_Spawner_Cat_Siamese` – Siamese Cat

### How to Tame a Cat

1. Obtain fish
   Supported types: Raw Fish, Grilled Fish, Salmon, Catfish, Trout, Pike, Bluegill, Minnow
2. Spawn a wild cat
3. Hold fish in your hand
4. The cat will approach you
5. Right-click the cat while holding fish
6. The cat is now tamed and belongs to you

### Available Commands

Commands work by looking at your cat or by providing its entity ID:

* `/cat sit` – Make the cat sit and stay
* `/cat sleep` – Put the cat to sleep
* `/cat follow` – Make the cat follow you
* `/cat wait` – Stop following and wait in place
* `/cat wander` – Allow free roaming
* `/cat play` – Enable playful behavior
* `/cat search` – Send the cat roaming and hunting
* `/cat name <name>` – Set a custom name
* `/cat info` – Show detailed cat information
* `/cat owner` – Admin command to view or change ownership
* `/cat menu` – Shows a WIP message (menu UI planned)

Tip: For best results, look directly at your cat when using commands.

## ⚠️ Known Limitations

### Important Notes

* **State persistence**
  Behavior states are currently not saved after a server restart.

* **No natural spawning**
  Cats do not spawn naturally yet. Use spawn commands or spawn eggs.

* **No UI menu**
  The interactive menu is temporarily disabled and will return in a later version.

## 🚧 Planned Features

Planned improvements and additions:

* State persistence across server restarts
* Natural cat spawning in forests and plains
* Interactive UI menu
* Cat breeding and kittens
* Accessories such as collars and bells
* Toys and interactive items
* Cat beds and assigned sleeping spots
* Additional cat breeds
* Cat progression and special abilities

## 🐛 Known Issues

* Cat states reset after server restart
* Wild cats may occasionally get stuck while approaching players holding fish
* Some animation transitions are not yet smooth
* Pathfinding still needs refinement

Enjoy your new feline companions. 🐱

*This plugin is under active development. Updates and improvements are added regularly.*
