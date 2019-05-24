package net.pwing.scrolls;

import com.earth2me.essentials.Essentials;
import net.milkbowl.vault.economy.Economy;
import net.pwing.scrolls.commands.PwingScrollsCommand;
import net.pwing.scrolls.commands.PortCommand;
import net.pwing.scrolls.listener.PlayerListener;
import net.pwing.scrolls.owner.OwnerManager;
import net.pwing.scrolls.port.Port;
import net.pwing.scrolls.port.PortManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by Redned on 12/16/2018.
 */
public class PwingScrolls extends JavaPlugin {

    private static Economy economy = null;

    private File data;
    private File playerData;

    private FileConfiguration dataConfig;
    private FileConfiguration playerDataConfig;

    private PortManager portManager;
    private OwnerManager ownerManager;

    private Essentials ess;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (hasEssentials()) {
            ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
            getLogger().info("Found Essentials! Hooking in for /back support!");
        }

        saveDefaultConfig();
        data = new File(getDataFolder(), "data.yml");
        playerData = new File(getDataFolder(), "playerData.yml");
        setupData();

        portManager = new PortManager(this);
        ownerManager = new OwnerManager(this);

        setupPorts();

        getCommand("pwingscrolls").setExecutor(new PwingScrollsCommand(this));
        getCommand("port").setExecutor(new PortCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    public void setupPorts() {
        dataConfig.getConfigurationSection("ports").getKeys(false).forEach(key -> {
            Location loc = Port.fromConfigLocation(dataConfig.getString("ports." + key + ".location"));
            String password = dataConfig.getString("ports." + key + ".password");
            String owner = dataConfig.getString("ports." + key + ".owner");
            String permission = "scrolls.player";

            if (dataConfig.contains("ports." + key + ".permission"))
                permission = dataConfig.getString("ports." + key + ".permission");

            portManager.getPorts().add(new Port(key, ownerManager.getOwner(UUID.fromString(owner)), loc, password, permission));
        });
    }

    public void setupData() {
        if (!data.exists()) {
            try {
                data.createNewFile();
                saveResource("data.yml", true);
            } catch (IOException e) {
                getLogger().severe("Could not create data.yml!");
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(data);

        if (!playerData.exists()) {
            try {
                playerData.createNewFile();
                saveResource("playerData.yml", true);
            } catch (IOException e) {
                getLogger().severe("Could not create playerData.yml!");
            }
        }
        playerDataConfig = YamlConfiguration.loadConfiguration(playerData);
    }

    public FileConfiguration getDataConfig() {
        return dataConfig;
    }

    public FileConfiguration getPlayerDataConfig() {
        return playerDataConfig;
    }

    public PortManager getPortManager() {
        return portManager;
    }

    public OwnerManager getOwnerManager() {
        return ownerManager;
    }

    public void saveData() {
        try {
            dataConfig.save(data);
        } catch (IOException e) {
            getLogger().severe("Could not save dataConfig.yml !!");
        }
    }

    public void savePlayerData() {
        try {
            playerDataConfig.save(playerData);
        } catch (IOException e) {
            getLogger().severe("Could not save playerData.yml !!");
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public boolean hasEssentials() {
        return getServer().getPluginManager().getPlugin("Essentials") != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    public Essentials getEssentials() {
        return ess;
    }

}
