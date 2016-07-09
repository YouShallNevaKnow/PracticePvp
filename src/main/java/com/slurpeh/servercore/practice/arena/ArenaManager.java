package com.slurpeh.servercore.practice.arena;

import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.util.LocationUtil;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by Bradley on 4/29/16.
 */
public class ArenaManager {
    private List<Arena> arenas;
    private FileConfiguration config;
    private KohiPractice plugin;

    public ArenaManager(KohiPractice plugin) {
        this.plugin = plugin;
        this.config = this.plugin.getConfig();
        this.arenas = new ArrayList<>();
        for (String s : this.config.getConfigurationSection("arenas").getKeys(false)) {
            Arena a = new Arena(s);
            if (this.config.getString("arenas." + s + ".spawn1") != null) {
                a.setSpawn1(LocationUtil.getLocation(this.config.getString("arenas." + s + ".spawn1")));
            }
            if (this.config.getString("arenas." + s + ".spawn2") != null) {
                a.setSpawn2(LocationUtil.getLocation(this.config.getString("arenas." + s + ".spawn2")));
            }
            arenas.add(a);
        }
    }

    public List<Arena> getArenas() {
        return this.arenas;
    }

    public Arena getArena(Predicate<Arena> arenas) {
        for (Arena arena : this.arenas) {
            if (arenas.test(arena)) {
                return arena;
            }
        }
        return null;
    }

    public Arena getArena(String name) {
        return this.getArena(new Predicate<Arena>() {
            @Override
            public boolean test(Arena arena) {
                return name.equals(arena.getName());
            }
        });
    }

    public boolean doesArenaExist(String name) {
        for (Arena a : this.arenas) {
            if (name.equalsIgnoreCase(a.getName())) {
                return true;
            }
        }
        return false;
    }

    public void createArena(String name) {
        Arena a = new Arena(name);
        this.arenas.add(a);
        this.saveArenas();
    }

    public void removeArena(String name) {
        Arena a = this.getArena(name);
        this.arenas.remove(a);
        this.saveArenas();
    }

    public void saveArenas() {
        this.config.set("arenas", null);
        for (Arena a : this.arenas) {
            this.config.set("arenas." + a.getName() + ".spawn1", LocationUtil.getString(a.getSpawn1()));
            this.config.set("arenas." + a.getName() + ".spawn2", LocationUtil.getString(a.getSpawn2()));
        }
        this.plugin.saveConfig();
    }
}
