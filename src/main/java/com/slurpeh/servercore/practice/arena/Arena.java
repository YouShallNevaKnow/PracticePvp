package com.slurpeh.servercore.practice.arena;

import com.sun.org.apache.xml.internal.resolver.readers.OASISXMLCatalogReader;
import org.bukkit.Location;

/**
 * Created by Bradley on 4/29/16.
 */
public class Arena {
    private String name;
    private Location spawn1;
    private Location spawn2;

    public Arena(String name) {
        this.name = name;
    }

    public Arena(String name, Location spawn1, Location spawn2) {
        this.name = name;
        this.spawn1 = spawn1;
        this.spawn2 = spawn2;
    }

    public boolean isSetup() {
        return this.spawn1 != null && this.spawn2 != null;
    }

    public String getName() {
        return this.name;
    }

    public Location getSpawn1() {
        return this.spawn1;
    }

    public Location getSpawn2() {
        return this.spawn2;
    }

    public void setSpawn1(Location spawn1) {
        this.spawn1 = spawn1;
    }

    public void setSpawn2(Location spawn2) {
        this.spawn2 = spawn2;
    }
}
