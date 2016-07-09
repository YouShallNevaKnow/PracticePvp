package com.slurpeh.servercore.practice.inventory;

import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.gametype.GameType;
import com.slurpeh.servercore.practice.player.Kit;
import com.slurpeh.servercore.practice.player.PlayerDataManager;
import com.slurpeh.servercore.practice.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bradley on 5/15/16.
 */
public class KitEditorManager implements Listener {
    //  incomplete
    private KohiPractice plugin;
    private HashMap<Player, GameType> editing;
    private Location editLocation;
    private HashMap<Player, Kit> renaming;
    private PlayerDataManager pdm;
    private HashMap<Player, Inventory> menus;
    private List<Player> clickCooldown;

    public KitEditorManager(KohiPractice plugin) {
        this.plugin = plugin;
        this.editing = new HashMap<>();
        if (plugin.getConfig().getConfigurationSection("editing") != null) {
            plugin.getConfig().getConfigurationSection("editing").getKeys(false).forEach(id -> {
                if (Bukkit.getPlayer(id) != null) {
                    editing.put(Bukkit.getPlayer(id), plugin.getGameTypeManager().getGameType(plugin.getConfig().getString("editing." + id)));
                    beginEditing(Bukkit.getPlayer(id), plugin.getGameTypeManager().getGameType(plugin.getConfig().getString("editing." + id)));
                }
            });
        }
        this.editLocation = LocationUtil.getLocation(plugin.getConfig().getString("editor"));
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
        this.renaming = new HashMap<>();
        this.menus = new HashMap<>();
        this.pdm = plugin.getPlayerDataManager();
    }

    public void beginEditing(Player ply, GameType gt) {
        ply.teleport(editLocation);
        ply.getInventory().clear();
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player eply : editing.keySet()) {
                    eply.hidePlayer(ply);
                    ply.hidePlayer(eply);
                }
            }
        }.runTaskLater(this.plugin, 2l);
        this.editing.put(ply, gt);
        this.plugin.getConfig().set("editing." + ply.getUniqueId(), gt.getName());
        this.plugin.saveConfig();
        ply.sendMessage("Now editing kits for " + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', gt.getName())));
    }

    @EventHandler
    public void onRightClickSign(final PlayerInteractEvent event) {
        final Player ply = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && (event.getClickedBlock().getType() == Material.SIGN || event.getClickedBlock().getType() == Material.WALL_SIGN || event.getClickedBlock().getType() == Material.SIGN_POST) && this.editing.containsKey(ply)) {
            this.editing.remove(ply);
            this.plugin.getConfig().set("editing." + ply.getUniqueId(), null);
            this.plugin.saveConfig();
            ply.teleport(this.plugin.getSpawn());
            this.plugin.getInventorySetter().setupInventory(InventoryType.DEFAULT, ply);
            for (final Player eply : Bukkit.getOnlinePlayers()) {
                if (ply != eply && !plugin.getConfig().getBoolean("hide-players")) {
                    ply.showPlayer(eply);
                    eply.showPlayer(ply);
                } else {
                    ply.hidePlayer(eply);
                    eply.hidePlayer(ply);
                }
            }
        }
    }

    @EventHandler
    public void onInteractWithBook(PlayerInteractEvent e) {
        Player ply = e.getPlayer();
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (ply.getItemInHand().isSimilar(new ItemBuilder(Material.BOOK, "&6Kit Editor", "").getItem())) {
                openGameTypes(ply);
            }
        }
    }

    @EventHandler
    public void onRightClick(InventoryClickEvent e) {
        Player ply = (Player) e.getWhoClicked();
        if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta()) {
            if (e.getClickedInventory().getSize() == getKitEditorSize() && e.getClickedInventory().getTitle().equalsIgnoreCase("Edit Kits")) {
                for (GameType gt : plugin.getGameTypeManager().getGameTypes()) {
                    if (("Edit " + gt.getName() + " kits").equalsIgnoreCase(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', e.getCurrentItem().getItemMeta().getDisplayName())))) {
                        beginEditing(ply, gt);
                        System.out.println("began editing " + gt.getName());
                        e.setCancelled(true);
                        ply.closeInventory();
                    }
                }
            }
        }
        if (e.getInventory().getTitle().equalsIgnoreCase("Edit Kits")) {
            e.setCancelled(true);
        }
    }


    public void openGameTypes(Player ply) {
        Inventory inv = new InventoryBuilder(getKitEditorSize(), "Edit Kits", false, false).build();
        int i = 0;
        for (GameType gt : plugin.getGameTypeManager().getGameTypes()) {
            if (gt.isSetup() && gt.isEditable()) {
                inv.setItem(i, new ItemBuilder(gt.getDisplay().getType(), "&9Edit &r&b" + gt.getName() + "&r&9 kits", "").getItem());
                i++;
            }
        }
        ply.openInventory(inv);
        ply.updateInventory();
    }

    private int getKitEditorSize() {
        final List<GameType> editableGametypes = new ArrayList<>();
        final int games = KohiPractice.getInstance().getGameTypeManager().getGameTypes().size();
        for (int i = 0; i < games; i++) {
            if (KohiPractice.getInstance().getGameTypeManager().getGameTypes().get(i).isEditable() && KohiPractice.getInstance().getGameTypeManager().getGameTypes().get(i).isSetup()) {
                editableGametypes.add(KohiPractice.getInstance().getGameTypeManager().getGameTypes().get(i));
            } else {
                continue;
            }
        }
        int e = editableGametypes.size();
        if (e >= 1 && e <= 9) {
            return 9;
        } else if (e >= 10 && e <= 18) {
            return 18;
        } else if (e >= 19 && e <= 27) {
            return 27;
        } else if (e >= 28 && e <= 36) {
            return 36;
        } else if (e >= 37 && e <= 45) {
            return 45;
        } else if (e >= 46 && e <= 54) {
            return 54;
        } else {
            return 54;
        }
    }

    @EventHandler
    public void onItemDrop(final PlayerDropItemEvent event) {
        final Player ply = event.getPlayer();
        if (this.editing.containsKey(ply)) {
            final EntityHider hider = this.plugin.getEntityHider();
            for (final Player p : Bukkit.getOnlinePlayers()) {
                if (p != ply) {
                    hider.hideEntity(ply, (Entity) event.getItemDrop());
                }
            }
            new BukkitRunnable() {
                public void run() {
                    event.getItemDrop().remove();
                }
            }.runTaskLater((Plugin) this.plugin, 200L);
        }
    }

    @EventHandler
    public void onOpenChest(final PlayerInteractEvent event) {
        final Player ply = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CHEST && this.editing.containsKey(ply)) {
            event.setCancelled(true);
            final GameType gt = this.editing.get(ply);
            Inventory inv = Bukkit.createInventory(null, 54, gt.getName());
            inv.setContents(gt.getPossibleGear().getContents());
            ply.openInventory(inv);
            ply.updateInventory();
        }
    }

    @EventHandler
    public void onClickAnvil(final PlayerInteractEvent event) {
        final Player ply = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.ANVIL && this.editing.containsKey(ply)) {
            final Inventory inv = getKitMenu(ply, this.editing.get(ply));
            this.menus.put(ply, inv);
            ply.openInventory(inv);
            ply.updateInventory();
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player ply = (Player) e.getWhoClicked();
        if (editing.containsKey(ply)) {
            GameType gt = editing.get(ply);
            if (e.getClickedInventory().getTitle().equalsIgnoreCase("Manage " + editing.get(ply).getName() + " kits")) {
                if (e.getCurrentItem().hasItemMeta() && e.getCurrentItem() != null) {
                    String itemName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
                    if (itemName.equals("")) {
                        return;
                    }
                    String[] itemNameA = itemName.split(" ");
                    String option = itemNameA[0] + " " + itemNameA[1] + " ";
                    String kitName = itemName.replaceFirst(option, "");
                    switch (option) {
                        case "Save kit ": {
                            int kitn = Integer.parseInt(kitName);
                            Kit kit1 = new Kit("Custom " + gt.getName() + " kit " + kitn, PlyInv.fromPlayerInventory(ply.getInventory()));
                            pdm.setKit(ply, gt, kitn, kit1);
                            ply.sendMessage(ChatColor.GREEN + "Saved kit: " + ChatColor.GOLD + kitName);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    updateMenu(ply, gt);
                                }
                            }.runTaskLater(plugin, 5);
                            break;
                        }
                        case "Save kit: ": {
                            Kit kit2 = this.pdm.getKit(ply, gt, this.getPostion(e.getSlot()));
                            kit2.setInv(PlyInv.fromPlayerInventory((PlayerInventory) ply.getInventory()));
                            ply.sendMessage((Object) ChatColor.GREEN + "Saved kit: " + (Object) ChatColor.GOLD + kitName);
                            break;
                        }
                        case "Load kit: ": {
                            Kit kit3 = this.pdm.getKit(ply, gt, this.getPostion(e.getSlot()));
                            if (kit3.getInv().getArmorContents() != null) {
                                ply.getInventory().setArmorContents(kit3.getInv().getArmorContents());
                            }
                            ply.getInventory().setContents(kit3.getInv().getContents());
                            ply.sendMessage((Object) ChatColor.GREEN + "Loaded kit: " + (Object) ChatColor.GOLD + kit3.getName());
                            break;
                        }
                        case "Rename kit: ": {
                            Kit kit4 = this.pdm.getKit(ply, gt, this.getPostion(e.getSlot()));
                            if (this.renaming.containsKey((Object) ply)) {
                                ply.sendMessage((Object) ChatColor.RED + "Cancelling renaming of " + ((Kit) this.renaming.get((Object) ply)).getName());
                                this.renaming.remove((Object) ply);
                            }
                            this.renaming.put(ply, kit4);
                            ply.sendMessage((Object) ChatColor.GREEN + "Type a new name for " + (Object) ChatColor.GOLD + kit4.getName());
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (! renaming.containsKey(ply)) return;
                                    if (renaming.get(ply) != kit4) return;
                                    renaming.remove(ply);
                                    ply.sendMessage(ChatColor.RED + "Renaming cancelled!");
                                }
                            }.runTaskLater(this.plugin, 300);
                            break;
                        }
                        case "Delete kit: ": {
                            this.pdm.removeKit(ply, gt, this.getPostion(e.getSlot()));
                            ply.sendMessage((Object) ChatColor.RED + "Deleted kit: " + (Object) ChatColor.GOLD + kitName);
                            break;
                        }
                    }
                    ply.closeInventory();
                }
            }
        }
    }

    public Inventory getKitMenu(Player ply, GameType gt) {
        Inventory inv = new InventoryBuilder(36, "Manage " + gt.getName() + " kits", false, false).build();
        for (int i = 1; i <= 5; i++) {
            Kit kit = pdm.getKit(ply, gt, i);
            int slot = (i - 1) * 2;
            if (kit == null) {
                inv.setItem(slot, new ItemBuilder(Material.CHEST, ChatColor.GREEN + "Save kit " + ChatColor.GOLD + i, "").getItem());
                continue;
            }
            String kitName = kit.getName();
            inv.setItem(slot, new ItemBuilder(Material.CHEST, ChatColor.GREEN + "Save kit " + ChatColor.GOLD + kitName, "").getItem());
            inv.setItem(slot + 9, new ItemBuilder(Material.ENCHANTED_BOOK, ChatColor.YELLOW + "Load kit: " + ChatColor.GOLD + kitName, "").getItem());
            inv.setItem(slot + 18, new ItemBuilder(Material.NAME_TAG, ChatColor.YELLOW + "Rename kit: " + ChatColor.GOLD + kitName, "").getItem());
            inv.setItem(slot + 27, new ItemBuilder(Material.FIRE, ChatColor.RED + "Delete kit: " + ChatColor.GOLD + kitName, "").getItem());
        }
        return inv;
    }

    @EventHandler
    public void onRenameKit(final AsyncPlayerChatEvent event) {
        final Player ply = event.getPlayer();
        if (this.renaming.containsKey(ply)) {
            final Kit kit = this.renaming.get(ply);
            final String newName = event.getMessage();
            ply.sendMessage(ChatColor.GOLD + kit.getName() + ChatColor.GREEN + " renamed to: " + ChatColor.GOLD + newName);
            this.renaming.get(ply).setName(newName);
            this.renaming.remove(ply);
            this.pdm.saveKits(ply);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (this.menus.containsKey(event.getPlayer())) {
            this.menus.remove(event.getPlayer());
        }
    }

    public void updateMenu(final Player ply, final GameType gt) {
        if (! this.menus.containsKey(ply)) {
            return;
        }
        final Inventory menu = this.menus.get(ply);
        for (int i = 1; i <= 5; ++ i) {
            final Kit kit = this.pdm.getKit(ply, gt, i);
            final int slot = (i - 1) * 2;
            if (kit == null) {
                menu.setItem(slot, new ItemBuilder(Material.CHEST, ChatColor.GREEN + "Save kit " + ChatColor.GOLD + i, "").getItem());
            } else {
                final String kitName = kit.getName();
                menu.setItem(slot, new ItemBuilder(Material.CHEST, ChatColor.GREEN + "Save kit: " + ChatColor.GOLD + kitName, "").getItem());
                menu.setItem(slot + 9, new ItemBuilder(Material.ENCHANTED_BOOK, ChatColor.YELLOW + "Load kit: " + ChatColor.GOLD + kitName, "").getItem());
                menu.setItem(slot + 18, new ItemBuilder(Material.NAME_TAG, ChatColor.YELLOW + "Rename kit: " + ChatColor.GOLD + kitName, "").getItem());
                menu.setItem(slot + 27, new ItemBuilder(Material.FIRE, ChatColor.RED + "Delete kit: " + ChatColor.GOLD + kitName, "").getItem());
            }
        }
    }

    private int getPostion(int slot) {
        if (slot > 26) {
            slot -= 27;
        } else if (slot > 17) {
            slot -= 18;
        } else if (slot > 8) {
            slot -= 9;
        }
        return slot / 2 + 1;
    }

    public boolean isEditing(final Player player) {
        return this.editing.containsKey(player);
    }
}
