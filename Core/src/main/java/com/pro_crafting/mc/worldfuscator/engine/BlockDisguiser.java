package com.pro_crafting.mc.worldfuscator.engine;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.pro_crafting.mc.worldfuscator.WorldFuscator;
import com.pro_crafting.mc.worldfuscator.engine.ChunkPacketProcessor.ChunkletProcessor;
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
        registerListener(parent);
        ChunkPacketProcessor.dataFolder = parent.getDataFolder();
        ChunkPacketProcessor.isDebugEnabled = parent.getConfiguration().isDebugEnabled();
    }

    private void registerListener(Plugin plugin) {
        final ChunkletProcessor processor = new MapPacketChunkletProcessor(this.plugin.getTranslator());

        PacketAdapter listener = new PacketAdapter(plugin, ListenerPriority.LOWEST,
                Server.BLOCK_CHANGE, Server.MULTI_BLOCK_CHANGE, Server.MAP_CHUNK) {

            public void onPacketSending(PacketEvent event) {
                Player player = event.getPlayer();

                if (player.hasPermission("worldfuscator.bypass")) {
                    return;
                }

                PacketContainer packet = event.getPacket();

                World world = player.getWorld();
                if (event.getPacketType() == Server.BLOCK_CHANGE) {
                    packet = translateBlockChange(packet, world, player);
                } else if (event.getPacketType() == Server.MULTI_BLOCK_CHANGE) {
                    packet = translateMultiBlockChange(packet, world, player);
                } else {
                    packet = ChunkPacketProcessor.fromMapPacket(packet, world).process(processor, player, packet);
                }

                event.setPacket(packet);
            }
        };

        if (processor.isThreadSafe()) {
            ProtocolLibrary.getProtocolManager().getAsynchronousManager().registerAsyncHandler(listener).start(Runtime.getRuntime().availableProcessors());
        } else {
            ProtocolLibrary.getProtocolManager().addPacketListener(listener);
        }
    }

    public void close() {
        if (listener != null) {
            ProtocolLibrary.getProtocolManager().removePacketListener(listener);
            listener = null;
        }
    }

    private PacketContainer translateBlockChange(PacketContainer packet, World world, Player player)
            throws FieldAccessException {
        BlockPosition blockPosition = packet.getBlockPositionModifier().read(0);
        int x = blockPosition.getX();
        int y = blockPosition.getY();
        int z = blockPosition.getZ();

        Material type = packet.getBlockData().read(0).getType();
        if (this.plugin.getConfiguration().getHideMaterials().contains(type) && plugin.getTranslator().needsTranslation(world, x, y, z, player)) {
            PacketContainer clonedPacket = packet.shallowClone();
            clonedPacket.getBlockData().write(0, WrappedBlockData.createData(this.plugin.getConfiguration().getPreferredObfuscationMaterial()));
            return clonedPacket;
        }

        return packet;
    }

    private PacketContainer translateMultiBlockChange(PacketContainer packet, World world, Player player)
            throws FieldAccessException {
        boolean didFuscate = false;
        MultiBlockChangeInfo[] array = packet.getMultiBlockChangeInfoArrays().read(0).clone();
        for (MultiBlockChangeInfo change : array) {
            int x = change.getAbsoluteX();
            int y = change.getY();
            int z = change.getAbsoluteZ();
            Material type = change.getData().getType();
            if (this.plugin.getConfiguration().getHideMaterials().contains(type) && plugin.getTranslator().needsTranslation(world, x, y, z, player)) {
                change.setData(WrappedBlockData.createData(this.plugin.getConfiguration().getPreferredObfuscationMaterial()));
                didFuscate = true;
            }
        }

        if (didFuscate) {
            PacketContainer clonedPacket = packet.shallowClone();
            clonedPacket.getMultiBlockChangeInfoArrays().write(0, array);
            return clonedPacket;
        }
        return packet;
    }
}
