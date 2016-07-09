package com.slurpeh.servercore.practice.arena;

import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.util.LocationUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
//import org.bukkit.craftbukkit.v1_7_R4.generator.InternalChunkGenerator;
import org.bukkit.entity.Player;

public class ArenaCommand implements CommandExecutor {
    ArenaManager am;
    KohiPractice plugin;
    public ArenaCommand(KohiPractice plugin) {
        this.plugin = plugin;
        this.am = plugin.getArenaManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("arena")) {
            if (sender instanceof Player) {
                Player ply = (Player)sender;
                if (ply.hasPermission("kohipractice.commands.arena")) {
                    if (args.length == 1) {
                        switch (args[0]) {
                            case "list": {
                                StringBuilder builder = new StringBuilder();
                                builder.append(ChatColor.GOLD).append(ChatColor.BOLD).append("Arenas: \n");
                                for (Arena a : am.getArenas()) {
                                    if (!a.isSetup() || LocationUtil.getString(a.getSpawn1()).equalsIgnoreCase(LocationUtil.getString(a.getSpawn2()))) {
                                        builder.append(ChatColor.BLUE + " - " + ChatColor.DARK_AQUA + "" + ChatColor.BOLD).append(a.getName()).append(ChatColor.RESET).append(ChatColor.BLUE).append(": ").append(ChatColor.DARK_AQUA).append("Not Setup (Either has the exact same spawnpoints or does not have 2 spawnpoints)");
                                    } else {
                                        builder.append(ChatColor.BLUE + " - " + ChatColor.DARK_AQUA + "" + ChatColor.BOLD).append(a.getName()).append(ChatColor.RESET).append(ChatColor.BLUE).append(": ").append(ChatColor.DARK_AQUA).append(LocationUtil.getString(a.getSpawn1())).append(" ").append(LocationUtil.getString(a.getSpawn2())).append("\n");
                                    }
                                }
                                ply.sendMessage(builder.toString());
                                return true;
                            }
                            default: {
                                ply.sendMessage(ChatColor.RED + "Invalid syntax! [/arena <list>, <create [name]>, <remove [name]>, <setspawn [1, 2] <name>>");
                                return true;
                            }
                        }
                    } else if (args.length == 2) {
                        switch (args[0]) {
                            case "create": {
                                String name = args[1];
                                if (am.doesArenaExist(name)) {
                                    ply.sendMessage(ChatColor.RED + "That arena already exists!");
                                    return true;
                                } else {
                                    am.createArena(name);
                                    ply.sendMessage(ChatColor.GREEN + "Arena successfully created!");
                                    return true;
                                }
                            }
                            case "remove": {
                                String name = args[1];
                                if (am.doesArenaExist(name)) {
                                    am.removeArena(name);
                                    ply.sendMessage(ChatColor.GREEN + "Arena successfully removed!");
                                    return true;
                                } else {
                                    ply.sendMessage(ChatColor.RED + "That arena doesn't exist!");
                                    return true;
                                }
                            }
                            default: {
                                ply.sendMessage(ChatColor.RED + "Invalid syntax! [/arena <list>, <create [name]>, <remove [name]>, <setspawn [1, 2] <name>>");
                                return true;
                            }
                        }
                    } else if (args.length == 3) {
                        switch (args[0]) {
                            case "setspawn": {
                                Integer i = Integer.parseInt(args[1]);
                                switch (i) {
                                    case 1: {
                                        String arena = args[2];
                                        if (am.doesArenaExist(arena)) {
                                            Arena a = am.getArena(arena);
                                            a.setSpawn1(ply.getLocation());
                                            am.saveArenas();
                                        } else {
                                            ply.sendMessage(ChatColor.RED + "That arena doesn't exist!");
                                            return true;
                                        }
                                    }
                                    case 2: {
                                        String arena = args[2];
                                        if (am.doesArenaExist(arena)) {
                                            Arena a = am.getArena(arena);
                                            a.setSpawn2(ply.getLocation());
                                            am.saveArenas();
                                        } else {
                                            ply.sendMessage(ChatColor.RED + "That arena doesn't exist!");
                                            return true;
                                        }
                                    }
                                    ply.sendMessage(ChatColor.GREEN + "Arena successfully modified!");
                                    am.saveArenas();
                                    return true;
                                    default: {
                                        ply.sendMessage(ChatColor.RED + "Enter a number from 1-2!");
                                        return true;
                                    }
                                }
                            }
                            default: {
                                ply.sendMessage(ChatColor.RED + "Invalid syntax! [/arena <list>, <create [name]>, <remove [name]>, <setspawn [1, 2] <name>>");
                                return true;
                            }
                        }
                    } else {
                        ply.sendMessage(ChatColor.RED + "Invalid syntax! [/arena <list>, <create [name]>, <remove [name]>, <setspawn [1, 2] <name>>");
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
        } else {
            return false;
        }
    }
}
