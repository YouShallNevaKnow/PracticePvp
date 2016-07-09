package com.slurpeh.servercore.practice.inventory;

import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.gametype.GameType;
import com.slurpeh.servercore.practice.player.Kit;
import com.slurpeh.servercore.practice.util.ItemBuilder;
import com.slurpeh.servercore.practice.util.MiscUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by Bradley on 5/14/16.
 */
public class InventoryManager implements Listener {
   // incomplete add command
   private HashMap<String, Inventory> invs;
    private List<Player> checkingInvs;
    KohiPractice plugin;
    public InventoryManager(KohiPractice plugin) {
        this.plugin  = plugin;
        this.invs = new HashMap<>();
        this.checkingInvs = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
        this.plugin.getCommand("inventory").setExecutor(new InventoryCommand());
    }

    public void showKits(Player ply, GameType gt) {
        final PlayerInventory inv = ply.getInventory();
        inv.setItem(0, new ItemBuilder(Material.ENCHANTED_BOOK, ChatColor.YELLOW + "Default " + ChatColor.stripColor(gt.getDisplayName()) + " Kit", "", 1).getItem());
        int i = 1;
        if (this.plugin.getPlayerDataManager().getKits(ply, gt) == null) {
            this.plugin.getPlayerDataManager().loadPlayerInfo(ply);
        }
        for (Kit kit : this.plugin.getPlayerDataManager().getKits(ply, gt)) {
            if (kit != null) {
                inv.setItem(i, new ItemBuilder(Material.ENCHANTED_BOOK, ChatColor.BLUE + kit.getName(), "", 1).getItem());
                i++;
            }
        }
    }

    @EventHandler
    public void onInvInteract(final InventoryClickEvent event) {
        if (this.checkingInvs.contains(event.getWhoClicked())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInvClose(final InventoryCloseEvent event) {
        if (this.checkingInvs.contains(event.getPlayer())) {
            this.checkingInvs.remove(event.getPlayer());
        }
    }

    @EventHandler
    public void onRightClick(final PlayerInteractEvent event) {
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getItem() != null) {
            final Player ply = event.getPlayer();
            if (event.getItem().getType() == Material.ENCHANTED_BOOK) {
                final String kitName = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());
                if (kitName.startsWith("Default")) {
                    if (plugin.getMatchManager().isInMatch(ply)) {
                        final Kit startingKit = this.plugin.getMatchManager().getGameType(ply).getStartingKit();
                        ply.sendMessage("Giving you " + ChatColor.YELLOW + "Default " + this.plugin.getMatchManager().getGameType(ply).getName() + " kit");
                        if (startingKit.getInv() != null && startingKit.getInv().getArmorContents() != null) {
                            ply.getInventory().setArmorContents(startingKit.getInv().getArmorContents());
                        }
                        if (startingKit.getInv() != null && startingKit.getInv().getContents() != null) {
                            ply.getInventory().setContents(startingKit.getInv().getContents());
                        }
                    } else if (plugin.getTeamManager().getTeam(ply) != null && plugin.getTeamManager().getTeam(ply).isInMatch()) {
                        Kit startingKit = plugin.getTeamManager().getTeam(ply).getTeamMatch().getGameType().getStartingKit();
                        ply.sendMessage("Giving you " + ChatColor.YELLOW + plugin.getTeamManager().getTeam(ply).getTeamMatch().getGameType().getName() + " kit");
                        if (startingKit.getInv() != null && startingKit.getInv().getArmorContents() != null) {
                            ply.getInventory().setArmorContents(startingKit.getInv().getArmorContents());
                        }
                        if (startingKit.getInv() != null && startingKit.getInv().getContents() != null) {
                            ply.getInventory().setContents(startingKit.getInv().getContents());
                        }
                    } else if (plugin.get2v2MatchManager().isInMatch(ply)) {
                        Kit startingKit = plugin.get2v2MatchManager().getMatch(ply).getGametype().getStartingKit();
                        ply.sendMessage("Giving you " + ChatColor.YELLOW + plugin.get2v2MatchManager().getMatch(ply).getGametype().getName() + " kit");
                        if (startingKit.getInv() != null && startingKit.getInv().getArmorContents() != null) {
                            ply.getInventory().setArmorContents(startingKit.getInv().getArmorContents());
                        }
                        if (startingKit.getInv() != null && startingKit.getInv().getContents() != null) {
                            ply.getInventory().setContents(startingKit.getInv().getContents());
                        }
                    }
                } else {
                    if (plugin.getMatchManager().isInMatch(ply)) {
                        final Kit kit = this.plugin.getPlayerDataManager().getKit(ply, this.plugin.getMatchManager().getGameType(ply), event.getPlayer().getInventory().getHeldItemSlot());
                        if (kit == null) {
                            Bukkit.broadcastMessage("uhhh");
                        }
                        ply.sendMessage("Giving you " + ChatColor.YELLOW + kit.getName() + " kit");
                        if (kit.getInv() != null && kit.getInv().getArmorContents() != null) {
                            ply.getInventory().setArmorContents(kit.getInv().getArmorContents());
                        }
                        if (kit.getInv() != null && kit.getInv().getContents() != null) {
                            ply.getInventory().setContents(kit.getInv().getContents());
                        }
                    } else {
                        if (plugin.getTeamManager().getTeam(ply).isInMatch()) {
                            final Kit kit = this.plugin.getPlayerDataManager().getKit(ply, this.plugin.getTeamManager().getTeam(ply).getTeamMatch().getGameType(), event.getPlayer().getInventory().getHeldItemSlot());
                            if (kit == null) {
                                Bukkit.broadcastMessage("uhhh");
                            }
                            ply.sendMessage("Giving you " + ChatColor.YELLOW + kit.getName() + " kit");
                            if (kit.getInv() != null && kit.getInv().getArmorContents() != null) {
                                ply.getInventory().setArmorContents(kit.getInv().getArmorContents());
                            }
                            if (kit.getInv() != null && kit.getInv().getContents() != null) {
                                ply.getInventory().setContents(kit.getInv().getContents());
                            }
                        } else {
                            if (plugin.get2v2MatchManager().isInMatch(ply)) {
                                final Kit kit = this.plugin.getPlayerDataManager().getKit(ply, this.plugin.get2v2MatchManager().getMatch(ply).getGametype(), event.getPlayer().getInventory().getHeldItemSlot());
                                if (kit == null) {
                                    Bukkit.broadcastMessage("uhhh");
                                }
                                ply.sendMessage("Giving you " + ChatColor.YELLOW + kit.getName() + " kit");
                                if (kit.getInv() != null && kit.getInv().getArmorContents() != null) {
                                    ply.getInventory().setArmorContents(kit.getInv().getArmorContents());
                                }
                                if (kit.getInv() != null && kit.getInv().getContents() != null) {
                                    ply.getInventory().setContents(kit.getInv().getContents());
                                }
                            }
                        }
                    }
                }
                ply.updateInventory();
            }
        }
    }

    public void storeInv(final Player ply, final boolean dead) {
        final Inventory inv = Bukkit.createInventory((InventoryHolder)null, 54, ply.getName());
        final PlayerInventory pinv = ply.getInventory();
        for (int i = 9; i <= 35; ++i) {
            inv.setItem(i - 9, pinv.getContents()[i]);
        }
        for (int i = 0; i <= 8; ++i) {
            inv.setItem(i + 27, pinv.getContents()[i]);
        }
        inv.setItem(36, pinv.getHelmet());
        inv.setItem(37, pinv.getChestplate());
        inv.setItem(38, pinv.getLeggings());
        inv.setItem(39, pinv.getBoots());
        if (dead) {
            inv.setItem(48, new ItemBuilder(Material.SKULL_ITEM, ChatColor.RED + "Played Died", "", 1).getItem());
        }
        else {
            inv.setItem(48, new ItemBuilder(Material.SPECKLED_MELON, ChatColor.RED + "Player health points", "", (int)ply.getHealth()).getItem());
        }
        inv.setItem(49, new ItemBuilder(Material.COOKED_BEEF, ChatColor.RED + "Player food points", "", ply.getFoodLevel()).getItem());
        final ItemStack potions = new ItemBuilder(Material.BREWING_STAND_ITEM, ChatColor.GOLD + "Potion Effects:", "", ply.getActivePotionEffects().size()).getItem();
        final ItemMeta imm = potions.getItemMeta();
        final List<String> lore = (List<String>)imm.getLore();
        for (PotionEffect pe : ply.getActivePotionEffects()) {
            lore.add(ChatColor.DARK_PURPLE + pe.getType().getName() + " " + (pe.getAmplifier() + 1) + " for " + MiscUtil.formatSeconds(pe.getDuration() / 20) + "!");
        }
        imm.setLore((List)lore);
        potions.setItemMeta(imm);
        potions.setAmount(ply.getActivePotionEffects().size());
        inv.setItem(50, potions);
        this.invs.put(ply.getName(), inv);
        new BukkitRunnable() {
            public void run() {
                InventoryManager.this.invs.remove(ply.getName());
            }
        }.runTaskLater((Plugin) this.plugin, 2400L);
    }

    public class InventoryCommand implements CommandExecutor {
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You must be a player to execute this command!");
                return false;
            }
            Player ply = (Player)sender;
            if (args.length == 1) {
                String plyName = args[0];
                if (invs.containsKey(plyName)) {
                    ply.openInventory(invs.get(plyName));
                    checkingInvs.add(ply);
                    return true;
                } else {
                    ply.sendMessage(ChatColor.RED + "That inventory no longer exists!");
                    return true;
                }
            } else {
                ply.sendMessage(ChatColor.RED + "/inventory [ply-name]");
                return true;
            }
        }
    }
}
