package com.slurpeh.servercore.practice.player;

import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.gametype.GameType;
import com.slurpeh.servercore.practice.util.MiscUtil;
import com.slurpeh.servercore.practice.util.PlyInv;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import sun.jvm.hotspot.ui.ObjectHistogramPanel;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Bradley on 5/4/16.
 */
public class PlayerDataManager implements Listener {
    private KohiPractice plugin;
    private HashMap<Player, PlayerData> playerData;
    public PlayerDataManager(KohiPractice plugin) {
        this.plugin = plugin;
        this.playerData = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
        for (Player ply : Bukkit.getOnlinePlayers()) {
            this.playerData.put(ply, new PlayerData(ply));
            this.loadPlayerInfo(ply);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        this.playerData.put(e.getPlayer(), new PlayerData(e.getPlayer()));
        this.loadPlayerInfo(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (this.playerData.containsKey(e.getPlayer())) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    PlayerDataManager.this.playerData.get(e.getPlayer()).save();
                    PlayerDataManager.this.playerData.remove(e.getPlayer());
                }
            }.runTaskLater(this.plugin, 5L);
        }
    }

    public void loadPlayerInfo(Player ply) {
        final PlayerData data = this.playerData.get(ply);
        final FileConfiguration config = data.getConfig();
        if (config.contains("ratings")) {
            for (final GameType gt : this.plugin.getGameTypeManager().getGameTypes()) {
                if (config.contains("ratings." + gt.getName())) {
                    data.setRating(gt, config.getInt("ratings." + gt.getName()), false);
                }
                else {
                    data.setRating(gt, 1000, false);
                }
            }
        }
        else {
            for (final GameType gt : this.plugin.getGameTypeManager().getGameTypes()) {
                config.set("ratings." + gt.getName(), (Object)1000);
                data.setRating(gt, 1000, false);
            }
        }
        if (config.contains("kits")) {
            for (final GameType gt : this.plugin.getGameTypeManager().getGameTypes()) {
                if (!config.contains("kits." + gt.getName())) {
                    for (int i = 1; i <= 5; ++i) {
                        config.set("kits." + gt.getName() + "." + i, (Object)"");
                    }
                }
                for (int i = 1; i <= 5; ++i) {
                    final String in = config.getString("kits." + gt.getName() + "." + i);
                    if (in == null) {
                        data.setKit(gt, null, i, false);
                        config.set("kits." + gt.getName() + "." + i, (Object)"");
                    }
                    else if (in.equals("")) {
                        data.setKit(gt, null, i, false);
                    }
                    else {
                        final String kitName = in.split("\\|")[0];
                        final int startIndex = in.indexOf("|");
                        final PlyInv inv = MiscUtil.playerInventoryFromString(in.substring(startIndex + 1, in.length() - 1));
                        data.setKit(gt, new Kit(kitName, inv), i, false);
                    }
                }
            }
        }
        else {
            for (final GameType gt : this.plugin.getGameTypeManager().getGameTypes()) {
                for (int i = 1; i <= 5; ++i) {
                    config.set("kits." + gt.getName() + "." + i, (Object)"");
                }
                data.setupKit(gt);
            }
        }
        if (config.contains("average")) {
            data.setAverage(config.getInt("average"), false);
        } else {
            int total = 0;
            for (GameType gt : data.getRatings().keySet()) {
                total += getRating(data.getPlayer(), gt);
            }
            data.setAverage(total / data.getRatings().keySet().size(), false);
            config.set("average", data.getAverage());
        }
        if (config.contains("unranked-games-played")) {
            data.setGamesPlayed(config.getInt("unranked-games-played"), false);
        } else {
            data.setGamesPlayed(0, false);
        }
        data.save();
    }

    public void setKit(final Player ply, final GameType gt, final int postion, final Kit kit) {
        this.playerData.get(ply).setKit(gt, kit, postion, true);
    }

    public int getRating(final Player ply, final GameType gt) {
        return this.playerData.get(ply).getRatings().get(gt);
    }

    public Kit[] getKits(final Player ply, final GameType gt) {
        return this.playerData.get(ply).getKits().get(gt);
    }

    public Kit getKit(final Player ply, final GameType gt, final int postion) {
        return this.getKits(ply, gt)[postion - 1];
    }

    public void removeKit(final Player ply, final GameType gt, final int postion) {
        this.playerData.get(ply).removeKit(gt, postion, true);
    }

    public void updateElo(final Player ply, final GameType gt, final int scoreChange, final boolean add) {
        final PlayerData data = this.playerData.get(ply);
        final int rating = data.getRating(gt);
        if (add) {
            this.playerData.get(ply).setRating(gt, rating + scoreChange, true);
        }
        else {
            this.playerData.get(ply).setRating(gt, rating - scoreChange, true);
        }
    }

    public void saveKits(final Player ply) {
        this.playerData.get(ply).save();
    }

    private PlayerData getPlayerData(final String player) {
        for (final Player ply : this.playerData.keySet()) {
            if (ply.getName().equals(player)) {
                return this.playerData.get(ply);
            }
        }
        return null;
    }

    public void setupNewGameType(final GameType gt) {
        for (final PlayerData data : this.playerData.values()) {
            data.setRating(gt, 1000, true);
        }
    }

    public PlayerData getPlayerData(Player ply) {
        return playerData.get(ply);
    }

    public FileConfiguration getConfigForOfflinePlayer(UUID id) {
        File f = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "playerdata" + File.separator + id.toString() + ".yml" );
        if (f.exists()) {
            return (FileConfiguration) YamlConfiguration.loadConfiguration(f);
        } else {
            return null;
        }
    }
}
