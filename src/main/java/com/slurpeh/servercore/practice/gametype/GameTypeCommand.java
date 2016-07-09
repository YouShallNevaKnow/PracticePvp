package com.slurpeh.servercore.practice.gametype;

import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.player.Kit;
import com.slurpeh.servercore.practice.util.InventoryBuilder;
import com.slurpeh.servercore.practice.util.PlyInv;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Bradley on 5/15/16.
 */
public class GameTypeCommand implements CommandExecutor {
    private KohiPractice plugin;
    private GameTypeManager gtm;

    public GameTypeCommand(KohiPractice plugin) {
        this.plugin = plugin;
        this.gtm = plugin.getGameTypeManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (! (sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to run this command!");
            return false;
        }
        Player ply = (Player) sender;
        if (! ply.hasPermission("kohipractice.commands.gametype")) {
            ply.sendMessage(ChatColor.RED + "You do not have permission to execute this command!");
            return true;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                StringBuilder builder = new StringBuilder();
                builder.append(ChatColor.GOLD).append(ChatColor.BOLD).append("GameTypes: \n");
                for (GameType gt : this.gtm.getGameTypes()) {
                    builder.append(ChatColor.BLUE).append(" - ").append(ChatColor.DARK_AQUA).append(ChatColor.BOLD).append(gt.getName()).append(ChatColor.RESET).append(ChatColor.BLUE).append("\n");
                }
                ply.sendMessage(builder.toString());
                return true;
            } else {
                ply.sendMessage(ChatColor.RED + "Invalid syntax! /gametype <list>, /gametype <setbuildandbreak, create, remove, seteditable, setdisplay, setinventory, loadinventory, editinv> [name]");
                return true;
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create")) {
                String gt = args[1];
                if (gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType already exists!");
                    return true;
                } else {
                    gtm.createGameType(gt);
                    ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully created!");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                String gt = args[1];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.removeGameType(gt);
                    ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully deleted!");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("seteditable")) {
                String gt = args[1];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.getGameType(gt).setEditable(! gtm.getGameType(gt).isEditable());
                    if (gtm.getGameType(gt).isEditable()) {
                        ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' is now editable!");
                        this.gtm.saveGameTypes();
                        return true;
                    } else {
                        ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' is now not editable!");
                        this.gtm.saveGameTypes();
                        return true;
                    }
                }
            } else if (args[0].equalsIgnoreCase("setbuildandbreak")) {
                String gt = args[1];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.getGameType(gt).setPlaceAndBreak(!gtm.getGameType(gt).canPlaceAndBreak());
                    if (gtm.getGameType(gt).canPlaceAndBreak()) {
                        ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully modified!");
                        this.gtm.saveGameTypes();
                        return true;
                    } else {
                        ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully modified!");
                        this.gtm.saveGameTypes();
                        return true;
                    }
                }
            } else if (args[0].equalsIgnoreCase("setdisplay")) {
                String gt = args[1];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.getGameType(gt).setDisplay(ply.getItemInHand());
                    gtm.getGameType(gt).setDisplayName(ply.getItemInHand().getItemMeta().getDisplayName());
                    ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully modified!");
                    this.gtm.saveGameTypes();
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("setinventory")) {
                String gt = args[1];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.getGameType(gt).setStartingKit(new Kit(gtm.getGameType(gt).getDisplayName(), PlyInv.fromPlayerInventory(ply.getInventory())));
                    ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully modified!");
                    gtm.saveGameTypes();
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("loadinventory")) {
                String gt = args[1];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    ply.getInventory().setContents(gtm.getGameType(gt).getStartingKit().getInv().getContents());
                    ply.getInventory().setArmorContents(gtm.getGameType(gt).getStartingKit().getInv().getArmorContents());
                    ply.updateInventory();
                    ply.sendMessage(ChatColor.GREEN + "Loaded default kit for GameType '" + gt + "'!");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("editinv")) {
                String gt = args[1];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    if (gtm.getGameType(gt).isEditable()) {
                        ply.openInventory(gtm.getGameType(gt).getPossibleGear());
                        this.gtm.addEditing(ply);
                        ply.sendMessage(ChatColor.GREEN + "Editing edit inventory for GameType '" + gt + "'!");
                        return true;
                    } else {
                        ply.sendMessage(ChatColor.RED + "GameType '" + gt + "' is not editable!");
                        return true;
                    }
                }
            } else {
                ply.sendMessage(ChatColor.RED + "Invalid syntax! /gametype <list>, /gametype <setbuildandbreak, create, remove, seteditable, setdisplay, setinventory, loadinventory, editinv> [name]");
                return true;
             }
        }
        else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("create")) {
                String gt = args[1] + " " + args[2];
                if (gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType already exists!");
                    return true;
                } else {
                    gtm.createGameType(gt);
                    ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully created!");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                String gt = args[1] + " " + args[2];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.removeGameType(gt);
                    ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully deleted!");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("seteditable")) {
                String gt = args[1] + " " + args[2];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.getGameType(gt).setEditable(! gtm.getGameType(gt).isEditable());
                    if (gtm.getGameType(gt).isEditable()) {
                        ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' is now editable!");
                        this.gtm.saveGameTypes();
                        return true;
                    } else {
                        ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' is now not editable!");
                        this.gtm.saveGameTypes();
                        return true;
                    }
                }
            } else if (args[0].equalsIgnoreCase("setbuildandbreak")) {
                String gt = args[1] + " " + args[2];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.getGameType(gt).setPlaceAndBreak(!gtm.getGameType(gt).canPlaceAndBreak());
                    if (gtm.getGameType(gt).canPlaceAndBreak()) {
                        ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully modified!");
                        this.gtm.saveGameTypes();
                        return true;
                    } else {
                        ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully modified!");
                        this.gtm.saveGameTypes();
                        return true;
                    }
                }
            } else if (args[0].equalsIgnoreCase("setdisplay")) {
                String gt = args[1] + " " + args[2];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.getGameType(gt).setDisplay(ply.getItemInHand());
                    gtm.getGameType(gt).setDisplayName(ply.getItemInHand().getItemMeta().getDisplayName());
                    ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully modified!");
                    this.gtm.saveGameTypes();
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("setinventory")) {
                String gt = args[1] + " " + args[2];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.getGameType(gt).setStartingKit(new Kit(gtm.getGameType(gt).getDisplayName(), PlyInv.fromPlayerInventory(ply.getInventory())));
                    ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully modified!");
                    gtm.saveGameTypes();
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("loadinventory")) {
                String gt = args[1] + " " + args[2];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    ply.getInventory().setContents(gtm.getGameType(gt).getStartingKit().getInv().getContents());
                    ply.getInventory().setArmorContents(gtm.getGameType(gt).getStartingKit().getInv().getArmorContents());
                    ply.updateInventory();
                    ply.sendMessage(ChatColor.GREEN + "Loaded default kit for GameType '" + gt + "'!");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("editinv")) {
                String gt = args[1] + " " + args[2];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    if (gtm.getGameType(gt).isEditable()) {
                        InventoryBuilder builder = new InventoryBuilder(54, gtm.getGameType(gt).getName(), false, false);
                        int i = 0;
                        for (ItemStack is : gtm.getGameType(gt).getPossibleGear().getContents()) {
                            builder.withItem(i, is);
                            i++;
                        }
                        ply.openInventory(builder.build());
                        ply.updateInventory();
                        this.gtm.addEditing(ply);
                        ply.sendMessage(ChatColor.GREEN + "Editing edit inventory for GameType '" + gt + "'!");
                        return true;
                    } else {
                        ply.sendMessage(ChatColor.RED + "GameType '" + gt + "' is not editable!");
                        return true;
                    }
                }
            } else {
                ply.sendMessage(ChatColor.RED + "Invalid syntax! /gametype <list>, /gametype <setbuildandbreak, create, remove, seteditable, setdisplay, setinventory, loadinventory, editinv> [name]");
                return true;
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("create")) {
                String gt = args[1] + " " + args[2] + " " + args[3];
                if (gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType already exists!");
                    return true;
                } else {
                    gtm.createGameType(gt);
                    ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully created!");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                String gt = args[1] + " " + args[2] + " " + args[3];;
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.removeGameType(gt);
                    ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully deleted!");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("seteditable")) {
                String gt = args[1] + " " + args[2] + " " + args[3];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.getGameType(gt).setEditable(! gtm.getGameType(gt).isEditable());
                    if (gtm.getGameType(gt).isEditable()) {
                        ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' is now editable!");
                        this.gtm.saveGameTypes();
                        return true;
                    } else {
                        ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' is now not editable!");
                        this.gtm.saveGameTypes();
                        return true;
                    }
                }
            } else if (args[0].equalsIgnoreCase("setbuildandbreak")) {
                String gt = args[1] + " " + args[2] + " " + args[3];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.getGameType(gt).setPlaceAndBreak(!gtm.getGameType(gt).canPlaceAndBreak());
                    if (gtm.getGameType(gt).canPlaceAndBreak()) {
                        ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully modified!");
                        this.gtm.saveGameTypes();
                        return true;
                    } else {
                        ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully modified!");
                        this.gtm.saveGameTypes();
                        return true;
                    }
                }
            } else if (args[0].equalsIgnoreCase("setdisplay")) {
                String gt = args[1] + " " + args[2] + " " + args[3];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.getGameType(gt).setDisplay(ply.getItemInHand());
                    gtm.getGameType(gt).setDisplayName(ply.getItemInHand().getItemMeta().getDisplayName());
                    ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully modified!");
                    this.gtm.saveGameTypes();
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("setinventory")) {
                String gt = args[1] + " " + args[2] + " " + args[3];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.getGameType(gt).setStartingKit(new Kit(gtm.getGameType(gt).getDisplayName(), PlyInv.fromPlayerInventory(ply.getInventory())));
                    ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully modified!");
                    gtm.saveGameTypes();
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("loadinventory")) {
                String gt = args[1] + " " + args[2] + " " + args[3];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    ply.getInventory().setContents(gtm.getGameType(gt).getStartingKit().getInv().getContents());
                    ply.getInventory().setArmorContents(gtm.getGameType(gt).getStartingKit().getInv().getArmorContents());
                    ply.updateInventory();
                    ply.sendMessage(ChatColor.GREEN + "Loaded default kit for GameType '" + gt + "'!");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("editinv")) {
                String gt = args[1] + " " + args[2] + " " + args[3];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    if (gtm.getGameType(gt).isEditable()) {
                        ply.openInventory(gtm.getGameType(gt).getPossibleGear());
                        this.gtm.addEditing(ply);
                        ply.sendMessage(ChatColor.GREEN + "Editing edit inventory for GameType '" + gt + "'!");
                        return true;
                    } else {
                        ply.sendMessage(ChatColor.RED + "GameType '" + gt + "' is not editable!");
                        return true;
                    }
                }
            } else {
                ply.sendMessage(ChatColor.RED + "Invalid syntax! /gametype <list>, /gametype <setbuildandbreak, create, remove, seteditable, setdisplay, setinventory, loadinventory, editinv> [name]");
                return true;
            }
        } else if (args.length == 5) {
            if (args[0].equalsIgnoreCase("create")) {
                String gt = args[1] + " " + args[2] + " " + args[3] + " " + args[4];
                if (gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType already exists!");
                    return true;
                } else {
                    gtm.createGameType(gt);
                    ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully created!");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                String gt = args[1] + " " + args[2] + " " + args[3] + " " + args[4];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.removeGameType(gt);
                    ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully deleted!");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("seteditable")) {
                String gt = args[1] + " " + args[2] + " " + args[3] + " " + args[4];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.getGameType(gt).setEditable(! gtm.getGameType(gt).isEditable());
                    if (gtm.getGameType(gt).isEditable()) {
                        ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' is now editable!");
                        this.gtm.saveGameTypes();
                        return true;
                    } else {
                        ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' is now not editable!");
                        this.gtm.saveGameTypes();
                        return true;
                    }
                }
            } else if (args[0].equalsIgnoreCase("setbuildandbreak")) {
                String gt = args[1] + " " + args[2] + " " + args[3] + " " + args[4];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.getGameType(gt).setPlaceAndBreak(!gtm.getGameType(gt).canPlaceAndBreak());
                    if (gtm.getGameType(gt).canPlaceAndBreak()) {
                        ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully modified!");
                        this.gtm.saveGameTypes();
                        return true;
                    } else {
                        ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully modified!");
                        this.gtm.saveGameTypes();
                        return true;
                    }
                }
            } else if (args[0].equalsIgnoreCase("setdisplay")) {
                String gt = args[1] + " " + args[2] + " " + args[3] + " " + args[4];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.getGameType(gt).setDisplay(ply.getItemInHand());
                    gtm.getGameType(gt).setDisplayName(ply.getItemInHand().getItemMeta().getDisplayName());
                    ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully modified!");
                    this.gtm.saveGameTypes();
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("setinventory")) {
                String gt = args[1] + " " + args[2] + " " + args[3] + " " + args[4];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.getGameType(gt).setStartingKit(new Kit(gtm.getGameType(gt).getDisplayName(), PlyInv.fromPlayerInventory(ply.getInventory())));
                    ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully modified!");
                    gtm.saveGameTypes();
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("loadinventory")) {
                String gt = args[1] + " " + args[2] + " " + args[3] + " " + args[4];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    ply.getInventory().setContents(gtm.getGameType(gt).getStartingKit().getInv().getContents());
                    ply.getInventory().setArmorContents(gtm.getGameType(gt).getStartingKit().getInv().getArmorContents());
                    ply.updateInventory();
                    ply.sendMessage(ChatColor.GREEN + "Loaded default kit for GameType '" + gt + "'!");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("editinv")) {
                String gt = args[1] + " " + args[2] + " " + args[3] + " " + args[4];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    if (gtm.getGameType(gt).isEditable()) {
                        ply.openInventory(gtm.getGameType(gt).getPossibleGear());
                        this.gtm.addEditing(ply);
                        ply.sendMessage(ChatColor.GREEN + "Editing edit inventory for GameType '" + gt + "'!");
                        return true;
                    } else {
                        ply.sendMessage(ChatColor.RED + "GameType '" + gt + "' is not editable!");
                        return true;
                    }
                }
            } else {
                ply.sendMessage(ChatColor.RED + "Invalid syntax! /gametype <list>, /gametype <setbuildandbreak, create, remove, seteditable, setdisplay, setinventory, loadinventory, editinv> [name]");
                return true;
            }
        } else if (args.length == 6) {
            if (args[0].equalsIgnoreCase("create")) {
                String gt = args[1] + " " + args[2] + " " + args[3] + " " + args[4] + " " + args[5];
                if (gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType already exists!");
                    return true;
                } else {
                    gtm.createGameType(gt);
                    ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully created!");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                String gt = args[1] + " " + args[2] + " " + args[3] + " " + args[4] + " " + args[5];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.removeGameType(gt);
                    ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully deleted!");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("seteditable")) {
                String gt = args[1] + " " + args[2] + " " + args[3] + " " + args[4] + " " + args[5];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.getGameType(gt).setEditable(! gtm.getGameType(gt).isEditable());
                    if (gtm.getGameType(gt).isEditable()) {
                        ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' is now editable!");
                        this.gtm.saveGameTypes();
                        return true;
                    } else {
                        ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' is now not editable!");
                        this.gtm.saveGameTypes();
                        return true;
                    }
                }
            } else if (args[0].equalsIgnoreCase("setbuildandbreak")) {
                String gt = args[1] + " " + args[2] + " " + args[3] + " " + args[4] + " " + args[5];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.getGameType(gt).setPlaceAndBreak(!gtm.getGameType(gt).canPlaceAndBreak());
                    if (gtm.getGameType(gt).canPlaceAndBreak()) {
                        ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully modified!");
                        this.gtm.saveGameTypes();
                        return true;
                    } else {
                        ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully modified!");
                        this.gtm.saveGameTypes();
                        return true;
                    }
                }
            } else if (args[0].equalsIgnoreCase("setdisplay")) {
                String gt = args[1] + " " + args[2] + " " + args[3] + " " + args[4] + " " + args[5];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.getGameType(gt).setDisplay(ply.getItemInHand());
                    gtm.getGameType(gt).setDisplayName(ply.getItemInHand().getItemMeta().getDisplayName());
                    ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully modified!");
                    this.gtm.saveGameTypes();
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("setinventory")) {
                String gt = args[1] + " " + args[2] + " " + args[3] + " " + args[4] + " " + args[5];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    gtm.getGameType(gt).setStartingKit(new Kit(gtm.getGameType(gt).getDisplayName(), PlyInv.fromPlayerInventory(ply.getInventory())));
                    ply.sendMessage(ChatColor.GREEN + "GameType '" + gt + "' successfully modified!");
                    gtm.saveGameTypes();
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("loadinventory")) {
                String gt = args[1] + " " + args[2] + " " + args[3] + " " + args[4] + " " + args[5];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    ply.getInventory().setContents(gtm.getGameType(gt).getStartingKit().getInv().getContents());
                    ply.getInventory().setArmorContents(gtm.getGameType(gt).getStartingKit().getInv().getArmorContents());
                    ply.updateInventory();
                    ply.sendMessage(ChatColor.GREEN + "Loaded default kit for GameType '" + gt + "'!");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("editinv")) {
                String gt = args[1] + " " + args[2] + " " + args[3] + " " + args[4] + " " + args[5];
                if (! gtm.doesGameTypeExist(gt)) {
                    ply.sendMessage(ChatColor.RED + "That GameType doesn't exist!");
                    return true;
                } else {
                    if (gtm.getGameType(gt).isEditable()) {
                        ply.openInventory(gtm.getGameType(gt).getPossibleGear());
                        this.gtm.addEditing(ply);
                        ply.sendMessage(ChatColor.GREEN + "Editing edit inventory for GameType '" + gt + "'!");
                        return true;
                    } else {
                        ply.sendMessage(ChatColor.RED + "GameType '" + gt + "' is not editable!");
                        return true;
                    }
                }
            } else {
                ply.sendMessage(ChatColor.RED + "Invalid syntax! /gametype <list>, /gametype <setbuildandbreak, create, remove, seteditable, setdisplay, setinventory, loadinventory, editinv> [name]");
                return true;
            }
        } else {
            ply.sendMessage(ChatColor.RED + "Invalid syntax! /gametype <list>, /gametype <setbuildandbreak, create, remove, seteditable, setdisplay, setinventory, loadinventory, editinv> [name]");
            return true;
        }
    }
}
