package com.slurpeh.servercore.practice.player;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.gametype.GameType;
import com.sun.istack.internal.Nullable;
import com.sun.org.apache.xerces.internal.dom.RangeExceptionImpl;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

/**
 * Created by Bradley on 5/8/16.
 */
public class RankingManager {
    Table<UUID, GameType, Integer> rankings;
    Table<UUID, GameType, Integer> topGametypeRankings;
    HashMap<UUID, Integer> topAverageRankings;
    HashMap<UUID, Integer> averageRankings;
    KohiPractice plugin;
    File file;
    FileConfiguration config;
    public RankingManager(KohiPractice plugin) {
        this.plugin = plugin;
        this.averageRankings = new HashMap<>();
        this.topAverageRankings = new HashMap<>();
        this.topGametypeRankings = HashBasedTable.create();
        this.rankings = HashBasedTable.create();
        this.file = new File(plugin.getDataFolder().getAbsolutePath(), "rankings.yml");
        this.config = (FileConfiguration) YamlConfiguration.loadConfiguration(file);
        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        /*
        rankings.yml -
        rankings:
          *gametype*:
            1:
              player: *player*
              elo: *elo*
        averagetoprankings:
          1:
            player: *player*
            elo: *elo*
        */
        if (config.getConfigurationSection("toprankings") != null) {
            config.getConfigurationSection("toprankings").getKeys(false).forEach(gametype -> {
                GameType gt = plugin.getGameTypeManager().getGameType(gametype);
                if (config.getConfigurationSection("toprankings." + gametype) != null) {
                    config.getConfigurationSection("toprankings." + gametype).getKeys(false).forEach(ranking -> {
                        if (Bukkit.getPlayer(UUID.fromString(config.getString("toprankings." + gametype + "." + ranking + ".player"))) != null) {
                            Player ply = Bukkit.getPlayer(UUID.fromString(config.getString("toprankings." + gametype + "." + ranking + ".player")));
                            Integer elo = config.getInt("toprankings." + gametype + "." + ranking + ".elo");
                            topGametypeRankings.put(ply.getUniqueId(), gt, elo);
                        }
                        else {
                            if (Bukkit.getOfflinePlayer(UUID.fromString(config.getString("toprankings." + gametype + "." + ranking + ".player"))) != null) {
                                OfflinePlayer pl = (OfflinePlayer)Bukkit.getOfflinePlayer(UUID.fromString(config.getString("toprankings." + gametype + "." + ranking + ".player")));
                                Integer elo2 = config.getInt("toprankings." + gametype + "." + ranking + ".elo");
                                topGametypeRankings.put(pl.getUniqueId(), gt, elo2);
                            } else {
                                return;
                            }
                        }
                    });
                }
            });
        }
        if (config.getConfigurationSection("averagetoprankings") != null) {
            config.getConfigurationSection("averagetoprankings").getKeys(false).forEach(ranking -> {
                if (Bukkit.getPlayer(UUID.fromString(config.getString("averagetoprankings." + ranking + ".player"))) != null) {
                    Player ply = Bukkit.getPlayer(UUID.fromString(config.getString("averagetoprankings." + ranking + ".player")));
                    Integer elo = config.getInt("averagetoprankings." + ranking + ".elo");
                    topAverageRankings.put(ply.getUniqueId(), elo);
                } else {
                    if (Bukkit.getOfflinePlayer(UUID.fromString(config.getString("averagetoprankings." + ranking + ".player"))) != null) {
                        OfflinePlayer ply = Bukkit.getOfflinePlayer(UUID.fromString(config.getString("averagetoprankings." + ranking + ".player")));
                        Integer elo = config.getInt("averagetoprankings." + ranking + ".elo");
                        topAverageRankings.put(ply.getUniqueId(), elo);
                    } else {
                        return;
                    }
                }
            });
        }
        File[] eloFiles = new File(plugin.getDataFolder() + File.separator + "playerdata").listFiles();
        if (eloFiles != null) {
            for (File f : eloFiles) {
                if (f.getName().endsWith(".yml")) {
                    UUID id = UUID.fromString(f.getName().split("\\.")[0]);
                    Player ply = Bukkit.getPlayer(id);
                    if (ply != null) {
                        FileConfiguration conf = (FileConfiguration)YamlConfiguration.loadConfiguration(f);
                        if (conf.getConfigurationSection("ratings") != null) {
                            conf.getConfigurationSection("ratings").getKeys(false).forEach(gametype -> {
                                GameType gt = plugin.getGameTypeManager().getGameType(gametype);
                                int elo = conf.getInt("ratings." + gametype);
                                rankings.put(ply.getUniqueId(), gt, elo);
                            });
                        }
                        if (conf.get("average") != null) {
                            int total = 0;
                            Map<GameType, Integer> playerRankings = rankings.row(id);
                            for (GameType gt : playerRankings.keySet()) {
                                total += playerRankings.get(gt);
                            }
                            int average = total / playerRankings.keySet().size();
                            conf.set("average", average);
                            averageRankings.put(id, average);
                        } else {
                            int total = 0;
                            Map<GameType, Integer> playerRankings = rankings.row(id);
                            for (GameType gt : playerRankings.keySet()) {
                                total += playerRankings.get(gt);
                            }
                            int average = total / playerRankings.keySet().size();
                            conf.set("average", average);
                            averageRankings.put(id, average);
                        }
                    }
                }
            }
        } else {
            return;
        }
    }

    public void sendElos(Player ply, String toRetrieveName) {
        try {
            ply.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "------[" + ChatColor.RESET + ChatColor.GOLD + " Elo Stats" + ChatColor.GRAY + ": " + ChatColor.DARK_GREEN + toRetrieveName + " " + ChatColor.YELLOW + ChatColor.STRIKETHROUGH + ChatColor.BOLD + "]------");
            Player tor = Bukkit.getPlayerExact(toRetrieveName);
            if (tor.isOnline() && tor != null) {
                PlayerData plyData = plugin.getPlayerDataManager().getPlayerData(tor);
                for (GameType gt : plyData.getRatings().keySet()) {
                        ply.sendMessage("               " + ChatColor.YELLOW + gt.getName() + ": " + ChatColor.GREEN + plyData.getRating(gt));
                }
            } else {
                FileConfiguration config = plugin.getPlayerDataManager().getConfigForOfflinePlayer(Bukkit.getOfflinePlayer(toRetrieveName).getUniqueId());
                if (config != null) {
                    if (config.getConfigurationSection("ratings") != null) {
                        config.getConfigurationSection("ratings").getKeys(false).forEach(gametype -> {
                            int elo = config.getInt("ratings." + gametype);
                            ply.sendMessage("               " + ChatColor.YELLOW + gametype + ": " + ChatColor.GREEN + elo);
                        });
                    }
                } else {
                    ply.sendMessage(ChatColor.RED + "This user has never played before!");
                }
            }
        } catch (NullPointerException ex) {
            ply.sendMessage(ChatColor.RED + "ERROR: This user has never played before!");
        }
    }

    public void sendAverageElo(Player ply, String toRetrieveName) {
        try {
            ply.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "------[" + ChatColor.RESET + ChatColor.GOLD + " Elo Stats (Average)" + ChatColor.GRAY + ": " + ChatColor.DARK_GREEN + toRetrieveName + " " + ChatColor.YELLOW + ChatColor.STRIKETHROUGH + ChatColor.BOLD + "]------");
            Player tor = Bukkit.getPlayerExact(toRetrieveName);
            if (tor.isOnline() && tor != null) {
                PlayerData plyData = plugin.getPlayerDataManager().getPlayerData(tor);
                ply.sendMessage("               " + ChatColor.YELLOW + "Average: " + ChatColor.GREEN + plyData.getAverage());
            } else {
                FileConfiguration config = plugin.getPlayerDataManager().getConfigForOfflinePlayer(Bukkit.getOfflinePlayer(toRetrieveName).getUniqueId());
                if (config != null) {
                    if (config.get("average") != null) {
                        ply.sendMessage("               " + ChatColor.YELLOW + "Average: " + ChatColor.GREEN + averageRankings.get(tor.getUniqueId()));
                    }
                } else {
                    ply.sendMessage(ChatColor.RED + "This user has never played before!");
                }
            }
        } catch (NullPointerException ex) {
            ply.sendMessage(ChatColor.RED + "ERROR: This user has never played before!");
        }
    }

    public void sendEloTop(GameType type, Player ply) {
        ply.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "------[" + ChatColor.RESET + ChatColor.GOLD + " Top Elo Stats" + ChatColor.GRAY + ": " + ChatColor.DARK_GREEN + type.getName() + " " + ChatColor.YELLOW + ChatColor.STRIKETHROUGH + ChatColor.BOLD + "]------");
        Map<UUID, Integer> rankingsForGametype = rankings.column(type);
        List<Integer> ints = new ArrayList<>();
        rankingsForGametype.values().forEach(elo -> {
            ints.add(elo);
        });
        Collections.sort(ints);
        Collections.reverse(ints);
        if (ints.size() > 10) {
            List<Integer> topRankings = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                topRankings.add(ints.get(i));
            }
            Collections.sort(ints);
            for (Integer i : topRankings) {
                int placeInArray = 1;
                for (int i2 = 0; i2 < topRankings.size(); i2++) {
                    if (topRankings.get(i2) == i) {
                        placeInArray += i2;
                    }
                }
                while (placeInArray < 11) {
                    if (getPlayer(type, i) instanceof Player) {
                        ply.sendMessage(ChatColor.GREEN + "" + (placeInArray) + ".)" + ChatColor.RESET + "               " + ChatColor.YELLOW + ((Player) getPlayer(type, i)).getName() + ": " + ChatColor.GREEN + i);
                        topGametypeRankings.put(((Player) getPlayer(type, i)).getUniqueId(), type, i);
                        this.config.set("toprankings." + type.getName() + "." + (placeInArray) + ".player", ((Player) getPlayer(type, i)).getUniqueId().toString());
                        this.config.set("toprankings." + type.getName() + "." + (placeInArray) + ".elo", i);
                        try {
                            this.config.save(this.file);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else if (getPlayer(type, i) instanceof OfflinePlayer) {
                        ply.sendMessage(ChatColor.GREEN + "" + (placeInArray) + ".)" + ChatColor.RESET + "               " + ChatColor.YELLOW + ((OfflinePlayer) getPlayer(type, i)).getName() + ": " + ChatColor.GREEN + i);
                        topGametypeRankings.put(((OfflinePlayer) getPlayer(type, i)).getUniqueId(), type, i);
                        this.config.set("toprankings." + type.getName() + "." + (placeInArray) + ".player", ((OfflinePlayer) getPlayer(type, i)).getUniqueId().toString());
                        this.config.set("toprankings." + type.getName() + "." + (placeInArray) + ".elo", i);
                        try {
                            this.config.save(this.file);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        } else {
            List<Integer> topRankings = new ArrayList<>();
            for (int i = 0; i < ints.size(); i++) {
                topRankings.add(ints.get(i));
            }
            Collections.sort(ints);
            for (Integer i : topRankings) {
                int placeInArray = 1;
                for (int i2 = 0; i2 < topRankings.size(); i2++) {
                    if (topRankings.get(i2) == i) {
                        placeInArray += i2;
                    }
                }
                if (getPlayer(type, i) instanceof Player) {
                    ply.sendMessage(ChatColor.GREEN + "" + (placeInArray) + ".)" + ChatColor.RESET + "               " + ChatColor.YELLOW + ((Player) getPlayer(type, i)).getName() + ": " + ChatColor.GREEN + i);
                    topGametypeRankings.put(((Player) getPlayer(type, i)).getUniqueId(), type, i);
                    this.config.set("toprankings." + type.getName() + "." + (placeInArray) + ".player", ((Player) getPlayer(type, i)).getUniqueId().toString());
                    this.config.set("toprankings." + type.getName() + "." + (placeInArray) + ".elo", i);
                    try {
                        this.config.save(this.file);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (getPlayer(type, i) instanceof OfflinePlayer) {
                    ply.sendMessage(ChatColor.GREEN + "" + (placeInArray) + ".)" + ChatColor.RESET + "               " + ChatColor.YELLOW + ((OfflinePlayer) getPlayer(type, i)).getName() + ": " + ChatColor.GREEN + i);
                    topGametypeRankings.put(((OfflinePlayer) getPlayer(type, i)).getUniqueId(), type, i);
                    this.config.set("toprankings." + type.getName() + "." + (placeInArray) + ".player", ((OfflinePlayer) getPlayer(type, i)).getUniqueId().toString());
                    this.config.set("toprankings." + type.getName() + "." + (placeInArray) + ".elo", i);
                    try {
                        this.config.save(this.file);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public void sendEloAverageTop(Player ply) {
        ply.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "------[" + ChatColor.RESET + ChatColor.GOLD + " Top Elo Stats" + ChatColor.GRAY + ": " + ChatColor.DARK_GREEN + "Average " + ChatColor.YELLOW + ChatColor.STRIKETHROUGH + ChatColor.BOLD + "]------");
        List<Integer> ints = new ArrayList<>();
        averageRankings.values().forEach(average -> {
            ints.add(average);
        });
        Collections.sort(ints);
        Collections.reverse(ints);
        if (ints.size() > 10) {
            List<Integer> topRankings = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                topRankings.add(ints.get(i));
            }
            Collections.sort(ints);
            for (Integer i : topRankings) {
                int placeInArray = 1;
                for (int i2 = 0; i2 < topRankings.size(); i2++) {
                    if (topRankings.get(i2) == i) {
                        placeInArray += i2;
                    }
                }
                while (placeInArray < 11) {
                    if (getPlayer(i) instanceof Player) {
                        ply.sendMessage(ChatColor.GREEN + "" + (placeInArray) + ".)" + ChatColor.RESET + "               " + ChatColor.YELLOW + ((Player)getPlayer(i)).getName() + ": " + ChatColor.GREEN + i);
                        topAverageRankings.put(((Player)getPlayer(i)).getUniqueId(), i);
                        this.config.set("averagetoprankings." + (placeInArray) + ".player", ((Player)getPlayer(i)).getUniqueId().toString());
                        this.config.set("averagetoprankings." + (placeInArray) + ".elo", i);
                        try {
                            this.config.save(this.file);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else if (getPlayer(i) instanceof OfflinePlayer) {
                        ply.sendMessage(ChatColor.GREEN + "" + (placeInArray) + ".)" + ChatColor.RESET + "               " + ChatColor.YELLOW + ((OfflinePlayer)getPlayer(i)).getName() + ": " + ChatColor.GREEN + i);
                        topAverageRankings.put(((OfflinePlayer)getPlayer(i)).getUniqueId(), i);
                        this.config.set("averagetoprankings." + (placeInArray) + ".player", ((OfflinePlayer)getPlayer(i)).getUniqueId().toString());
                        this.config.set("averagetoprankings." + (placeInArray) + ".elo", i);
                        try {
                            this.config.save(this.file);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
            }
        }

        } else {
            List<Integer> topRankings = new ArrayList<>();
            for (int i = 0; i < ints.size(); i++) {
                topRankings.add(ints.get(i));
            }
            Collections.sort(ints);
            for (Integer i : topRankings) {
                int placeInArray = 1;
                for (int i2 = 0; i2 < topRankings.size(); i2++) {
                    if (topRankings.get(i2) == i) {
                        placeInArray += i2;
                    }
                }
                if (getPlayer(i) instanceof Player) {
                    ply.sendMessage(ChatColor.GREEN + "" + (placeInArray) + ".)" + ChatColor.RESET + "               " + ChatColor.YELLOW + ((Player)getPlayer(i)).getName() + ": " + ChatColor.GREEN + i);
                    topAverageRankings.put(((Player)getPlayer(i)).getUniqueId(), i);
                    this.config.set("averagetoprankings." + (placeInArray) + ".player", ((Player)getPlayer(i)).getUniqueId().toString());
                    this.config.set("averagetoprankings." + (placeInArray) + ".elo", i);
                    try {
                        this.config.save(this.file);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (getPlayer(i) instanceof OfflinePlayer) {
                    ply.sendMessage(ChatColor.GREEN + "" + (placeInArray) + ".)" + ChatColor.RESET + "               " + ChatColor.YELLOW + ((OfflinePlayer)getPlayer(i)).getName() + ": " + ChatColor.GREEN + i);
                    topAverageRankings.put(((OfflinePlayer)getPlayer(i)).getUniqueId(), i);
                    this.config.set("averagetoprankings." + (placeInArray) + ".player", ((OfflinePlayer)getPlayer(i)).getUniqueId().toString());
                    this.config.set("averagetoprankings." + (placeInArray) + ".elo", i);
                    try {
                        this.config.save(this.file);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public Object getPlayer(GameType gt, int elo) {
        Map<UUID, Integer> rankings1 = rankings.column(gt);
        for (UUID id : rankings1.keySet()) {
            if (rankings1.get(Bukkit.getPlayer(id).getUniqueId()) == elo) {
                return Bukkit.getPlayer(id);
            } else if (rankings1.get(Bukkit.getOfflinePlayer(id).getUniqueId()) == elo) {
                return Bukkit.getOfflinePlayer(id);
            }
        }
        return null;
    }

    public Object getPlayer(int average) {
        for (UUID id : averageRankings.keySet()) {
            if (averageRankings.get(Bukkit.getPlayer(id).getUniqueId()) == average) {
                return Bukkit.getPlayer(id);
            }
            else if (averageRankings.get(Bukkit.getOfflinePlayer(id).getUniqueId()) == average) {
                return Bukkit.getOfflinePlayer(id);
            }
        }
        return null;
    }

    public void updateRankings() {
        //average ranking updater start
        List<Integer> intss = new ArrayList<>();
        averageRankings.values().forEach(average -> {
            intss.add(average);
        });
        Collections.sort(intss);
        Collections.reverse(intss);
        if (intss.size() > 10) {
            List<Integer> topRankings = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                topRankings.add(intss.get(i));
            }
            Collections.sort(intss);
            for (Integer i : topRankings) {
                for (int z = 0; i < 10; i++) {
                    if (getPlayer(i) instanceof Player) {
                        topAverageRankings.put((UUID)((Player) getPlayer(i)).getUniqueId(), i);
                        this.config.set("averagetoprankings." + (z + 1) + ".player", ((Player)getPlayer(i)).getUniqueId().toString());
                        this.config.set("averagetoprankings." + (z + 1) + ".elo", i);
                        try {
                            this.config.save(this.file);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else if (getPlayer(i) instanceof OfflinePlayer) {
                        topAverageRankings.put(((OfflinePlayer) getPlayer(i)).getUniqueId(), i);
                        this.config.set("averagetoprankings." + (z + 1) + ".player", ((OfflinePlayer)getPlayer(i)).getUniqueId().toString());
                        this.config.set("averagetoprankings." + (z + 1) + ".elo", i);
                        try {
                            this.config.save(this.file);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }

        } else {
            List<Integer> topRankings = new ArrayList<>();
            for (int i = 0; i < intss.size(); i++) {
                topRankings.add(intss.get(i));
            }
            Collections.sort(intss);
            for (Integer i : topRankings) {
                for (int z = 0; i < topRankings.size(); i++) {
                    if (getPlayer(i) instanceof Player) {
                        topAverageRankings.put((UUID)((Player) getPlayer(i)).getUniqueId(), i);
                        this.config.set("averagetoprankings." + (z + 1) + ".player", ((Player)getPlayer(i)).getUniqueId().toString());
                        this.config.set("averagetoprankings." + (z + 1) + ".elo", i);
                        try {
                            this.config.save(this.file);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else if (getPlayer(i) instanceof OfflinePlayer) {
                        topAverageRankings.put(((OfflinePlayer) getPlayer(i)).getUniqueId(), i);
                        this.config.set("averagetoprankings." + (z + 1) + ".player", ((OfflinePlayer)getPlayer(i)).getUniqueId().toString());
                        this.config.set("averagetoprankings." + (z + 1) + ".elo", i);
                        try {
                            this.config.save(this.file);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
        //average ranking updater end

        //top ranking updater start
        for (GameType type : topGametypeRankings.columnKeySet()) {
            Map<UUID, Integer> rankingsForGametype = rankings.column(type);
            List<Integer> ints = new ArrayList<>();
            rankingsForGametype.values().forEach(elo -> {
                ints.add(elo);
            });
            Collections.sort(ints);
            Collections.reverse(ints);
            if (ints.size() > 10) {
                List<Integer> topRankings = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    topRankings.add(ints.get(i));
                }
                Collections.sort(ints);
                for (Integer i : topRankings) {
                    for (int z = 0; z < 10; z++) {
                        if (getPlayer(type, i) instanceof Player) {
                            topGametypeRankings.put(((Player)getPlayer(type, i)).getUniqueId(), type, i);
                            this.config.set("toprankings." + type.getName() + "." + (z + 1) + ".player", ((Player)getPlayer(type, i)).getUniqueId().toString());
                            this.config.set("toprankings." + type.getName() + "." + (z + 1)+ ".elo", i);
                            try {
                                this.config.save(this.file);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else if (getPlayer(type, i) instanceof OfflinePlayer) {
                            topGametypeRankings.put(((OfflinePlayer)getPlayer(type, i)).getUniqueId(), type, i);
                            this.config.set("toprankings." + type.getName() + "." + (z + 1) + ".player", ((OfflinePlayer)getPlayer(type, i)).getUniqueId().toString());
                            this.config.set("toprankings." + type.getName() + "." + (z + 1)+ ".elo", i);
                            try {
                                this.config.save(this.file);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            } else {
                List<Integer> topRankings = new ArrayList<>();
                for (int i = 0; i < ints.size(); i++) {
                    topRankings.add(ints.get(i));
                }
                Collections.sort(ints);
                for (Integer i : topRankings) {
                    for (int z = 0; z < topRankings.size(); z++) {
                        if (getPlayer(type, i) instanceof Player) {
                            topGametypeRankings.put(((Player)getPlayer(type, i)).getUniqueId(), type, i);
                            this.config.set("toprankings." + type.getName() + "." + (z + 1) + ".player", ((Player)getPlayer(type, i)).getUniqueId().toString());
                            this.config.set("toprankings." + type.getName() + "." + (z + 1)+ ".elo", i);
                            try {
                                this.config.save(this.file);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else if (getPlayer(type, i) instanceof OfflinePlayer) {
                            topGametypeRankings.put(((OfflinePlayer)getPlayer(type, i)).getUniqueId(), type, i);
                            this.config.set("toprankings." + type.getName() + "." + (z + 1) + ".player", ((OfflinePlayer)getPlayer(type, i)).getUniqueId().toString());
                            this.config.set("toprankings." + type.getName() + "." + (z + 1)+ ".elo", i);
                            try {
                                this.config.save(this.file);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        //top ranking updater end
    }

    public UUID[] toUUIDArray(String s) {
        String[] strings = s.split("\\|");
        UUID[] ids = new UUID[strings.length - 1];
        int i = 0;
        for (String s2 : strings) {
            ids[i] = UUID.fromString(s2);
            i++;
        }
        return ids;
    }
}
