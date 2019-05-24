package net.pwing.scrolls.listener;

import net.pwing.scrolls.PwingScrolls;
import net.pwing.scrolls.port.Port;
import net.pwing.scrolls.port.PortManager;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by Redned on 12/16/2018.
 */
public class PlayerListener implements Listener {

    private PwingScrolls plugin;

    public PlayerListener(PwingScrolls plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        FileConfiguration playerData = plugin.getPlayerDataConfig();
        if (playerData.contains("data." + event.getPlayer().getUniqueId()))
            return;

        playerData.set("data." + event.getPlayer().getUniqueId().toString() + ".maxPorts", 0);
        plugin.savePlayerData();
    }

    @EventHandler // Avoid conflict with ProtocolSupport
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!plugin.getConfig().getBoolean("override-protocolsupport-ps", true))
            return;

        if (event.getMessage().startsWith("/ps")) {
            event.setCancelled(true);

            String cmd = event.getMessage().substring(1);
            String[] args = new String[0];
            if (cmd.contains(" ")) {
                args = event.getMessage().substring(event.getMessage().indexOf(' ') + 1).split(" ");
            }

            plugin.getCommand("pwingscrolls").execute(event.getPlayer(), "pwingscrolls", args);
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        if (!ChatColor.stripColor(event.getLine(0)).equalsIgnoreCase("[Port]"))
            return;

        if (!player.hasPermission("pwingscrolls.mod")) {
            event.setLine(0, ChatColor.DARK_RED + "[Port]");
            event.setLine(1, ChatColor.RED + "You do not have");
            event.setLine(2, ChatColor.RED + "permission to");
            event.setLine(3, ChatColor.RED + "make port signs!");
            return;
        }

        String portName = ChatColor.stripColor(event.getLine(1));
        Port port = plugin.getPortManager().getPortFromName(portName);
        if (port == null) {
            event.setLine(0, ChatColor.DARK_RED + "[Port]");
            event.setLine(1, ChatColor.RED + "That port does");
            event.setLine(2, ChatColor.RED + "not exist!");
            return;
        }

        event.setLine(0, ChatColor.BLUE + "[Port]");
        event.setLine(1, port.getName());
    }

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (!(event.getClickedBlock().getState() instanceof Sign))
            return;

        Sign sign = (Sign) event.getClickedBlock().getState();
        if (!ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("[Port]"))
            return;

        Port port = plugin.getPortManager().getPortFromName(ChatColor.stripColor(sign.getLine(1)));
        if (port == null)
            return;

        // No password for ports with signs, however if there is
        // a permission (i.e. dun1), it'll still block them from accessing it.
        plugin.getPortManager().portPlayer(port, event.getPlayer(), port.getPassword(), false);
    }

    @EventHandler
    public void onScrollUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_AIR)
            return;

        if (event.getPlayer().getInventory().getItemInMainHand() == null)
            return;

        PortManager manager = plugin.getPortManager();
        if (!manager.isScroll(event.getPlayer().getInventory().getItemInMainHand()))
            return;

        String name = ChatColor.stripColor(event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().replace("[Portal: ", "").replace("]", ""));
        Port port = manager.getPortFromName(name);
        if (port == null)
            return;

        if (!manager.hasCooldown(event.getPlayer().getUniqueId()))
            event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);

        manager.portPlayer(port, event.getPlayer(), port.getPassword(), false);
    }
}
