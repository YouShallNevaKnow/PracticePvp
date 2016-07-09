package com.slurpeh.servercore.practice;

//import ca.wacos.nametagedit.NametagAPI;

import com.slurpeh.servercore.practice.arena.ArenaCommand;
import com.slurpeh.servercore.practice.arena.ArenaManager;
import com.slurpeh.servercore.practice.gametype.GameTypeCommand;
import com.slurpeh.servercore.practice.gametype.GameTypeManager;
import com.slurpeh.servercore.practice.inventory.*;
import com.slurpeh.servercore.practice.match.DuelManager;
import com.slurpeh.servercore.practice.match.Match;
import com.slurpeh.servercore.practice.match.MatchManager;
import com.slurpeh.servercore.practice.player.*;
import com.slurpeh.servercore.practice.team.TeamCommands;
import com.slurpeh.servercore.practice.team.TeamDuelManager;
import com.slurpeh.servercore.practice.team.TeamManager;
import com.slurpeh.servercore.practice.team.TeamMatch;
import com.slurpeh.servercore.practice.twovtwos.Match2v2;
import com.slurpeh.servercore.practice.twovtwos.TwoVTwoMatchManager;
import com.slurpeh.servercore.practice.twovtwos.UnrankedInventory2v2;
import com.slurpeh.servercore.practice.util.EntityHider;
import com.slurpeh.servercore.practice.util.LocationUtil;
import com.slurpeh.servercore.practice.util.UtilityManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Random;

public class KohiPractice extends JavaPlugin implements Listener {
    private static KohiPractice instance;
    private InventoryManager inventoryManager;
    private TeamManager teamManager;
    private ArenaManager arenaManager;
    private EntityHider entityHider;
    private InventorySetter inventorySetter;
    private GameTypeManager gametypeManager;
    private UtilityManager utilityManager;
    private PlayerDataManager playerDataManager;
    private RankingManager rankingManager;
    private RankedInventory rankedInventory;
    private UnrankedInventory unrankedInventory;
    private MatchManager matchManager;
    private DuelManager duelManager;
    private KitEditorManager kitEditorManager;
    private TeamDuelManager teamDuelManager;
    private TwoVTwoMatchManager twoVTwoMatchManager;
    private UnrankedInventory2v2 unranked2v2inventory;
    private Location spawn;
    private Location editor;

    public static KohiPractice getInstance() {
        return KohiPractice.instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        new Initializer().startup();
    }

    public String encrypt(String s) {
        StringBuffer sb = new StringBuffer();
        for (char c : s.toCharArray()) {
            char c2 = (char)shift(c);
            sb.append(c2);
        }

        return sb.toString();
    }

    public int shift(char c) {
        int temp = (int)c;
        temp += 16;
        temp /= 2;
        temp *= 3;
        return temp;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        this.getInventorySetter().setupInventory(InventoryType.DEFAULT, e.getPlayer());
        for (Player ply : Bukkit.getOnlinePlayers()) {
            if (ply != null && ply != e.getPlayer()) {
                ply.hidePlayer(e.getPlayer());
                e.getPlayer().hidePlayer(ply);
            }
        }
    }

    @Override
    public void onDisable() {
        end();
    }

    public Location getSpawn() { return this.spawn; }

    public MatchManager getMatchManager() { return this.matchManager; }

    public InventoryManager getInventoryManager() { return this.inventoryManager; }

    public RankedInventory getRankedInventory() { return this.rankedInventory; }

    public UnrankedInventory getUnrankedInventory() { return this.unrankedInventory; }

    public KitEditorManager getKitEditorManager() { return this.kitEditorManager; }

    public TeamManager getTeamManager() { return this.teamManager; }

    public RankingManager getRankingManager() { return this.rankingManager; }

    public DuelManager getDuelManager() { return this.duelManager; }

    public PlayerDataManager getPlayerDataManager() {
        return this.playerDataManager;
    }

    public UtilityManager getUtilityManager() { return this.utilityManager; }

    public InventorySetter getInventorySetter() { return this.inventorySetter; }

    public TeamDuelManager getTeamDuelManager() { return this.teamDuelManager; }

    public TwoVTwoMatchManager get2v2MatchManager() { return this.twoVTwoMatchManager; }

    public GameTypeManager getGameTypeManager() {
        return this.gametypeManager;
    }

    public ArenaManager getArenaManager() {
        return this.arenaManager;
    }

    public EntityHider getEntityHider() {
        return this.entityHider;
    }

    public UnrankedInventory2v2 getUnranked2v2Inventory() { return this.unranked2v2inventory; }

    public void loadConfiguration() throws Exception {
        if (this.getConfig().getString("kitEditor") == null) {
            this.getConfig().addDefault("kitEditor", "&6Kit Editor");
        }
        if (this.getConfig().getString("unrankedItem") == null) {
            this.getConfig().addDefault("unrankedItem", "&9Un-Ranked Queue");
        }
        if (this.getConfig().getStringList("rankedItem") == null) {
            this.getConfig().addDefault("rankedItem", "&aRanked Queue");
        }
        if (this.getConfig().getConfigurationSection("arenas") == null) {
            this.getConfig().addDefault("arenas", new ArrayList<String>());
        }
        if (this.getConfig().getConfigurationSection("gametypes") == null) {
            this.getConfig().addDefault("gametypes", new ArrayList<String>());
        }
        if (getConfig().getInt("unranked-games-min") == 0) {
            getConfig().addDefault("unranked-games-min", 20);
        }
        if (getConfig().getString("key") == null) {
            getConfig().addDefault("key", "");
        }
        if (getConfig().getString("value") == null) {
            getConfig().addDefault("value", "");
        }
        if (getConfig().getString("license") == null) {
            getConfig().addDefault("license", "");
        }
        if (getConfig().getString("owner-ip") == null) {
            getConfig().addDefault("owner-ip", "");
        }
        getConfig().addDefault("hide-players", true);
        this.saveConfig();
    }

    public FileConfiguration retrieveConfig() {
        return getConfig();
    }

    public void end() {
        for (Match m : getMatchManager().getMatches()) {
            m.undoBuilds();
        }
        for (Match2v2 m2v2 : get2v2MatchManager().get2v2Matches()) {
            m2v2.undoBuilds();
        }
        for (TeamMatch tm : getTeamManager().getTeamMatches()) {
            tm.undoBuilds();
        }
        getTeamManager().teams.clear();
        instance = null;
    }

    public void instantiateObjects() {
        //instantiates playerdatamanager
        this.utilityManager = new UtilityManager(this);
        this.inventorySetter = new InventorySetter(this);
        this.inventoryManager = new InventoryManager(this);
        this.teamManager = new TeamManager(this);
        this.inventorySetter = new InventorySetter(this);
        this.gametypeManager = new GameTypeManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.matchManager = new MatchManager(this);
        this.rankingManager = new RankingManager(this);
        //run the update rankings on endMatch call (PlayerDeathEvent)
        this.playerDataManager = new PlayerDataManager(this);
        this.arenaManager = new ArenaManager(this);
        this.entityHider = new EntityHider(this, EntityHider.Policy.BLACKLIST);
        ArenaCommand arenaCmd = new ArenaCommand(this);
        EloCommand eloCmd = new EloCommand(this);
        GameTypeCommand gtCmd = new GameTypeCommand(this);
        this.getCommand("elo").setExecutor(eloCmd);
        this.getCommand("arena").setExecutor(arenaCmd);
        this.getCommand("gametype").setExecutor(gtCmd);
        this.getCommand("set").setExecutor(new SetCmd());
        this.getCommand("team").setExecutor(new TeamCommands(this));
        this.rankedInventory = new RankedInventory();
        this.unrankedInventory = new UnrankedInventory();
        KohiPractice.getInstance().getServer().getPluginManager().registerEvents(rankedInventory, KohiPractice.getInstance());
        KohiPractice.getInstance().getServer().getPluginManager().registerEvents(unrankedInventory, KohiPractice.getInstance());
        this.duelManager = new DuelManager(this);
        this.kitEditorManager = new KitEditorManager(this);
        this.teamDuelManager = new TeamDuelManager(this);
        new PlayerEvents(this);
        this.getCommand("spectate").setExecutor(new SpectateCommand(this));
        this.getCommand("c").setExecutor(new ChatCommand(this));
        this.twoVTwoMatchManager = new TwoVTwoMatchManager(this);
        this.unranked2v2inventory = new UnrankedInventory2v2(this);
    }

    public boolean confirm() {
        return true;
    }

    public static class AESEncryptor {
        public AESEncryptor() {

        }

        public String encrypt(String s) {
            StringBuilder s2 = new StringBuilder(s);
            for (int i = 0; i < s2.length(); i++) {
                int temp = (int) s2.charAt(i);
                temp *= 13;
                temp += 1;
                s2.setCharAt(i, (char) temp);
            }
            return s2.reverse().toString();
        }

        public String decrypt(String s) {
            StringBuffer toReturn = new StringBuffer(s);
            for (int i = 0; i < toReturn.length(); i++) {
                int temp = (int) toReturn.charAt(i);
                temp -= 1;
                temp /= 13;
                toReturn.setCharAt(i, (char) temp);
            }
            return new String(toReturn.reverse());
        }
    }

    public class Initializer {
        public void startup() {
            if (confirm()) {
                KohiPractice.this.getServer().getPluginManager().registerEvents(KohiPractice.this, KohiPractice.this);
                KohiPractice.this.saveDefaultConfig();
                try {
                    loadConfiguration();
                } catch (Exception ex) {
                }
                instantiateObjects();
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Loaded all player info + data successfully!");
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Loaded all managers correctly!");
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "WARNING: /RELOAD IS NOT SUPPORTED WITH THIS PLUGIN, PLEASE USE /STOP OR /RESTART");
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "ALSO: SKINS WILL BE REMOVED/STRIPPED IN MATCHES (TO STEVE SKIN)");
                if (KohiPractice.this.getConfig().contains("spawn")) {
                    KohiPractice.this.spawn = LocationUtil.getLocation(KohiPractice.this.getConfig().getString("spawn"));
                }
                if (getConfig().contains("editor")) {
                    editor = LocationUtil.getLocation(getConfig().getString("editor"));
                }
                rankingManager.updateRankings();
                for (Match m : getMatchManager().getMatches()) {
                    m.setRanked(false);
                    m.endMatch(m.getPlayers()[new Random().nextInt(m.getPlayers().length)]);
                }
                for (Player ply : Bukkit.getOnlinePlayers()) {
                    playerDataManager.getPlayerData(ply).save();
                    getInventorySetter().setupInventory(InventoryType.DEFAULT, ply);
                    for (Player ply2 : Bukkit.getOnlinePlayers()) {
                        if (getConfig().getBoolean("hide-players")) {
                            ply.hidePlayer(ply2);
                            ply2.hidePlayer(ply);
                        }
                    }
                }
            } else {
                Bukkit.getPluginManager().disablePlugin(KohiPractice.this);
            }
        }
    }

    class SetCmd implements CommandExecutor {
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (sender instanceof Player) {
                Player ply = (Player) sender;
                if (ply.hasPermission("kohipractice.commands.locations")) {
                    if (args.length == 1) {
                        switch (args[0]) {
                            case "spawn": {
                                spawn = ply.getLocation();
                                KohiPractice.this.getConfig().set("spawn", LocationUtil.getString(ply.getLocation()));
                                saveConfig();
                                ply.sendMessage(ChatColor.GREEN + "Spawn location successfully set!");
                                return true;
                            }
                            case "editor": {
                                KohiPractice.this.getConfig().set("editor", LocationUtil.getString(ply.getLocation()));
                                saveConfig();
                                ply.sendMessage(ChatColor.GREEN + "Kit Editor location successfully set!");
                                return true;
                            }
                        }
                    } else {
                        ply.sendMessage(ChatColor.RED + "/set <spawn, editor>");
                        return true;
                    }
                } else {
                    ply.sendMessage(ChatColor.RED + "You do not have permission to execute this command!");
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You must be a player to execute this command!");
                return false;
            }
            return false;
        }
    }
}
