package org.hackedserver.spigot.utils.logs;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.hackedserver.spigot.HackedServerPlugin;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.hackedserver.core.config.Action;

public class ActionManager {
  public static void performActions(Action action, Player player, Boolean isTemporaryPlayer,
      TagResolver.Single... templates) {
    if (action.hasAlert()) {
      Logs.logComponent(action.getAlert(templates));
      for (Player admin : Bukkit.getOnlinePlayers())
        if (admin.hasPermission("hackedserver.alert"))
          HackedServerPlugin.get().getAudiences().player(admin)
              .sendMessage(action.getAlert(templates));
    }
    if (!isTemporaryPlayer && player.hasPermission("hackedserver.bypass"))
      return;
    for (String command : action.getConsoleCommands())
      Bukkit.getScheduler().runTask(HackedServerPlugin.get(),
          () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
              command.replace("<player>",
                  player.getName())));
    for (String command : action.getPlayerCommands())
      Bukkit.getScheduler().runTask(HackedServerPlugin.get(),
          () -> Bukkit.dispatchCommand(player,
              command.replace("<player>",
                  player.getName())));
    for (String command : action.getOppedPlayerCommands()) {
      boolean op = player.isOp();
      player.setOp(true);
      Bukkit.getScheduler().runTask(HackedServerPlugin.get(),
          () -> Bukkit.dispatchCommand(player,
              command.replace("<player>",
                  player.getName())));
      player.setOp(op);
    }
  }

}
