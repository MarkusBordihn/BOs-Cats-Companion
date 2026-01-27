# Changelog for Cats (Hytale)

## Note

This change log includes the summarized changes.
For the full changelog, please go to the [GitHub History][history] instead.

Note: Please always back up your world before updating to a new version!

### 1.3.0

- Fixed cat following behavior when tracking is lost by retriggering search state.
- Fixed commands to show the correct cat name instead of just "Cat".
- Added experimental kitten variant (Calico breed only for now).
- Improved existing cat textures with better details and shading.

Note: The kitten variant is experimental and has limited functionality and animations issues.

### 1.2.0

- Fixed persistent component storage for cat owner and state components.
- Fixed command translation placeholders (use `{name}` / `{owner}` instead of `%name%` / `%owner%`).
- Fixed item consumption for taming and feeding (items are now consumed from the player's
  inventory).
- Fixed cats spawn eggs icons.
- Added Tuxedo cat variant (NPC role, spawn egg, model + textures).
- Added cat sounds for specific actions (purring, meowing, hissing).
- Refactored commands, interactions, and systems to use the registered component types.
- Improved NPC role behavior with additional idle actions (stretching, licking, playful pouncing,
  searching).

### 1.1.0

- Fixed translation system with German and English support.
- Fixed state synchronization between component and NPC substates.
- Fixed single player world taming issue.
- Refactored interaction system with proper player messages.
- Refactored files to a separate namespace for better mod compatibility.
- Improved command structure with semantic namespaces.
- Improved animation handling and added missing animations.
- Improved taming system with proper feedback messages.

### 1.0.0 ✨

- Initial release of Cats - Tameable Cat Companions for Hytale!

[history]: https://github.com/MarkusBordihn/BOs-Cats-Hytale/commits/
