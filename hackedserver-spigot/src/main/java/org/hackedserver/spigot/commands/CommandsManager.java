package org.hackedserver.spigot.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.hackedserver.core.HackedPlayer;
import org.hackedserver.core.HackedServer;
import org.hackedserver.core.config.ConfigsManager;
import org.hackedserver.core.config.GenericCheck;
import org.hackedserver.core.config.Message;
import org.hackedserver.spigot.HackedHolder;
import org.hackedserver.spigot.utils.logs.Logs;

import java.util.*;

public class CommandsManager {

    private final JavaPlugin plugin;
    private final BukkitAudiences audiences;

    public CommandsManager(JavaPlugin plugin, BukkitAudiences audiences) {
        this.plugin = plugin;
        this.audiences = audiences;
    }

    public void loadCommands() {
        new CommandAPICommand("hackedserver")
                .withAliases("hs")
                .withPermission("hackedserver.command")
                .withSubcommand(getReloadCommand())
                .withSubcommand(getCheckCommand())
                .withSubcommand(getListCommand())
                .withSubcommand(getInvCommand())
                .executes((sender, args) -> {
                    Message.COMMANDS_HELP.send(audiences.sender(sender));
                })
                .register();
    }

    private CommandAPICommand getReloadCommand() {
        return new CommandAPICommand("reload")
                .withPermission("hackedserver.command.reload")
                .executes((sender, args) -> {
                    ConfigsManager.reload(Logs.getLogger(), plugin.getDataFolder());
                    Message.COMMANDS_RELOAD_SUCCESS.send(audiences.sender(sender));
                });
    }

    private CommandAPICommand getCheckCommand() {
        return new CommandAPICommand("check")
                .withPermission("hackedserver.command.check")
                .withArguments(new PlayerArgument("player"))
                .executes((sender, args) -> {
                    Player player = (Player) args.get("player");
                    if (player == null) {
                        Message.PLAYER_NOT_FOUND.send(audiences.sender(sender));
                        return;
                    }
                    HackedPlayer hackedPlayer = HackedServer.getPlayer(player.getUniqueId());
                    if (hackedPlayer == null) {
                        Message.PLAYER_NOT_FOUND.send(audiences.sender(sender));
                        return;
                    }
                    if (hackedPlayer.getGenericChecks().isEmpty())
                        Message.CHECK_NO_MODS.send(audiences.sender(sender));
                    else {
                        Message.CHECK_MODS.send(audiences.sender(sender));
                        hackedPlayer.getGenericChecks().forEach(checkId ->
                                Message.MOD_LIST_FORMAT.send(audiences.sender(sender),
                                        Placeholder.parsed("mod", HackedServer.getCheck(checkId).getName())));
                    }
                });
    }

    private CommandAPICommand getListCommand() {
        return new CommandAPICommand("list")
                .withPermission("hackedserver.command.list")
                .executes((sender, args) -> {
                    Message.CHECK_PLAYERS.send(audiences.sender(sender));
                    HackedServer.getPlayers().forEach(hackedPlayer ->
                            Message.PLAYER_LIST_FORMAT.send(audiences.sender(sender),
                                    Placeholder.parsed("player",
                                            Objects.requireNonNull(
                                                    Bukkit.getOfflinePlayer(hackedPlayer.getUuid()).getName()))));
                });
    }

    private CommandAPICommand getInvCommand() {
        return new CommandAPICommand("inv")
                .withPermission("hackedserver.command.inv")
                .executesPlayer((player, args) -> {
                    Inventory inv = Bukkit.createInventory(new HackedHolder(player), 9, "HackedServer");
                    HackedServer.getPlayers().forEach(hackedPlayer -> {
                        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                        SkullMeta meta = (SkullMeta) head.getItemMeta();
                        assert meta != null;
                        meta.setOwningPlayer(Bukkit.getOfflinePlayer(hackedPlayer.getUuid()));
                        meta.setDisplayName(Bukkit.getOfflinePlayer(hackedPlayer.getUuid()).getName());

                        List<String> lore = new ArrayList<>();
                        List<GenericCheck> sortedChecks = new ArrayList<>(HackedServer.getChecks().stream().sorted(Comparator.comparing(GenericCheck::getName)).toList());
                        sortedChecks.remove(HackedServer.getCheck("fabric"));
                        sortedChecks.remove(HackedServer.getCheck("forge"));

                        lore.add(ChatColor.GOLD + "Fabric: " + (hackedPlayer.getGenericChecks().contains("fabric") ? ChatColor.GREEN + "true" : ChatColor.RED + "false"));
                        lore.add(ChatColor.GOLD + "Forge: " + (hackedPlayer.getGenericChecks().contains("forge") ? ChatColor.GREEN + "true" : ChatColor.RED + "false"));
                        lore.add(ChatColor.BLUE + "--------------------");

                        for (GenericCheck check : sortedChecks.stream().filter(check -> hackedPlayer.getGenericChecks().contains(check.getId())).toList()) {
                            lore.add(ChatColor.GOLD + check.getName() + ": " + ChatColor.GREEN + "true");
                            sortedChecks.remove(check);
                        }

                        for (GenericCheck check : sortedChecks.stream().filter(check -> !hackedPlayer.getGenericChecks().contains(check.getId())).toList()) {
                            lore.add(ChatColor.GOLD + check.getName() + ": " + ChatColor.RED + "false");
                        }
                        meta.setLore(lore);
                        head.setItemMeta(meta);
                        inv.addItem(head);
                    });
                    player.openInventory(inv);
                });
    }

}

