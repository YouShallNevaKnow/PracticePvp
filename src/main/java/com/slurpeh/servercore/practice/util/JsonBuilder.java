package com.slurpeh.servercore.practice.util;

import net.minecraft.server.v1_7_R4.ChatSerializer;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import java.util.Iterator;
import org.bukkit.ChatColor;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

public class JsonBuilder {
    private List<String> extras;

    public JsonBuilder(final String... text) {
        this.extras = new ArrayList<String>();
        for (final String extra : text) {
            this.parse(extra);
        }
    }

    public JsonBuilder parse(String text) {
        final String regex = "[&\ufffd]{1}([a-fA-Fl-oL-O0-9]){1}";
        text = text.replaceAll(regex, "\ufffd$1");
        if (!Pattern.compile(regex).matcher(text).find()) {
            this.withText(text);
            return this;
        }
        final String[] words = text.split(regex);
        int index = words[0].length();
        for (final String word : words) {
            try {
                if (index != words[0].length()) {
                    this.withText(word).withColor("\ufffd" + text.charAt(index - 1));
                }
            }
            catch (Exception ex) {}
            index += word.length() + 2;
        }
        return this;
    }

    public JsonBuilder withText(final String text) {
        this.extras.add("{text:\"" + text + "\"}");
        return this;
    }

    public JsonBuilder withColor(final ChatColor color) {
        final String c = color.name().toLowerCase();
        this.addSegment(color.isColor() ? ("color:" + c) : (c + ":true"));
        return this;
    }

    public JsonBuilder withColor(String color) {
        while (color.length() != 1) {
            color = color.substring(1).trim();
        }
        this.withColor(ChatColor.getByChar(color));
        return this;
    }

    public JsonBuilder withClickEvent(final ClickAction action, final String value) {
        this.addSegment("clickEvent:{action:" + action.toString().toLowerCase() + ",value:\"" + value + "\"}");
        return this;
    }

    public JsonBuilder withHoverEvent(final HoverAction action, final String value) {
        this.addSegment("hoverEvent:{action:" + action.toString().toLowerCase() + ",value:\"" + value + "\"}");
        return this;
    }

    private void addSegment(final String segment) {
        String lastText = this.extras.get(this.extras.size() - 1);
        lastText = lastText.substring(0, lastText.length() - 1) + "," + segment + "}";
        this.extras.remove(this.extras.size() - 1);
        this.extras.add(lastText);
    }

    @Override
    public String toString() {
        if (this.extras.size() <= 1) {
            return (this.extras.size() == 0) ? "{text:\"\"}" : this.extras.get(0);
        }
        String text = this.extras.get(0).substring(0, this.extras.get(0).length() - 1) + ",extra:[";
        this.extras.remove(0);
        for (final String extra : this.extras) {
            text = text + extra + ",";
        }
        text = text.substring(0, text.length() - 1) + "]}";
        return text;
    }

    public void sendJson(final Player p) {
       ((CraftPlayer)p).getHandle().playerConnection.sendPacket((Packet) new PacketPlayOutChat(ChatSerializer.a(this.toString()), true));
    }

    public enum ClickAction
    {
        RUN_COMMAND,
        SUGGEST_COMMAND,
        OPEN_URL;
    }

    public enum HoverAction
    {
        SHOW_TEXT;
    }
}
