package com.slurpeh.servercore.practice.player;

import java.util.*;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.gametype.GameType;
import com.slurpeh.servercore.practice.util.MiscUtil;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.IOException;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Created by Bradley on 5/4/16.
 */
public class PlayerData {
    private HashMap<GameType, Integer> ratings;
    private HashMap<GameType, Kit[]> kits;
    private FileConfiguration config;
    private Player player;
    private File file;
    private int average;
    private int gamesPlayed;

    public PlayerData(final Player player) {
        this.player = player;
        this.average = 0;
        this.ratings = new HashMap<GameType, Integer>();
        this.kits = new HashMap<GameType, Kit[]>();
        this.file = new File(((KohiPractice)JavaPlugin.getPlugin((Class)KohiPractice.class)).getDataFolder().getAbsolutePath() + File.separator + "playerdata");
        if (!this.file.exists()) {
            this.file.mkdir();
        }
        this.file = new File(((KohiPractice)JavaPlugin.getPlugin((Class)KohiPractice.class)).getDataFolder().getAbsolutePath() + File.separator + "playerdata" + File.separator + player.getUniqueId().toString() + ".yml");
        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = (FileConfiguration)new YamlConfiguration();
        try {
            this.config.load(this.file);
        }
        catch (IOException | InvalidConfigurationException e2) {
            e2.printStackTrace();
        }
    }

    public void setRating(final GameType gt, final int rating, final boolean save) {
        this.ratings.put(gt, rating);
        if (save) {
            this.save();
        }
    }

    public void setAverage(int average, boolean save) {
        config.set("average", average);
        this.average = average;
        if (save) {
            this.save();
        }
    }

    public int getAverage() {
        return config.getInt("average");
    }

    public void removeKit(final GameType gt, final int postion, final boolean save) {
        final Kit[] kits = this.kits.get(gt);
        kits[postion - 1] = null;
        this.kits.put(gt, kits);
        if (save) {
            this.save();
        }
    }

    public void removeKit(final GameType gt, final Kit kit, final boolean save) {
        final Kit[] kits = this.kits.get(gt);
        for (int i = 0; i < 5; ++i) {
            if (kits[i] == kit) {
                kits[i] = null;
            }
        }
        this.kits.put(gt, kits);
        if (save) {
            this.save();
        }
    }

    public void setKit(final GameType gt, final Kit kit, final int postion, final boolean save) {
        if (!this.kits.containsKey(gt)) {
            this.kits.put(gt, new Kit[5]);
        }
        final Kit[] kits = this.kits.get(gt);
        kits[postion - 1] = kit;
        this.kits.put(gt, kits);
        if (save) {
            this.save();
        }
    }

    public void save() {
        for (final GameType gt : this.ratings.keySet()) {
            if (gt != null) {
                this.config.set("ratings." + gt.getName(), (Object)this.ratings.get(gt));
            } else {
                this.config.set("ratings." + gt.getName(), null);
                this.config.set("kits." + gt.getName(), null);
            }
        }
        for (final GameType gt : this.kits.keySet()) {
            this.config.set("kits." + gt.getName(), (Object)new ArrayList());
            for (int i = 1; i < 5; ++i) {
                final Kit kit = this.kits.get(gt)[i - 1];
                if (kit == null) {
                    this.config.set("kits." + gt.getName() + "." + i, (Object)null);
                }
                else {
                    this.config.set("kits." + gt.getName() + "." + i, (Object)(kit.getName() + "|" + MiscUtil.playerInventoryToString(kit.getInv())));
                }
            }
        }
        int total = 0;
        for (GameType gt : ratings.keySet()) {
            total += getRating(gt);
        }
        this.config.set("average", total / ratings.keySet().size());
        this.config.set("unranked-games-played", gamesPlayed);
        try {
            this.config.save(this.file);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getRating(final GameType gt) {
        return this.ratings.get(gt);
    }

    public void setupKit(final GameType gt) {
        this.kits.put(gt, new Kit[5]);
    }

    public HashMap<GameType, Integer> getRatings() {
        return this.ratings;
    }

    public HashMap<GameType, Kit[]> getKits() {
        return this.kits;
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public Player getPlayer() {
        return this.player;
    }

    public int getGamesPlayed() {
        return this.gamesPlayed;
    }

    public void setGamesPlayed(int i, boolean save) {
        this.gamesPlayed = i;
        if (save) {
            save();
        }
    }
}
