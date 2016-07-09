package com.slurpeh.servercore.practice.inventory;

import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.gametype.GameType;
import com.slurpeh.servercore.practice.team.Team;
import com.slurpeh.servercore.practice.util.InventoryBuilder;
import com.slurpeh.servercore.practice.util.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Bradley on 4/30/16.
 */
public class InventorySetter {
   // incomplete
    KohiPractice plugin;

    public InventorySetter(KohiPractice plugin) {
        this.plugin = plugin;
    }

    public void setupInventory(InventoryType type, Player ply) {
        switch (type) {
            case DEFAULT: {
                ply.getInventory().clear();
                ItemStack kitEditor = new ItemBuilder(Material.BOOK, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("kitEditor")), "").getItem();
                ply.getInventory().setItem(0, kitEditor);
                ItemStack unrankedItem = new ItemBuilder(Material.IRON_SWORD, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("unrankedItem")), "").getItem();
                ply.getInventory().setItem(4, unrankedItem);
                ItemStack rankedItem = new ItemBuilder(Material.DIAMOND_SWORD, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("rankedItem")), "").getItem();
                ply.getInventory().setItem(8, rankedItem);
                ply.updateInventory();
                return;
            }
            case PARTY_LEADER: {
                ply.getInventory().clear();
                ItemStack star = new ItemBuilder(Material.NETHER_STAR, "&9Your team", "").getItem();
                ply.getInventory().setItem(0, star);
                ItemStack head = new ItemBuilder(Material.SKULL_ITEM, "&9List all members in your Team", "", 1, (short)SkullType.PLAYER.ordinal()).getItem();
                ply.getInventory().setItem(1, head);
                ItemStack fire = new ItemBuilder(Material.FIRE, "&cLeave this team.", "").getItem();
                ply.getInventory().setItem(3, fire);
                ItemStack eye = new ItemBuilder(Material.EYE_OF_ENDER, "&9Show other teams to duel", "").getItem();
                ply.getInventory().setItem(5, eye);
                ItemStack unranked2v2 = new ItemBuilder(Material.DIAMOND_SWORD, "&9Join 2v2 unranked queue", "").getItem();
                ply.getInventory().setItem(7, unranked2v2);
                ItemStack gold = new ItemBuilder(Material.GOLD_SWORD, "&eStart a Team Event", "").getItem();
                ply.getInventory().setItem(8, gold);
                ply.updateInventory();
                return;
            }
            case PARTY_MEMBER: {
                ply.getInventory().clear();
                ItemStack star = new ItemBuilder(Material.NETHER_STAR, "&9Your team", "").getItem();
                ply.getInventory().setItem(0, star);
                ItemStack head = new ItemBuilder(Material.SKULL_ITEM, "&9List all members in your Team", "", 1, (short)SkullType.PLAYER.ordinal()).getItem();
                ply.getInventory().setItem(0, head);
                ItemStack fire = new ItemBuilder(Material.FIRE, "&cLeave this team.", "").getItem();
                ply.getInventory().setItem(1, fire);
                ItemStack other = new ItemBuilder(Material.EYE_OF_ENDER, "&9Show other teams to duel", "").getItem();
                ply.getInventory().setItem(4, other);
                return;
            }
            case STAFF: {
                if (plugin.getUtilityManager().isStaff(ply.getUniqueId())) {
                    ItemStack reports = new ItemBuilder(Material.PAPER, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("reportShower")), "").getItem();
                    ply.getInventory().setItem(0, reports);
                }
                return;
            }
        }
    }
}
