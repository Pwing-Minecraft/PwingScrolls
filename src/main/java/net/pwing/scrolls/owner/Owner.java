package net.pwing.scrolls.owner;

import java.util.UUID;

/**
 * Created by Redned on 12/16/2018.
 */
public class Owner {

    private UUID uuid;
    private int maxPorts;

    public Owner(UUID uuid, int maxPorts) {
        this.uuid = uuid;
        this.maxPorts = maxPorts;
    }

    public UUID getOwnerUUID() {
        return uuid;
    }

    public int getMaxPorts() {
        return maxPorts;
    }
}
