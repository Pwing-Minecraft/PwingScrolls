package net.pwing.scrolls.port;

import com.earth2me.essentials.User;
import io.papermc.lib.PaperLib;
import net.pwing.scrolls.PwingScrolls;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Redned on 12/16/2018.
 */
public class PortManager {

    private String prefix;
    private double scrollCost = 20;
    private List<World> blacklistedWorlds = new ArrayList<World>();

    private List<Port> ports = new ArrayList<Port>();
    private Map<UUID, Long> cooldown = new HashMap<UUID, Long>();

    private PwingScrolls plugin;

    public PortManager(PwingScrolls plugin) {
        this.plugin = plugin;

        initConfig();
    }

    public void initConfig() {
        FileConfiguration config = plugin.getConfig();
        prefix = ChatColor.translateAlternateColorCodes('&', config.getString("prefix", "&7[Pwing Scrolls] "));
        scrollCost = config.getDouble("create-scroll-cost", 20);
        config.getStringList("blacklisted-worlds").forEach(world -> blacklistedWorlds.add(Bukkit.getWorld(world)));
    }

    public void addPort(Port port) {
        ports.add(port);
        addPortToConfig(port);
    }

    public void removePort(Port port) {
        ports.remove(port);
        removePortFromConfig(port);
    }

    public Port getPortFromName(String name) {
        for (Port port : ports)
            if (port.getName().equalsIgnoreCase(name))
                return port;

        return null;
    }

    // This should always return true
    public boolean doesPortExist(Port port) {
        return ports.contains(port);
    }

    public boolean doesPortExist(String name) {
        if (getPortFromName(name) != null)
            return true;

        return false;
    }

    private void addPortToConfig(Port port) {
        FileConfiguration config = plugin.getDataConfig();
        config.set("ports." + port.getName() + ".location", Port.toConfigLocation(port.getLocation()));
        config.set("ports." + port.getName() + ".owner", port.getOwner().getOwnerUUID().toString());
        config.set("ports." + port.getName() + ".password", port.getPassword());

        plugin.saveData();
    }

    private void removePortFromConfig(Port port) {
        FileConfiguration config = plugin.getDataConfig();
        config.set("ports." + port.getName(), null);

        plugin.saveData();
    }

    public Map<UUID, Long> getCooldownMap() {
        return cooldown;
    }

    public void setCooldown(UUID uuid, int time) {
        cooldown.put(uuid, (time * 1000) + System.currentTimeMillis());
    }

    public boolean hasCooldown(UUID uuid) {
        if (!cooldown.containsKey(uuid))
            return false;

        return (cooldown.get(uuid) > System.currentTimeMillis());
    }

    public void giveScrollItem(Player player, Port port, String desc) {
        ItemStack stack = new ItemStack(Material.PAPER);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "[Portal: " + port.getName() + "]");
        meta.setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&', desc), ChatColor.GOLD + "Click to use this scroll!"));
        stack.setItemMeta(meta);

        player.getInventory().addItem(stack);
    }

    public boolean isScroll(ItemStack stack) {
        if (stack.getType() != Material.PAPER)
            return false;

        if (!stack.hasItemMeta())
            return false;

        if (!stack.getItemMeta().hasDisplayName())
            return false;

        if (!stack.getItemMeta().hasLore())
            return false;

        if (!ChatColor.stripColor(stack.getItemMeta().getDisplayName()).startsWith("[Portal:") || !ChatColor.stripColor(stack.getItemMeta().getDisplayName()).endsWith("]"))
            return false;

        boolean contains = false;

        for (String str : stack.getItemMeta().getLore())
            if (ChatColor.stripColor(str).startsWith("Left click to use scroll") || ChatColor.stripColor(str).startsWith("Click to use this scroll!"))
                contains = true;

        return contains;
    }

    public void portPlayer(Port port, Player player, String password, boolean enteredPassword) {
        PortManager manager = plugin.getPortManager();

        if (port.hasPermission() && !player.hasPermission(port.getPermission())) {
            player.sendMessage(prefix + ChatColor.RED + "You do not have permission to go to this port.");
            return;
        }

        if (!port.getOwner().getOwnerUUID().equals(player.getUniqueId())) {
            if (port.hasPassword() && !port.getPassword().equals(password)) {
                if (enteredPassword)
                    player.sendMessage(prefix + ChatColor.RED + "Invalid Password!");
                else
                    player.sendMessage(prefix + ChatColor.RED + "This port has a password. To enter, use /port " + port.getName() + " <password>.");

                return;
            }
        }

        if (manager.hasCooldown(player.getUniqueId()))
            return;

        // Set /back location for Essentials
        if (plugin.hasEssentials()) {
            User user = plugin.getEssentials().getUser(player.getUniqueId());
            user.setLastLocation(player.getLocation());
        }

        player.sendMessage(getPrefix() + ChatColor.AQUA + "Porting you to " + ChatColor.DARK_AQUA + port.getName() + ChatColor.AQUA + ".");
        PaperLib.teleportAsync(player, port.getLocation());

        int[] volume = {10, 100, 200, 300, 15, 105, 205, 305};
        for (int i : volume) {
            player.getWorld().playEffect(player.getLocation(), Effect.ENDER_SIGNAL, i);
        }

        setCooldown(player.getUniqueId(), 5);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.50f, 0.8f);
    }

    public double getScrollCost() {
        return scrollCost;
    }

    public List<World>  getBlacklistedWorlds() {
        return blacklistedWorlds;
    }

    public String getPrefix() {
        return prefix;
    }

    public List<Port> getPorts() {
        return ports;
    }

}
