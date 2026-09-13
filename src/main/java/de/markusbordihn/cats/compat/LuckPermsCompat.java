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

package de.markusbordihn.cats.compat;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class LuckPermsCompat {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private static final PluginIdentifier LUCKPERMS_ID =
      new PluginIdentifier("LuckPerms", "LuckPerms");

  private static final String COMMAND_PERMISSION = "markusbordihn.cats.command.cat";
  private static final int BOX_INNER_WIDTH = 75;

  private static final String[] SUB_COMMANDS = {
    "bed", "despawn", "follow", "info", "list", "name", "needs", "owner", "play", "pounce",
    "release", "reload", "search", "sit", "sleep", "spawn", "wait", "wander"
  };
  private static final Set<String> OP_SUB_COMMANDS = Set.of("needs", "owner", "reload");

  private static boolean isDetected = false;

  private LuckPermsCompat() {}

  public static void detect() {
    if (isDetected) {
      return;
    }

    isDetected = true;

    if (PluginManager.get().getAvailablePlugins().containsKey(LUCKPERMS_ID)) {
      logLuckPermsEnabled();
    }
  }

  private static void logLuckPermsEnabled() {
    LOGGER.at(Level.INFO).log("=".repeat(BOX_INNER_WIDTH + 4));
    logBoxLine("[Cats Plugin] LuckPerms detected! Permission checks are ENABLED.");
    LOGGER.at(Level.INFO).log("|" + "=".repeat(BOX_INNER_WIDTH + 2) + "|");
    logBoxLine("IMPORTANT: Use wildcard permission to grant access to /cat command");
    logBoxLine("and all sub-commands (/cat list, /cat info, etc.)");
    logBoxLine("");
    logBoxLine("Recommended: Grant wildcard to player or group:");
    logBoxLine("  /lp user <player> permission set " + COMMAND_PERMISSION + ".* true");
    logBoxLine("  /lp group default permission set " + COMMAND_PERMISSION + ".* true");
    logBoxLine("");
    logBoxLine("Alternative: Grant individual permissions:");
    logBoxLine("  /lp user <player> permission set " + COMMAND_PERMISSION + " true");
    logBoxLine("  /lp user <player> permission set " + COMMAND_PERMISSION + ".list true");
    logBoxLine("  ...and so on for each sub-command");
    logBoxLine("");
    logBoxLine("Verify permissions were set:");
    logBoxLine("  /lp user <player> permission info");
    logBoxLine("");
    logBoxLine("Available permissions:");
    logPermissionLine(COMMAND_PERMISSION, "Base /cat command");
    logPermissionLine(COMMAND_PERMISSION + ".*", "Wildcard: all sub-commands");

    for (String subCommand : SUB_COMMANDS) {
      String adminMarker = OP_SUB_COMMANDS.contains(subCommand) ? " (admin)" : "";
      logPermissionLine(COMMAND_PERMISSION + "." + subCommand, "/cat " + subCommand + adminMarker);
    }

    logPermissionLine("markusbordihn.cats.admin.bypass", "Bypass ownership checks");
    logPermissionLine("markusbordihn.cats.limit.unlimited", "Unlimited cat ownership");
    logPermissionLine("markusbordihn.cats.limit.{number}", "Limit to N cats (e.g. .8-32)");
    LOGGER.at(Level.INFO).log("=".repeat(BOX_INNER_WIDTH + 4));
  }

  private static void logPermissionLine(@Nonnull String permission, @Nonnull String description) {
    logBoxLine(String.format("  - %-40s - %s", permission, description));
  }

  private static void logBoxLine(@Nonnull String content) {
    LOGGER.at(Level.INFO).log(String.format("| %-" + BOX_INNER_WIDTH + "s |", content));
  }
}
