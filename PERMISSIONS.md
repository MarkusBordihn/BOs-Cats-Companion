# Cats Permissions

This document describes all permissions available in the Cats mod for Hytale.

## Permission System Overview

The Cats mod uses a **two-tier protection system**:

### Without LuckPerms

- **Normal commands** (`/cat follow`, `/cat sit`, etc.) are protected by **ownership checks only**
- Only cat owners can control their own cats
- OPs can bypass ownership checks via `cats.admin.bypass` permission
- **Admin commands** (`/cat owner`) require OP status or explicit permission
- **Info commands** (`/cat info`, `/cat list`) are available to all players

### With LuckPerms

- **All protection from above**, PLUS
- **Additional permission checks** for all commands
- Commands can be selectively disabled per group
- Fine-grained access control via LuckPerms groups

The plugin automatically detects LuckPerms on startup and enables permission checks if found.

## Commands and Permissions

| Command               | Permission                               | Without LuckPerms              | With LuckPerms                 | Description                                           |
|-----------------------|------------------------------------------|--------------------------------|--------------------------------|-------------------------------------------------------|
| `/cat info`           | `markusbordihn.cats.command.cat.info`    | All Players                    | Requires Permission            | Shows detailed information about any cat              |
| `/cat list`           | `markusbordihn.cats.command.cat.list`    | All Players                    | Requires Permission            | Lists all your tamed cats                             |
| `/cat sit`            | `markusbordihn.cats.command.cat.sit`     | Owner Only                     | Owner + Permission             | Makes your cat sit in place                           |
| `/cat sleep`          | `markusbordihn.cats.command.cat.sleep`   | Owner Only                     | Owner + Permission             | Makes your cat sleep                                  |
| `/cat bed`            | `markusbordihn.cats.command.cat.bed`     | Owner Only                     | Owner + Permission             | Sends your cat to the nearest cat bed                 |
| `/cat follow`         | `markusbordihn.cats.command.cat.follow`  | Owner Only                     | Owner + Permission             | Makes your cat follow you                             |
| `/cat wait`           | `markusbordihn.cats.command.cat.wait`    | Owner Only                     | Owner + Permission             | Makes your cat wait in place                          |
| `/cat wander`         | `markusbordihn.cats.command.cat.wander`  | Owner Only                     | Owner + Permission             | Allows your cat to wander freely                      |
| `/cat name <name>`    | `markusbordihn.cats.command.cat.name`    | Owner Only                     | Owner + Permission             | Sets a custom name for your cat                       |
| `/cat play`           | `markusbordihn.cats.command.cat.play`    | Owner Only                     | Owner + Permission             | Makes your cat enter play mode                        |
| `/cat search`         | `markusbordihn.cats.command.cat.search`  | Owner Only                     | Owner + Permission             | Makes your cat search/hunt                            |
| `/cat release`        | `markusbordihn.cats.command.cat.release` | Owner Only                     | Owner + Permission             | Releases your cat back to the wild                    |
| `/cat owner <player>` | `markusbordihn.cats.command.cat.owner`   | **Always Requires Permission** | **Always Requires Permission** | **[Admin]** Transfers cat ownership to another player |
| N/A                   | `markusbordihn.cats.admin.bypass`        | OP Only                        | OP Only                        | **[Admin]** Bypasses ownership checks                 |
| N/A                   | `markusbordihn.cats.limit.<amount>`      | Default: 16                    | Default: 16                    | Maximum cats per player (e.g., `.limit.8`)            |
| N/A                   | `markusbordihn.cats.limit.unlimited`     | Can be set                     | Can be set                     | Allows unlimited cat taming                           |

**Access Level Legend:**

- **All Players**: Available to everyone without restrictions
- **Owner Only**: Only the cat owner (or users with `cats.admin.bypass`)
- **Requires Permission**: Must be granted via LuckPerms (or uses default access)
- **Always Requires Permission**: Checked regardless of LuckPerms (admin commands)

### Permission Wildcards

* `markusbordihn.cats.*` - All permissions
* `markusbordihn.cats.command.cat.*` - All cat command permissions (recommended)
* `markusbordihn.cats.admin.*` - All admin permissions

## Cat Limit System

Players can tame wild cats by feeding them raw fish. The mod enforces per-player cat limits based on
permissions.

**This system works with AND without LuckPerms** - server admins can set limits using either
LuckPerms or Hytale's built-in permission system.

### How Limits Work

* **`markusbordihn.cats.limit.<number>`** - Sets maximum cats (1-32, e.g., `.limit.8` for 8 cats)
* **Multiple limits** - If player has multiple limit permissions, the **highest value** is used
* **`markusbordihn.cats.limit.unlimited`** - Bypasses all numeric limits (truly unlimited cats)
* **Default** - Players without any limit permissions can tame **16 cats**
* **Persistence** - Ownership tracking persists across server restarts and chunk loading/unloading

**Note:** Only numeric limits from 1 to 32 are supported. For unlimited cats, use
`markusbordihn.cats.limit.unlimited`.

### Setting Limits

#### With LuckPerms (Recommended)

```bash
# Grant all players access to cat commands and set default limit to 8 cats
/lp group default permission set markusbordihn.cats.command.cat.* true
/lp group default permission set markusbordihn.cats.limit.8 true

# Give VIP players 24 cats
/lp group vip permission set markusbordihn.cats.limit.24 true

# Give admins unlimited cats
/lp group admin permission set markusbordihn.cats.limit.unlimited true

# Set limit for specific player
/lp user <playername> permission set markusbordihn.cats.limit.16 true
```

#### With Hytale's Built-in Permission System

```bash
# Grant command access (ownership checks still apply)
/perm group add default markusbordihn.cats.command.cat.*

# Set cat limits (works the same way as LuckPerms)
/perm group add default markusbordihn.cats.limit.8
/perm group add vip markusbordihn.cats.limit.24
/perm group add admin markusbordihn.cats.limit.unlimited

# Individual player
/perm player add <playername> markusbordihn.cats.limit.16
```

**Important:** If a player has multiple limit permissions (e.g., from different groups), the
**highest limit wins**. Example: if a player has both `.limit.8` and `.limit.16`, they get 16 cats.

## Permission Configuration

### With LuckPerms

The plugin will detect LuckPerms automatically on startup and display detailed setup instructions.

#### Basic Setup

Grant all players access to cat commands:

```bash
# Recommended: Give everyone cat commands
/lp group default permission set markusbordihn.cats.command.cat.* true

# Set default cat limit to 8
/lp group default permission set markusbordihn.cats.limit.8 true
```

Grant admins full access:

```bash
# Admin bypass and unlimited cats
/lp group admin permission set markusbordihn.cats.admin.bypass true
/lp group admin permission set markusbordihn.cats.limit.unlimited true
```

#### Tier System Example

```bash
# Default players: 8 cats
/lp group default permission set markusbordihn.cats.command.cat.* true
/lp group default permission set markusbordihn.cats.limit.8 true

# VIP players: 24 cats
/lp group vip permission set markusbordihn.cats.limit.24 true

# Admins: unlimited cats and bypass
/lp group admin permission set markusbordihn.cats.admin.bypass true
/lp group admin permission set markusbordihn.cats.limit.unlimited true
```

#### Verify Permissions

```bash
# Check player permissions
/lp user <playername> permission info

# Check group permissions
/lp group <groupname> permission info
```

### Without LuckPerms

If LuckPerms is not installed, the plugin still works with **ownership-based protection** and
**Hytale's built-in permission system**.

In this mode:

- All normal commands work for cat owners automatically (ownership checks)
- No permission configuration needed for basic commands
- **Cat limits still work** - use Hytale's `/perm` commands
- Default limit: 16 cats per player
- OPs automatically have `markusbordihn.cats.admin.bypass`
- Admin commands require OP status or explicit permission

#### Setting Limits with Hytale Permissions

```bash
# Set cat limit for default group (all players)
/perm group add default markusbordihn.cats.limit.8

# Set higher limit for VIP group
/perm group add vip markusbordihn.cats.limit.24

# Give admins unlimited cats
/perm group add admin markusbordihn.cats.limit.unlimited

# Set limit for individual player
/perm player add <playername> markusbordihn.cats.limit.16
```

**Note:** Cat limits work identically with or without LuckPerms - the system is the same!

## Notes

* **LuckPerms Detection**: Automatic on plugin startup
* **Ownership Protection**: Always active, regardless of LuckPerms
* **Admin Bypass**: `markusbordihn.cats.admin.bypass` allows admins to control any cat
* **Cat Limits**:
    - Work with AND without LuckPerms
    - Default limit: 16 cats per player
    - Configurable via permissions: `markusbordihn.cats.limit.<number>` (1-32)
    - Unlimited: `markusbordihn.cats.limit.unlimited`
    - Multiple limits: highest value wins
    - Per-server/per-world tracking
* **Chunk Loading**: Ownership persists even in unloaded chunks
* **Console**: Always has all permissions
* **Default Behavior**: Without LuckPerms, ownership-based protection only; with LuckPerms,
  additional permission layer
* **Permission Systems**: Works with both LuckPerms and Hytale's built-in `/perm` commands
