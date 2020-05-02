package com.pro_crafting.mc.worldfuscator.engine;

import com.pro_crafting.mc.worldfuscator.engine.ChunkPacketProcessor.ChunkletProcessor;
import com.pro_crafting.mc.worldfuscator.packetwrapper.WrapperPlayServerBlockChange;
import com.pro_crafting.mc.worldfuscator.packetwrapper.WrapperPlayServerMultiBlockChange;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.pro_crafting.mc.worldfuscator.WorldFuscator;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Simple class that can be used to alter the apperance of a number of blocks.
 *
 * @author Kristian
 */
public class BlockDisguiser {

    private final MapPacketChunkletProcessor mapPacketChunkletProcessor;
    // The current listener
    private PacketAdapter listener;
    private WorldFuscator plugin;

    /**
     * Construct a new block changer.
     *
     * @param parent - the owner Plugin.
     */
    public BlockDisguiser(WorldFuscator parent) {
        this.plugin = parent;
        this.mapPacketChunkletProcessor = new MapPacketChunkletProcessor(this.plugin.getTranslator());
        registerListener(parent);
        ChunkPacketProcessor.dataFolder = parent.getDataFolder();
        ChunkPacketProcessor.isDebugEnabled = parent.getConfiguration().isDebugEnabled();
    }

    private void registerListener(Plugin plugin) {
        final ChunkletProcessor processor = new MapPacketChunkletProcessor(this.plugin.getTranslator());

        ProtocolLibrary.getProtocolManager().addPacketListener(
                listener = new PacketAdapter(plugin, ListenerPriority.HIGHEST,
                        Server.BLOCK_CHANGE, Server.MULTI_BLOCK_CHANGE, Server.MAP_CHUNK) {

                    public void onPacketSending(PacketEvent event) {
                        Player player = event.getPlayer();

                        if (player.hasPermission("worldfuscator.bypass")) {
                            return;
                        }

                        PacketContainer packet = event.getPacket();

                        World world = player.getWorld();
                        if (event.getPacketType() == Server.BLOCK_CHANGE) {
                            packet = packet.shallowClone();
                            translateBlockChange(packet, world, player);
                        } else if (event.getPacketType() == Server.MULTI_BLOCK_CHANGE) {
                            packet = packet.shallowClone();
                            translateMultiBlockChange(packet, world, player);
                        } else if (event.getPacketType() == Server.MAP_CHUNK) {
                            packet = packet.shallowClone();
                            ChunkPacketProcessor.fromMapPacket(packet, world).process(processor, player, packet);
                        }

                        event.setPacket(packet);
                    }
                });
    }

    public void close() {
        if (listener != null) {
            ProtocolLibrary.getProtocolManager().removePacketListener(listener);
            listener = null;
        }
    }

    private void translateBlockChange(PacketContainer packet, World world, Player player)
            throws FieldAccessException {
        WrapperPlayServerBlockChange packetWrapper = new WrapperPlayServerBlockChange(packet);
        int x = packetWrapper.getLocation().getX();
        int y = packetWrapper.getLocation().getY();
        int z = packetWrapper.getLocation().getZ();

        Material type = packetWrapper.getBlockData().getType();
        if (this.plugin.getConfiguration().getHideMaterials().contains(type) && plugin.getTranslator().needsTranslation(world, x, y, z, player)) {
            packetWrapper
                    .setBlockData(WrappedBlockData.createData(this.plugin.getConfiguration().getPreferredObfuscationMaterial()));
        }
    }

    private void translateMultiBlockChange(PacketContainer packet, World world, Player player)
            throws FieldAccessException {
        WrapperPlayServerMultiBlockChange packetWrapper = new WrapperPlayServerMultiBlockChange(packet);
        MultiBlockChangeInfo[] array = packetWrapper.getRecords();
        for (MultiBlockChangeInfo change : array) {
            int x = change.getAbsoluteX();
            int y = change.getY();
            int z = change.getAbsoluteZ();
            Material type = change.getData().getType();
            if (this.plugin.getConfiguration().getHideMaterials().contains(type) && plugin.getTranslator().needsTranslation(world, x, y, z, player)) {
                change.setData(WrappedBlockData.createData(this.plugin.getConfiguration().getPreferredObfuscationMaterial()));
            }
        }
        packetWrapper.setRecords(array);
    }
}
