package com.slurpeh.servercore.practice.inventory;

import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.gametype.GameType;
import com.slurpeh.servercore.practice.player.Kit;
import com.slurpeh.servercore.practice.util.InventoryBuilder;
import com.slurpeh.servercore.practice.util.ItemBuilder;
import com.sun.istack.internal.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bradley on 5/4/16.
 */
public class KitInventorySetter {
   // incomplete
    private KohiPractice plugin;
    public KitInventorySetter(KohiPractice plugin) {
        this.plugin = plugin;
    }

    public void setupInventory(KitEditorInventoryType type, Player ply, @javax.annotation.Nullable GameType gt) {
        switch (type) {
            case EDITABLE_GAMETYPES: {
                InventoryBuilder builder = new InventoryBuilder(getKitEditorSize(), "Kit Editor", false, false);
                int i = 0;
                for (GameType gt1 : plugin.getGameTypeManager().getGameTypes()) {
                    if (gt1.isSetup() && gt1.isEditable()) {
                        builder.withItem(i, gt1.getDisplay());
                    } else {
                        continue;
                    }
                }
                ply.openInventory(builder.build());
                ply.updateInventory();
            }
            case KIT_LISTER: {
                final PlayerInventory inv = ply.getInventory();
                inv.setItem(0, new ItemBuilder(Material.ENCHANTED_BOOK, ChatColor.GOLD + "Default " + ChatColor.stripColor(gt.getDisplayName()) + " Kit", "", 1).getItem());
                int i = 2;
                if (this.plugin.getPlayerDataManager().getKits(ply, gt) == null) {
                    this.plugin.getPlayerDataManager().loadPlayerInfo(ply);
                }
                for (Kit kit : this.plugin.getPlayerDataManager().getKits(ply, gt)) {
                    if (kit != null) {
                        inv.setItem(i, new ItemBuilder(Material.ENCHANTED_BOOK, ChatColor.BLUE + kit.getName(), "", 1).getItem());
                        i++;
                    }
                }
                ply.updateInventory();
            }
        }
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
        for (int i = 9; i < editableGametypes.size(); i+= 9) {
            if (i >= editableGametypes.size()) {
                return i;
            }
        }
        return 54;
    }
}
