package net.pwing.scrolls.commands;

import net.pwing.scrolls.PwingScrolls;
import net.pwing.scrolls.port.Port;
import net.pwing.scrolls.port.PortManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Redned on 12/16/2018.
 */
public class PortCommand implements TabExecutor {

    private String header = ChatColor.AQUA + "------------------" + ChatColor.AQUA + "[" + ChatColor.AQUA + ChatColor.BOLD + " Pwing Scrolls " + ChatColor.GRAY + "]" + ChatColor.DARK_AQUA + "------------------";

    private PwingScrolls plugin;

    public PortCommand(PwingScrolls plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getPortManager().getPrefix() + ChatColor.RED + "Console cannot run this command!");
            return true;
        }

        if (!command.getName().equals("port"))
            return false;

        Player player = (Player) sender;
        if (args.length >= 1) {
            String password = "none";
            boolean enteredPassword = false;

            if (args.length == 2) {
                password = args[1];
                enteredPassword = true;
            }

            PortManager manager = plugin.getPortManager();
            Port port = manager.getPortFromName(args[0]);
            if (port == null) {
                player.sendMessage(plugin.getPortManager().getPrefix() + ChatColor.RED + "Invalid Port! That port does not exist.");
                return true;
            }

            if (player.hasPermission("pwingscrolls.mod")) {
                password = port.getPassword();
            }

            manager.portPlayer(port, player, password, enteredPassword);
        } else {
            sendHelpMessage(player);
        }

        return true;
    }

    public void sendHelpMessage(Player player) {
        player.sendMessage(header);
        player.sendMessage(ChatColor.AQUA + "/port <name> " + ChatColor.DARK_AQUA + "Teleport to the specified port.");
        player.sendMessage(ChatColor.AQUA + "/port <name> <password> " + ChatColor.DARK_AQUA + "Teleport to the specified port with a password.");
        player.sendMessage(ChatColor.AQUA + "");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> allowedPorts = new ArrayList<String>();

            for (Port port : plugin.getPortManager().getPorts()) {
                if (!port.hasPermission() || sender.hasPermission(port.getPermission()))
                    allowedPorts.add(port.getName());
            }

            for (int i = 0; i < allowedPorts.size(); i++) {
                String str = allowedPorts.get(i);

                if (!str.toLowerCase().startsWith(args[0].toLowerCase())) {
                    allowedPorts.remove(str);
                    i--;
                }
            }

            return allowedPorts;
        }

        return null;
    }
}
