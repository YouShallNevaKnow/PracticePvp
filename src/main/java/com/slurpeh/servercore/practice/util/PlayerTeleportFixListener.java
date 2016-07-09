package com.slurpeh.servercore.practice.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerTeleportFixListener implements Listener {
    static int visibility;
    static int min;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent e) {
        refreshPlayer(e.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent e) {
        refreshPlayer(e.getPlayer());
    }

    public static void refreshPlayer(Player p) {
        for (Player ply : Bukkit.getOnlinePlayers()) {
            p.showPlayer(ply);
            ply.showPlayer(p);
        }
    }
    static {
        PlayerTeleportFixListener.visibility = Bukkit.getServer().getViewDistance() * 16;
        PlayerTeleportFixListener.min = 4096;
    }
}
