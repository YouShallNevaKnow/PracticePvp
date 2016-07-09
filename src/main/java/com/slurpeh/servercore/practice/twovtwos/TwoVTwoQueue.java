package com.slurpeh.servercore.practice.twovtwos;

import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.gametype.GameType;
import com.slurpeh.servercore.practice.inventory.InventoryType;
import com.slurpeh.servercore.practice.team.Team;
import com.slurpeh.servercore.practice.util.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.material.Dye;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Bradley on 6/8/16.
 */
public class TwoVTwoQueue implements Listener {
    private GameType gt;
    private List<Team> queue;
    private HashMap<Team, Team> awaiting;
    //copy all classes in match package and also  unranked inventories and make sure to cancel inviting if they are in a queue

    public TwoVTwoQueue(GameType gt) {
        this.gt = gt;
        this.queue = new ArrayList<>();
        this.awaiting = new HashMap<>();
    }

    public void addToQueue(Team t) {
        if (t.getTeam().size() == 2) {
            this.queue.add(t);
            t.getTeam().forEach(ply -> ply.getInventory().clear());
            Dye dye = new Dye();
            dye.setColor(DyeColor.ORANGE);
            t.getLeader().getInventory().setItem(8, new ItemBuilder(Material.getMaterial(351), ChatColor.RED + "Right click to leave " + ChatColor.YELLOW + gt.getName() + " " + ChatColor.RED  + "queue", "", 1, (short)14).getItem());
            t.getLeader().sendMessage(ChatColor.YELLOW + "Added to the " + ChatColor.GREEN + gt.getName() + ChatColor.YELLOW + " queue, please wait for another team.");
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!queue.contains(KohiPractice.getInstance().getTeamManager().getTeam(t.getLeader()))) {
                        this.cancel();
                        return;
                    }
                    for (Team t2 : queue) {
                        if (t != t2 && t.getTeam().size() == 2 && t2.getTeam().size() == 2) {
                            this.cancel();
                            queue.remove(t);
                            queue.remove(t2);
                            awaiting.put(t, t2);
                        }
                    }
                }
            }.runTaskTimer(KohiPractice.getInstance(), 0l, 20l);
        } else {
            t.getLeader().sendMessage(ChatColor.RED + "You must have 2 players.");
        }
    }

    public boolean hasMatch() {
        return awaiting.size() > 0;
    }

    public void startMatch(Team t) {
        awaiting.remove(t);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (queue.contains(KohiPractice.getInstance().getTeamManager().getTeam(e.getPlayer())))  {
            queue.remove(KohiPractice.getInstance().getTeamManager().getTeam(e.getPlayer()));
        }
    }

    @EventHandler
    public void onLeave(PlayerInteractEvent e) {
        if (e.getItem() != null && e.getItem().hasItemMeta() && e.getItem().getItemMeta().getDisplayName() != null && ChatColor.stripColor(e.getItem().getItemMeta().getDisplayName()).equals("Right click to leave " + gt.getName() + " queue") && queue.contains(KohiPractice.getInstance().getTeamManager().getTeam(e.getPlayer()))) {
            queue.remove(KohiPractice.getInstance().getTeamManager().getTeam(e.getPlayer()));
            e.getPlayer().sendMessage(ChatColor.YELLOW + "Removed from the " + ChatColor.GREEN + gt.getName() + ChatColor.YELLOW + " queue.");
            KohiPractice.getInstance().getTeamManager().getTeam(e.getPlayer()).getTeam().stream().filter(ply -> ply.getUniqueId().toString().equalsIgnoreCase(KohiPractice.getInstance().getTeamManager().getTeam(e.getPlayer()).getLeader().getUniqueId().toString())).collect(Collectors.toList()).forEach(ply -> KohiPractice.getInstance().getInventorySetter().setupInventory(InventoryType.PARTY_MEMBER, ply));
            KohiPractice.getInstance().getInventorySetter().setupInventory(InventoryType.PARTY_LEADER, KohiPractice.getInstance().getTeamManager().getTeam(e.getPlayer()).getLeader());
        }
    }

    public boolean inQueue(Player ply) {
        for (Team t : queue) {
            return t.getTeam().contains(ply);
        }
        return false;
    }

    public boolean inQueue(Team t) {
        return queue.contains(t);
    }

    public GameType getGame() {
        return gt;
    }

    public List<Team> getQueue() {
        return queue;
    }

    public HashMap<Team, Team> getAwaiting() {
        return awaiting;
    }
}
