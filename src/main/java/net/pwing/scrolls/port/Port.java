package net.pwing.scrolls.port;

import net.pwing.scrolls.PwingScrolls;
import net.pwing.scrolls.owner.Owner;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

/**
 * Created by Redned on 12/16/2018.
 */
public class Port {

    private String name;

    private Owner owner;
    private Location loc;

    private String password = "none";
    private String permission = "scrolls.player";

    public Port(String name, Owner owner, Location loc, String password) {
        this(name, owner, loc, password, "scrolls.player");
    }
    public Port(String name, Owner owner, Location loc, String password, String permission) {
        this.name = name;
        this.owner = owner;
        this.loc = loc;
        this.password = password;
        this.permission = permission;
    }

    public String getName() {
        return name;
    }

    public Owner getOwner() {
        return owner;
    }

    public Location getLocation() {
        return loc;
    }

    public String getPassword() {
        return password;
    }

    public boolean hasPassword() {
        return password == null || !password.equals("none");
    }

    public String getPermission() {
        return permission;
    }

    public boolean hasPermission() {
        return !permission.equals("scrolls.player");
    }

    public static String toConfigLocation(Location loc) {
        World world = loc.getWorld();
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        float pitch = loc.getPitch();
        float yaw = loc.getYaw();

        return x + " " + y + " " + z + " " + yaw + " " + pitch + " " + world.getName();
    }

    public static Location fromConfigLocation(String string) {
        String[] split = string.split(" ");

        World world = Bukkit.getWorld(split[5]);
        double x = Double.valueOf(split[0]);
        double y = Double.valueOf(split[1]);
        double z = Double.valueOf(split[2]);
        float yaw = Float.valueOf(split[3]);
        float pitch = Float.valueOf(split[4]);

        return new Location(world, x, y, z, yaw, pitch);
    }
}
