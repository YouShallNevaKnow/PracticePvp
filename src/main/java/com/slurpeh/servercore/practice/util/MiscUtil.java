package com.slurpeh.servercore.practice.util;

import java.util.*;

import org.bukkit.Material;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ItemStack;

public class MiscUtil {
    public static boolean isInt(final String s) {
        try {
            Integer.parseInt(s);
        }
        catch (NumberFormatException | NullPointerException ex) {
            return false;
        }
        return true;
    }

    public static String playerInventoryToString(final PlyInv inv) {
        if (inv.getArmorContents()[0] != null && inv.getArmorContents()[1] != null && inv.getArmorContents()[2] != null && inv.getArmorContents()[3] != null) {
            final StringBuilder builder = new StringBuilder();
            final ItemStack[] armor = inv.getArmorContents();
            builder.append(itemStackToString(armor[0])).append(";");
            builder.append(itemStackToString(armor[1])).append(";");
            builder.append(itemStackToString(armor[2])).append(";");
            builder.append(itemStackToString(armor[3]));
            builder.append("|");
            for (int i = 0; i < inv.getContents().length; ++i) {
                builder.append(i).append("#").append(itemStackToString(inv.getContents()[i])).append((i == inv.getContents().length - 1) ? "" : ";");
            }
            return builder.toString();
        } else {
            final StringBuilder builder = new StringBuilder();
            final ItemStack[] armor = inv.getArmorContents();
            builder.append(itemStackToString(new ItemStack(Material.AIR))).append(";");
            builder.append(itemStackToString(new ItemStack(Material.AIR))).append(";");
            builder.append(itemStackToString(new ItemStack(Material.AIR))).append(";");
            builder.append(itemStackToString(new ItemStack(Material.AIR)));
            builder.append("|");
            for (int i = 0; i < inv.getContents().length; ++i) {
                builder.append(i).append("#").append(itemStackToString(inv.getContents()[i])).append((i == inv.getContents().length - 1) ? "" : ";");
            }
            return builder.toString();
        }
    }

    public static String playerInventoryToString(final PlayerInventory inv) {
        final StringBuilder builder = new StringBuilder();
        final ItemStack[] armor = inv.getArmorContents();
        builder.append(itemStackToString(armor[0])).append(";");
        builder.append(itemStackToString(armor[1])).append(";");
        builder.append(itemStackToString(armor[2])).append(";");
        builder.append(itemStackToString(armor[3]));
        builder.append("|");
        for (int i = 0; i < inv.getContents().length; ++i) {
            builder.append(i).append("#").append(itemStackToString(inv.getContents()[i])).append((i == inv.getContents().length - 1) ? "" : ";");
        }
        return builder.toString();
    }

    public static PlyInv playerInventoryFromString(final String in) {
        final PlyInv inv = new PlyInv();
        final String[] data = in.split("\\|");
        final ItemStack[] armor = new ItemStack[data[0].split(";").length];
        for (int i = 0; i < data[0].split(";").length; ++i) {
            armor[i] = itemStackFromString(data[0].split(";")[i]);
        }
        inv.setArmorContents(armor);
        final ItemStack[] contents = new ItemStack[data[1].split(";").length];
        for (final String s : data[1].split(";")) {
            final int slot = Integer.parseInt(s.split("#")[0]);
            if (s.split("#").length == 1) {
                contents[slot] = null;
            }
            else {
                contents[slot] = itemStackFromString(s.split("#")[1]);
            }
        }
        inv.setContents(contents);
        return inv;
    }

    public static String itemStackToString(final ItemStack item) {
        final StringBuilder builder = new StringBuilder();
        if (item != null) {
            final String isType = String.valueOf(item.getType().getId());
            builder.append("t@").append(isType);
            if (item.getDurability() != 0) {
                final String isDurability = String.valueOf(item.getDurability());
                builder.append(":d@").append(isDurability);
            }
            if (item.getAmount() != 1) {
                final String isAmount = String.valueOf(item.getAmount());
                builder.append(":a@").append(isAmount);
            }
            final Map<Enchantment, Integer> isEnch = (Map<Enchantment, Integer>)item.getEnchantments();
            if (isEnch.size() > 0) {
                for (final Map.Entry<Enchantment, Integer> ench : isEnch.entrySet()) {
                    builder.append(":e@").append(ench.getKey().getId()).append("@").append(ench.getValue());
                }
            }
            if (item.hasItemMeta()) {
                final ItemMeta imeta = item.getItemMeta();
                if (imeta.hasDisplayName()) {
                    builder.append(":dn@").append(imeta.getDisplayName());
                }
                if (imeta.hasLore()) {
                    builder.append(":l@").append(imeta.getLore());
                }
            }
        }
        return builder.toString();
    }

    public static String inventoryToString(final Inventory inv) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < inv.getContents().length; ++i) {
            builder.append(i).append("#").append(itemStackToString(inv.getContents()[i]));
            if (i != inv.getContents().length - 1) {
                builder.append(";");
            }
        }
        return builder.toString();
    }

    public static Inventory inventoryFromString(final String in) {
        final Inventory inv = Bukkit.createInventory((InventoryHolder)null, 54);
        final String[] split;
        final String[] data = split = in.split(";");
        for (final String s : split) {
            final String[] info = s.split("#");
            inv.setItem(Integer.parseInt(info[0]), (info.length > 1) ? itemStackFromString(info[1]) : null);
        }
        return inv;
    }

    public static ItemStack itemStackFromString(final String in) {
        ItemStack item = null;
        ItemMeta meta = null;
        final String[] split;
        final String[] data = split = in.split(":");
        for (final String itemInfo : split) {
            final String[] itemAttribute = itemInfo.split("@");
            final String s2 = itemAttribute[0];
            switch (s2) {
                case "t": {
                    item = new ItemStack(Material.getMaterial((int)Integer.valueOf(itemAttribute[1])));
                    meta = item.getItemMeta();
                    break;
                }
                case "d": {
                    if (item != null) {
                        item.setDurability((short)Short.valueOf(itemAttribute[1]));
                        break;
                    }
                    break;
                }
                case "a": {
                    if (item != null) {
                        item.setAmount((int)Integer.valueOf(itemAttribute[1]));
                        break;
                    }
                    break;
                }
                case "e": {
                    if (item != null) {
                        item.addEnchantment(Enchantment.getById((int)Integer.valueOf(itemAttribute[1])), (int)Integer.valueOf(itemAttribute[2]));
                        break;
                    }
                    break;
                }
                case "dn": {
                    if (meta != null) {
                        meta.setDisplayName(itemAttribute[1]);
                        break;
                    }
                    break;
                }
                case "l": {
                    itemAttribute[1] = itemAttribute[1].replace("[", "");
                    itemAttribute[1] = itemAttribute[1].replace("]", "");
                    final List<String> lore = Arrays.asList(itemAttribute[1].split(","));
                    for (int x = 0; x < lore.size(); ++x) {
                        String s = lore.get(x);
                        if (s != null) {
                            if (s.toCharArray().length != 0) {
                                if (s.charAt(0) == ' ') {
                                    s = s.replaceFirst(" ", "");
                                }
                                lore.set(x, s);
                            }
                        }
                    }
                    if (meta != null) {
                        meta.setLore((List)lore);
                        break;
                    }
                    break;
                }
            }
        }
        if (meta != null && (meta.hasDisplayName() || meta.hasLore())) {
            item.setItemMeta(meta);
        }
        return item;
    }

    public static String formatSeconds(int seconds) {
        final int minutes = seconds / 60;
        if (minutes == 0) {
            return seconds + " seconds";
        }
        seconds %= 60;
        return minutes + " minutes and " + seconds + " seconds";
    }

    public static List<Object> toList(Object[] array) {
        List<Object> objects = new ArrayList<>();
        for (Object o : array) {
            objects.add(o);
        }
        return objects;
    }

    public static Object[] toArray(List<Object> list) {
        Object[] objects = new Object[list.size() - 1];
        for (Object o : list) {
            for (int i = 0; i < objects.length; i++) {
                objects[i] = o;
            }
        }
        return objects;
    }

    public static Object getObject(List<Object> objects, int toGet) {
        return objects.get(toGet);
    }

    public static Object getObject(Object[] objects, int toGet) {
        return objects[toGet];
    }
}
