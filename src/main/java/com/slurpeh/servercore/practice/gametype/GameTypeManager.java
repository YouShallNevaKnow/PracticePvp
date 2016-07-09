package com.slurpeh.servercore.practice.gametype;

import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.arena.Arena;
import com.slurpeh.servercore.practice.player.Kit;
import com.slurpeh.servercore.practice.util.MiscUtil;
import com.slurpeh.servercore.practice.util.PlyInv;
import com.sun.tools.hat.internal.util.Misc;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by Bradley on 4/29/16.
 */
public class GameTypeManager implements Listener {
    private List<GameType> gameTypes;
    private FileConfiguration config;
    private List<Player> editing;
    private KohiPractice plugin;

    public GameTypeManager(KohiPractice plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.editing = new ArrayList<>();
        this.gameTypes = new ArrayList<>();
        if (this.config.getConfigurationSection("gametypes") != null) {
            this.config.getConfigurationSection("gametypes").getKeys(false).forEach(s -> {
                GameType gt = new GameType(s);
                if (this.config.getString("gametypes." + s + ".items") != null) {
                    String in = config.getString("gametypes." + s + ".items");
                    String kitName = in.split("\\|")[0];
                    int startIndex = in.indexOf("|");
                    PlyInv inv = MiscUtil.playerInventoryFromString(in.substring(startIndex, in.length() - 1));
                    gt.setStartingKit(new Kit(kitName, inv));
                }
                if (this.config.getStringList("gametypes." + s + ".arenas") != null) {
                    gt.setPossibleArenas((List<Arena>)this.config.getStringList("gametypes." + s + ".arenas").stream().map(an -> this.plugin.getArenaManager().getArena(an)).collect(Collectors.toList()));
                }
                if (this.config.getStringList("gametypes." + s + ".display") != null) {
                    gt.setDisplay(MiscUtil.itemStackFromString(this.config.getString("gametypes." + s + ".display")));
                }
                if (this.config.getString("gametypes." + s + ".display-name") != null) {
                    gt.setDisplayName(this.config.getString("gametypes." + s + ".display-name"));
                }
                if (this.config.getBoolean("gametypes." + s + ".editable")) {
                    gt.setEditable(config.getBoolean("gametypes." + s + ".editable"));
                }
                if (this.config.getBoolean("gametypes." + s + ".bandb")) {
                    gt.setPlaceAndBreak(config.getBoolean("gametypes." + s + ".bandb"));
                }
                if (this.config.getStringList("gametypes." + s + ".possible-gear") != null) {
                    Inventory inv = Bukkit.createInventory(null, 54, ChatColor.stripColor(gt.getDisplayName()));
                    inv.setContents(MiscUtil.inventoryFromString(this.config.getString("gametypes." + s + ".possible-gear")).getContents());
                    gt.setPossibleGear(inv);
                }
                this.gameTypes.add(gt);
                return;
            });
        } else {
            config.set("gametypes", new ArrayList<String>());
        }
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    public List<GameType> getGameTypes() {
        return gameTypes;
    }

    public GameType getGameType(String name) {
        for (GameType type : gameTypes) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    public GameType getGameTypeFromDisplayName(final String displayName) {
        for (GameType gt : gameTypes) {
            if (displayName.equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', gt.getDisplayName()))) {
                return gt;
            }
        }
        return null;
    }

    public GameType getGameTypeFromDisplayNameColorless(final String displayName) {
        for (GameType gt : gameTypes) {
            if (ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', gt.getDisplayName())).equals(displayName)) {
                return gt;
            }
        }
        return null;
    }

    public boolean doesGameTypeExist(final String name) {
        for (final GameType gt : this.getGameTypes()) {
            if (gt.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void createGameType(final String name) {
        final GameType gt = new GameType(name);
        this.gameTypes.add(gt);
        this.saveGameTypes();
        //this.plugin.getDuelManager().setupMenu();
        this.plugin.getPlayerDataManager().setupNewGameType(gt);
    }

    public void removeGameType(final String name) {
        final GameType gt = this.getGameType(name);
        this.gameTypes.remove(gt);
        this.saveGameTypes();
      //  this.plugin.getDuelManager().setupMenu();
    }


    public void addEditing(final Player ply) {
        this.editing.add(ply);
    }

    public void saveGameTypes() {
        this.config.set("gametype", (Object)null);
        for (final GameType gt : this.gameTypes) {
            this.config.set("gametypes." + gt.getName() + ".items", MiscUtil.playerInventoryToString(gt.getStartingKit().getInv()));
            List<String> arenas = new ArrayList<>();
            for (Arena a : gt.getPossibleArenas()) {
                arenas.add(a.getName());
            }
            this.config.set("gametypes." + gt.getName() + ".arenas", arenas);
            if (gt.getDisplay() != null) {
                this.config.set("gametypes." + gt.getName() + ".display", (Object)MiscUtil.itemStackToString(gt.getDisplay()));
            }
            this.config.set("gametypes." + gt.getName() + ".display-name", (Object)gt.getDisplayName());
            this.config.set("gametypes." + gt.getName() + ".editable", (Object)gt.isEditable());
            if (gt.getPossibleGear() != null && gt.getPossibleGear().getContents() != null) {
                this.config.set("gametypes." + gt.getName() + ".possible-gear", (Object)MiscUtil.inventoryToString(gt.getPossibleGear()));
            }
            this.config.set("gametypes." + gt.getName() + ".bandb", gt.canPlaceAndBreak());
        }
        this.plugin.saveConfig();
    }

    @EventHandler
    public void onInvClose(final InventoryCloseEvent event) {
        if (this.editing.contains(event.getPlayer()) && getGameType(event.getInventory().getTitle()) != null) {
            final GameType gt = getGameType(event.getInventory().getTitle());
            gt.setPossibleGear(event.getInventory());
            this.saveGameTypes();
            this.editing.remove(event.getPlayer());
        }
    }
}
