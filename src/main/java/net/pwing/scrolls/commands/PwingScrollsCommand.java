package net.pwing.scrolls.commands;

import net.pwing.scrolls.PwingScrolls;
import net.pwing.scrolls.owner.Owner;
import net.pwing.scrolls.owner.OwnerManager;
import net.pwing.scrolls.port.Port;
import net.pwing.scrolls.port.PortManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Redned on 12/16/2018.
 */
public class PwingScrollsCommand implements CommandExecutor {

    private String header = ChatColor.AQUA + "------------------" + ChatColor.AQUA + "[" + ChatColor.AQUA + ChatColor.BOLD + " Pwing Scrolls " + ChatColor.GRAY + "]" + ChatColor.DARK_AQUA + "------------------";

    private PwingScrolls plugin;

    public PwingScrollsCommand(PwingScrolls plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = plugin.getPortManager().getPrefix();
        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix + ChatColor.RED + "Console cannot run this command!");
            return true;
        }

        if (!command.getName().equals("pwingscrolls"))
            return false;

        Player player = (Player) sender;
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                List<Port> allowedPorts = new ArrayList<Port>();

                for (Port port : plugin.getPortManager().getPorts()) {
                    if (!port.hasPermission() || player.hasPermission(port.getPermission()))
                        allowedPorts.add(port);
                }

                StringBuilder builder = new StringBuilder();
                allowedPorts.forEach(port -> {
                    if (builder.length() > 0)
                        builder.append(", ");

                    builder.append(port.getName());
                });

                player.sendMessage(prefix + ChatColor.AQUA + "Ports: " + builder.toString());
            }

            if (args[0].equalsIgnoreCase("reload")) {
                if (!player.hasPermission("pwingscrolls.admin"))
                    return true;

                plugin.savePlayerData();
                plugin.saveData();

                plugin.getPortManager().getPorts().clear();

                plugin.setupData();
                plugin.setupPorts();

                player.sendMessage(prefix + ChatColor.AQUA + "Successfully reloaded the config file!");
            }
        } else if (args.length < 1) {
            sendHelpMessage(player);
        }

        if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("makescroll")) {
                OwnerManager ownerManager = plugin.getOwnerManager();
                PortManager portManager = plugin.getPortManager();

                Port port = portManager.getPortFromName(args[1]);
                if (port == null) {
                    player.sendMessage(prefix + ChatColor.RED + "Invalid Port! That port does not exist.");
                    return true;
                }

                Owner owner = ownerManager.getOwner(player.getUniqueId());

                if (!player.hasPermission("pwingscrolls.mod")) {
                    if (!port.getOwner().getOwnerUUID().equals(owner.getOwnerUUID())) {
                        player.sendMessage(prefix + ChatColor.RED + "You can only make scrolls for ports that you own!");
                        return true;
                    }
                }

                double cost = plugin.getPortManager().getScrollCost();
                plugin.getEconomy().withdrawPlayer(player, cost);

                StringBuilder sb = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    sb.append(ChatColor.translateAlternateColorCodes('&', args[i] + " "));
                }

                portManager.giveScrollItem(player, port, sb.toString());
                player.sendMessage(prefix + ChatColor.AQUA + "Created scroll for port " + ChatColor.DARK_AQUA + port.getName() + ChatColor.AQUA + ". " + cost + " rubles removed frmo balance.");
            }

            if (args[0].equalsIgnoreCase("setport")) {
                String password = "none";

                if (args.length > 2)
                    password = args[2];

                OwnerManager ownerManager = plugin.getOwnerManager();
                PortManager portManager = plugin.getPortManager();

                Owner owner = ownerManager.getOwner(player.getUniqueId());

                if (owner.getMaxPorts() < 1) {
                    player.sendMessage(prefix + ChatColor.RED + "You do not have an available port.");
                    return true;
                }

                if (portManager.doesPortExist(args[1])) {
                    player.sendMessage(prefix + ChatColor.RED + "A port by that name already exists. Please choose another name.");
                    return true;
                }

                Port port = new Port(args[1], owner, player.getLocation(), password);
                portManager.addPort(port);

                player.sendMessage(prefix + ChatColor.AQUA + "Successfully created a new port by the name of " + ChatColor.DARK_AQUA + args[1] + ChatColor.AQUA + "!");
                ownerManager.setMaxPorts(owner, owner.getMaxPorts() - 1);
            }

            if (args[0].equalsIgnoreCase("delport")) {
                OwnerManager ownerManager = plugin.getOwnerManager();
                PortManager portManager = plugin.getPortManager();

                Owner owner = ownerManager.getOwner(player.getUniqueId());

                Port port = portManager.getPortFromName(args[1]);

                if (port == null) {
                    player.sendMessage(prefix + ChatColor.RED + "Invalid Port! That port does not exist.");
                    return true;
                }

                if (!player.hasPermission("pwingscrolls.mod")) {
                    if (!port.getOwner().getOwnerUUID().equals(owner.getOwnerUUID())) {
                        player.sendMessage(prefix + ChatColor.RED + "You can only delete a port that you own!");
                        return true;
                    }
                }

                player.sendMessage(prefix + ChatColor.AQUA + "Deleted the port " + ChatColor.DARK_AQUA + args[1] + ChatColor.AQUA + ". One port refunded.");
                portManager.removePort(port);
                ownerManager.setMaxPorts(owner, owner.getMaxPorts() + 1);
            }

            if (player.hasPermission("pwingscrolls.mod")) {
                if (args[0].equalsIgnoreCase("add")) {
                    OwnerManager ownerManager = plugin.getOwnerManager();
                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

                    if (target == null) {
                        player.sendMessage(prefix + ChatColor.RED + "A player under that name has never joined the server!");
                        return true;
                    }

                    Owner owner = ownerManager.getOwner(target.getUniqueId());
                    ownerManager.setMaxPorts(owner, ownerManager.getMaxPorts(owner) + 1);

                    player.sendMessage(prefix + ChatColor.AQUA + "Added a port to " + target.getName() + "'s account!");
                }

                if (args[0].equalsIgnoreCase("remove")) {
                    OwnerManager ownerManager = plugin.getOwnerManager();
                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

                    if (target == null) {
                        player.sendMessage(prefix + ChatColor.RED + "A player under that name has never joined the server!");
                        return true;
                    }

                    Owner owner = ownerManager.getOwner(target.getUniqueId());
                    ownerManager.setMaxPorts(owner, ownerManager.getMaxPorts(owner) - 1);

                    player.sendMessage(prefix + ChatColor.AQUA + "Removed a port to " + target.getName() + "'s account!");
                }


                if (args[0].equalsIgnoreCase("info")) {
                    PortManager portManager = plugin.getPortManager();
                    Port port = portManager.getPortFromName(args[1]);

                    if (port == null) {
                        player.sendMessage(prefix + ChatColor.RED + "That port does not exist!");
                        return true;
                    }

                    String name = port.getOwner().getOwnerUUID().toString();
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(port.getOwner().getOwnerUUID());

                    if (owner != null)
                        name = owner.getName();

                    player.sendMessage(header);
                    player.sendMessage(ChatColor.DARK_AQUA + "Port Name: " + ChatColor.AQUA + port.getName());
                    player.sendMessage(ChatColor.DARK_AQUA + "Owner: " + ChatColor.AQUA + name);
                    player.sendMessage(ChatColor.DARK_AQUA + "Location: " + ChatColor.AQUA + Port.toConfigLocation(port.getLocation()));
                    player.sendMessage(ChatColor.DARK_AQUA + "Password: " + ChatColor.AQUA + port.getPassword());

                    if (port.hasPermission() && player.hasPermission("pwingscrolls.admin"))
                        player.sendMessage(ChatColor.DARK_RED + "Permission Node: " + ChatColor.RED + port.getPermission());
                }
            }
        }

        return true;
    }

    public void sendHelpMessage(Player player) {
        player.sendMessage(header);
        player.sendMessage(ChatColor.AQUA + "/ps list " + ChatColor.DARK_AQUA + "Lists all the ports.");
        player.sendMessage(ChatColor.AQUA + "/ps makescroll <name> <lore> " + ChatColor.DARK_AQUA + "Makes a scroll for a port you own. Costs " + plugin.getPortManager().getScrollCost() + " rubles.");
        player.sendMessage(ChatColor.AQUA + "/ps setport <name> " + ChatColor.DARK_AQUA + "Creates a new port.");
        player.sendMessage(ChatColor.AQUA + "/ps setport <name> <password> " + ChatColor.DARK_AQUA + "Creates a new port with a specified password.");
        player.sendMessage(ChatColor.AQUA + "/ps delport <name> " + ChatColor.DARK_AQUA + "Deletes one of your ports. You must be the owner to delete the port.");

        if (player.hasPermission("pwingscrolls.mod")) {
            player.sendMessage(ChatColor.RED + "/ps add <name> " + ChatColor.DARK_RED + "Adds a port to a player.");
            player.sendMessage(ChatColor.RED + "/ps remove <name> " + ChatColor.DARK_RED + "Removes a port to a player.");
            player.sendMessage(ChatColor.RED + "/ps info <port> " + ChatColor.DARK_RED + "Get info about a port.");
        }

        if (player.hasPermission("pwingscrolls.admin"))
            player.sendMessage(ChatColor.RED + "/ps reload " + ChatColor.DARK_RED + "Reload the scrolls config.");
        player.sendMessage(ChatColor.AQUA + "");
    }
}
