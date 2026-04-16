/*
 * Copyright 2026 Markus Bordihn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.cats.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class CatCommands extends AbstractCommandCollection {

  private final List<CatCommand> catSubCommands = new ArrayList<>();

  public CatCommands() {
    super("cat", "Cat management commands");
    this.addAliases("cats");

    register(new CatPounceCommand());
    register(new CatBedCommand());
    register(new CatDespawnCommand());
    register(new CatFollowCommand());
    register(new CatInfoCommand());
    register(new CatListCommand());
    register(new CatNameCommand());
    register(new CatNeedsCommand());
    register(new CatOwnerCommand());
    register(new CatPlayCommand());
    register(new CatReloadCommand());
    register(new CatReleaseCommand());
    register(new CatSearchCommand());
    register(new CatSitCommand());
    register(new CatSleepCommand());
    register(new CatSpawnCommand());
    register(new CatWaitCommand());
    register(new CatWanderCommand());
  }

  private void register(CatCommand cmd) {
    this.addSubCommand(cmd);
    this.catSubCommands.add(cmd);
  }

  public Set<String> buildPlayerPermissionNodes() {
    Set<String> nodes =
        catSubCommands.stream()
            .filter(cmd -> !cmd.requiresOp())
            .map(CatCommand::getPermission)
            .collect(Collectors.toCollection(java.util.HashSet::new));
    nodes.add(this.getPermission());
    return nodes;
  }
}
