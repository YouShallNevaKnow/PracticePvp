package com.slurpeh.servercore.practice.util;

import org.bukkit.event.HandlerList;
import java.lang.reflect.InvocationTargetException;
import com.comphenix.protocol.events.PacketContainer;
import java.util.List;
import java.util.Arrays;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.ProtocolLibrary;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import org.bukkit.plugin.Plugin;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.PacketType;
import com.google.common.collect.Table;
import org.bukkit.event.Listener;

public class EntityHider implements Listener {
    protected Table<Integer, Integer, Boolean> observerEntityMap;
    private static final PacketType[] ENTITY_PACKETS;
    private ProtocolManager manager;
    private Listener bukkitListener;
    private PacketAdapter protocolListener;
    protected final Policy policy;

    public EntityHider(final Plugin plugin, final Policy policy) {
        this.observerEntityMap = HashBasedTable.create();
        Preconditions.checkNotNull((Object)plugin, (Object)"plugin cannot be NULL.");
        this.policy = policy;
        this.manager = ProtocolLibrary.getProtocolManager();
        plugin.getServer().getPluginManager().registerEvents(this.bukkitListener = this.constructBukkit(), plugin);
        this.manager.addPacketListener((PacketListener)(this.protocolListener = this.constructProtocol(plugin)));
    }

    protected boolean setVisibility(final Player observer, final int entityID, final boolean visible) {
        switch (this.policy) {
            case BLACKLIST: {
                return !this.setMembership(observer, entityID, !visible);
            }
            case WHITELIST: {
                return this.setMembership(observer, entityID, visible);
            }
            default: {
                throw new IllegalArgumentException("Unknown policy: " + this.policy);
            }
        }
    }

    protected boolean setMembership(final Player observer, final int entityID, final boolean member) {
        if (member) {
            return this.observerEntityMap.put(observer.getEntityId(), entityID, true) != null;
        }
        return this.observerEntityMap.remove((Object)observer.getEntityId(), (Object)entityID) != null;
    }

    protected boolean getMembership(final Player observer, final int entityID) {
        return this.observerEntityMap.contains((Object)observer.getEntityId(), (Object)entityID);
    }

    protected boolean isVisible(final Player observer, final int entityID) {
        final boolean presence = this.getMembership(observer, entityID);
        return (this.policy == Policy.WHITELIST) ? presence : (!presence);
    }

    protected void removeEntity(final Entity entity, final boolean destroyed) {
        final int entityID = entity.getEntityId();
        for (final Map<Integer, Boolean> maps : this.observerEntityMap.rowMap().values()) {
            maps.remove(entityID);
        }
    }

    protected void removePlayer(final Player player) {
        this.observerEntityMap.rowMap().remove(player.getEntityId());
    }

    private Listener constructBukkit() {
        return (Listener)new Listener() {
            @EventHandler
            public void onEntityDeath(final EntityDeathEvent e) {
                EntityHider.this.removeEntity((Entity)e.getEntity(), true);
            }

            @EventHandler
            public void onChunkUnload(final ChunkUnloadEvent e) {
                for (final Entity entity : e.getChunk().getEntities()) {
                    EntityHider.this.removeEntity(entity, false);
                }
            }

            @EventHandler
            public void onPlayerQuit(final PlayerQuitEvent e) {
                EntityHider.this.removePlayer(e.getPlayer());
            }
        };
    }

    private PacketAdapter constructProtocol(final Plugin plugin) {
        return new PacketAdapter(plugin, EntityHider.ENTITY_PACKETS) {
            public void onPacketSending(final PacketEvent event) {
                final int entityID = (int)event.getPacket().getIntegers().read(0);
                if (!EntityHider.this.isVisible(event.getPlayer(), entityID)) {
                    event.setCancelled(true);
                }
            }
        };
    }

    public final boolean toggleEntity(final Player observer, final Entity entity) {
        if (this.isVisible(observer, entity.getEntityId())) {
            return this.hideEntity(observer, entity);
        }
        return !this.showEntity(observer, entity);
    }

    public final boolean showEntity(final Player observer, final Entity entity) {
        this.validate(observer, entity);
        final boolean hiddenBefore = !this.setVisibility(observer, entity.getEntityId(), true);
        if (this.manager != null && hiddenBefore) {
            this.manager.updateEntity(entity, (List)Arrays.asList(observer));
        }
        return hiddenBefore;
    }

    public final boolean hideEntity(final Player observer, final Entity entity) {
        this.validate(observer, entity);
        final boolean visibleBefore = this.setVisibility(observer, entity.getEntityId(), false);
        if (visibleBefore) {
            final PacketContainer destroyEntity = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
            destroyEntity.getIntegerArrays().write(0, new int[] { entity.getEntityId() });
            try {
                this.manager.sendServerPacket(observer, destroyEntity);
            }
            catch (InvocationTargetException e) {
                throw new RuntimeException("Cannot send server packet.", e);
            }
        }
        return visibleBefore;
    }

    public final boolean canSee(final Player observer, final Entity entity) {
        this.validate(observer, entity);
        return this.isVisible(observer, entity.getEntityId());
    }

    private void validate(final Player observer, final Entity entity) {
        Preconditions.checkNotNull((Object)observer, (Object)"observer cannot be NULL.");
        Preconditions.checkNotNull((Object)entity, (Object)"entity cannot be NULL.");
    }

    public Policy getPolicy() {
        return this.policy;
    }

    public void close() {
        if (this.manager != null) {
            HandlerList.unregisterAll(this.bukkitListener);
            this.manager.removePacketListener((PacketListener)this.protocolListener);
            this.manager = null;
        }
    }

    static {
        ENTITY_PACKETS = new PacketType[] { PacketType.Play.Server.ENTITY_EQUIPMENT, PacketType.Play.Server.BED, PacketType.Play.Server.ANIMATION, PacketType.Play.Server.NAMED_ENTITY_SPAWN, PacketType.Play.Server.COLLECT, PacketType.Play.Server.SPAWN_ENTITY, PacketType.Play.Server.SPAWN_ENTITY_LIVING, PacketType.Play.Server.SPAWN_ENTITY_PAINTING, PacketType.Play.Server.SPAWN_ENTITY_EXPERIENCE_ORB, PacketType.Play.Server.ENTITY_VELOCITY, PacketType.Play.Server.REL_ENTITY_MOVE, PacketType.Play.Server.ENTITY_LOOK, PacketType.Play.Server.ENTITY_MOVE_LOOK, PacketType.Play.Server.ENTITY_MOVE_LOOK, PacketType.Play.Server.ENTITY_TELEPORT, PacketType.Play.Server.ENTITY_HEAD_ROTATION, PacketType.Play.Server.ENTITY_STATUS, PacketType.Play.Server.ATTACH_ENTITY, PacketType.Play.Server.ENTITY_METADATA, PacketType.Play.Server.ENTITY_EFFECT, PacketType.Play.Server.REMOVE_ENTITY_EFFECT, PacketType.Play.Server.BLOCK_BREAK_ANIMATION };
    }

    public enum Policy
    {
        WHITELIST,
        BLACKLIST;
    }
}
