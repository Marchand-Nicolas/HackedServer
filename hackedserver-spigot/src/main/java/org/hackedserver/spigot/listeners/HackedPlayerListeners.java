package org.hackedserver.spigot.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.hackedserver.core.HackedPlayer;
import org.hackedserver.core.HackedServer;
import org.hackedserver.core.config.GenericCheck;
import org.hackedserver.spigot.HackedHolder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HackedPlayerListeners implements Listener {

    @EventHandler
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        HackedServer.registerPlayer(event.getUniqueId(), new HackedPlayer(event.getUniqueId()));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        HackedServer.removePlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onClickInv(InventoryClickEvent event) {
        HackedPlayer hackedPlayer = HackedServer.getPlayer(event.getWhoClicked().getUniqueId());
        Inventory inv = event.getInventory();
        if (inv.getHolder() instanceof HackedHolder) {
            event.setCancelled(true);
            String username = Bukkit.getOfflinePlayer(hackedPlayer.getUuid()).getName();
            username = username != null ? username : "Unknown";
            StringBuilder message = new StringBuilder();
            message.append(username);
            message.append(":\n");

            List<GenericCheck> sortedChecks = new ArrayList<>(HackedServer.getChecks().stream().sorted(Comparator.comparing(GenericCheck::getName)).toList());
            sortedChecks.remove(HackedServer.getCheck("fabric"));
            sortedChecks.remove(HackedServer.getCheck("forge"));

            message.append(ChatColor.GOLD).append("Fabric: ").append(hackedPlayer.getGenericChecks().contains("fabric") ? ChatColor.GREEN + "true" : ChatColor.RED + "false");
            message.append("\n");
            message.append(ChatColor.GOLD).append("Forge: ").append(hackedPlayer.getGenericChecks().contains("forge") ? ChatColor.GREEN + "true" : ChatColor.RED + "false");
            message.append("\n");
            message.append(ChatColor.BLUE).append("--------------------\n");

            for (GenericCheck check : sortedChecks.stream().filter(check -> hackedPlayer.getGenericChecks().contains(check.getId())).toList()) {
                message.append(ChatColor.GOLD).append(check.getName()).append(": ").append(ChatColor.GREEN).append("true");
                message.append("\n");
                sortedChecks.remove(check);
            }

            for (GenericCheck check : sortedChecks.stream().filter(check -> !hackedPlayer.getGenericChecks().contains(check.getId())).toList()) {
                message.append(ChatColor.GOLD).append(check.getName()).append(": ").append(ChatColor.RED).append("false");
                message.append("\n");
            }
            // Send message
            event.getWhoClicked().sendMessage(message.toString());
        }

    }

}
