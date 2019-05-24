package net.pwing.scrolls.owner;

import net.pwing.scrolls.PwingScrolls;
import net.pwing.scrolls.port.Port;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Redned on 12/16/2018.
 */
public class OwnerManager {

    private PwingScrolls plugin;

    public OwnerManager(PwingScrolls plugin) {
        this.plugin = plugin;
    }

    public Owner getOwner(UUID uuid) {
        return new Owner(uuid, plugin.getPlayerDataConfig().getInt("data." + uuid.toString() + ".maxPorts"));
    }

    public List<Port> getOwnedPorts(Owner owner) {
        List<Port> ownedPorts = new ArrayList<Port>();
        for (Port port : plugin.getPortManager().getPorts())
            if (port.getOwner().getOwnerUUID().equals(owner.getOwnerUUID()))
                ownedPorts.add(port);

        return ownedPorts;
    }

    public int getMaxPorts(Owner owner) {
        FileConfiguration playerData = plugin.getPlayerDataConfig();
        return playerData.getInt("data." + owner.getOwnerUUID().toString() + ".maxPorts");
    }

    public void setMaxPorts(Owner owner, int amount) {
        FileConfiguration playerData = plugin.getPlayerDataConfig();
        playerData.set("data." + owner.getOwnerUUID().toString() + ".maxPorts", + amount);
        plugin.savePlayerData();
    }
}
