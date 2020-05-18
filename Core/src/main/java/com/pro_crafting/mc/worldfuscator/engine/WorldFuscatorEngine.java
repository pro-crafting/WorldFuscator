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
import com.pro_crafting.mc.worldfuscator.NMSReflection;
import com.pro_crafting.mc.worldfuscator.WorldFuscator;
import com.pro_crafting.mc.worldfuscator.engine.processor.ChunkletProcessor;
import com.pro_crafting.mc.worldfuscator.engine.processor.ChunkletProcessorFactory;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public class WorldFuscatorEngine {

    // The current listener
    private WorldFuscator plugin;

    private WorldFuscatorEngine(WorldFuscator parent) {
        this.plugin = parent;
        ChunkPacketProcessor.dataFolder = parent.getDataFolder();
        ChunkPacketProcessor.isDebugEnabled = parent.getConfiguration().isDebugEnabled();
    }

    public static WorldFuscatorEngine start(WorldFuscator parent) {
        WorldFuscatorEngine engine = new WorldFuscatorEngine(parent);
        engine.registerListener();

        return engine;
    }

    private void registerListener() {
        if (this.plugin.getTranslator().getHiddenGlobalPaletteIds() == null || this.plugin.getTranslator().getHiddenGlobalPaletteIds().isEmpty()) {
            Bukkit.getLogger().info("No blocks found that should be fuscated. Not doing anything.");
            return;
        }

        ChunkletProcessorFactory chunkletProcessorFactory = new ChunkletProcessorFactory(this.plugin);
        final ChunkletProcessor processor = chunkletProcessorFactory.getProcessor();
        final ChunkPacketProcessor chunkPacketProcessor = new ChunkPacketProcessor();

        PacketAdapter listener = new PacketAdapter(this.plugin, ListenerPriority.LOWEST,
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
                    packet = chunkPacketProcessor.process(ChunkPacketData.fromMapPacket(packet, world), processor, player, packet);
                }

                event.setPacket(packet);
            }
        };

        // TODO: More testing about protocollib async handling
        if (plugin.getConfiguration().getAsyncWorkerCount() > 0 && processor.isThreadSafe()) {
            ProtocolLibrary.getProtocolManager().getAsynchronousManager().registerAsyncHandler(listener).start(plugin.getConfiguration().getAsyncWorkerCount());
        } else {
            ProtocolLibrary.getProtocolManager().addPacketListener(listener);
        }
    }

    private PacketContainer translateBlockChange(PacketContainer packet, World world, Player player)
            throws FieldAccessException {
        BlockPosition blockPosition = packet.getBlockPositionModifier().read(0);
        int x = blockPosition.getX();
        int y = blockPosition.getY();
        int z = blockPosition.getZ();

        int globalPaletteId;
        try {
            globalPaletteId = NMSReflection.getCombinedId(packet.getBlockData().read(0).getHandle());
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return packet;
        }

        if (this.plugin.getTranslator().getHiddenGlobalPaletteIds().contains(globalPaletteId) && plugin.getTranslator().needsTranslation(world, x, y, z, player)) {
            PacketContainer clonedPacket = packet.shallowClone();
            Object blockData = null;
            try {
                blockData = NMSReflection.getFromId(plugin.getTranslator().getPreferedObfuscationGlobalPaletteId());
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            clonedPacket.getBlockData().write(0, WrappedBlockData.fromHandle(blockData));
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
            int globalPaletteId;
            try {
                globalPaletteId = NMSReflection.getCombinedId(change.getData().getHandle());
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }
            if (this.plugin.getTranslator().getHiddenGlobalPaletteIds().contains(globalPaletteId) && plugin.getTranslator().needsTranslation(world, x, y, z, player)) {
                Object blockData = null;
                try {
                    blockData = NMSReflection.getFromId(plugin.getTranslator().getPreferedObfuscationGlobalPaletteId());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                change.setData(WrappedBlockData.fromHandle(blockData));
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
