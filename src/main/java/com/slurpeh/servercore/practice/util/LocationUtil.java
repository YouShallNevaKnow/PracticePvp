package com.slurpeh.servercore.practice.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationUtil {
    public static String getString(final Location loc) {
        final StringBuilder builder = new StringBuilder();
        if (loc == null) {
            return null;
        }
        builder.append(loc.getBlockX()).append("|");
        builder.append(loc.getBlockY()).append("|");
        builder.append(loc.getBlockZ()).append("|");
        builder.append(loc.getWorld().getName()).append("|");
        builder.append(loc.getYaw()).append("|");
        builder.append(loc.getPitch());
        return builder.toString();
    }

    public static Location getLocation(final String s) {
        final String[] data = s.split("\\|");
        final int x = Integer.parseInt(data[0]);
        final int y = Integer.parseInt(data[1]);
        final int z = Integer.parseInt(data[2]);
        final World world = Bukkit.getWorld(data[3]);
        final Float yaw = Float.parseFloat(data[4]);
        final Float pitch = Float.parseFloat(data[5]);
        return new Location(world, (double)x, (double)y, (double)z, (float)yaw, (float)pitch);
    }
}
