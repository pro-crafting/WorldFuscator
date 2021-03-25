package com.pro_crafting.mc.worldfuscator.engine;

import com.comphenix.packetwrapper.WrapperPlayServerBlockBreak;
import com.comphenix.packetwrapper.WrapperPlayServerBlockChange;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.pro_crafting.mc.worldfuscator.NMSReflection;
import com.pro_crafting.mc.worldfuscator.engine.processor.ChunkletProcessor;
import com.pro_crafting.mc.worldfuscator.engine.processor.ChunkletProcessorFactory;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;

public class WorldFuscatorEngine {

    private JavaPlugin parent;
    private BlockTranslator translator;

    public WorldFuscatorEngine(JavaPlugin parent, BlockTranslator translator) {
        this.parent = parent;
        this.translator = translator;
    }

    public void start() {
        MinecraftVersion minecraftVersion = ProtocolLibrary.getProtocolManager().getMinecraftVersion();

        if (MinecraftVersion.AQUATIC_UPDATE.compareTo(minecraftVersion) > 0) {
            Bukkit.getLogger().info("WorldFuscatorEngine only works for 1.13.0 and up");
            Bukkit.getPluginManager().disablePlugin(parent);
            return;
        }
        registerListener();
    }

    private void registerListener() {
        if (this.translator.getHiddenGlobalPaletteIds() == null || this.translator.getHiddenGlobalPaletteIds().isEmpty()) {
            Bukkit.getLogger().info("No blocks found that should be fuscated. Not doing anything.");
            return;
        }

        ChunkletProcessorFactory chunkletProcessorFactory = new ChunkletProcessorFactory(translator);
        final ChunkletProcessor processor = chunkletProcessorFactory.getProcessor();
        final ChunkPacketProcessor chunkPacketProcessor = new ChunkPacketProcessor(parent.getDataFolder(), translator.getConfiguration().isDebugEnabled());
        PacketAdapter listener = new PacketAdapter(this.parent, ListenerPriority.LOWEST,
                Server.BLOCK_CHANGE,
                Server.MULTI_BLOCK_CHANGE,
                Server.MAP_CHUNK,
                Server.BLOCK_ACTION,
                Server.BLOCK_BREAK) {

            public void onPacketSending(PacketEvent event) {
//                System.out.println("SEND " + event.getPacketType().name());
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
                } else if (event.getPacketType() == Server.MAP_CHUNK) {
                    packet = chunkPacketProcessor.process(ChunkPacketData.fromMapPacket(packet, world), processor, player, packet);
                } else if (event.getPacketType() == Server.BLOCK_BREAK) {
                    packet = translateBlockBreak(packet, world, player);
                }

                event.setPacket(packet);
            }
        };

        if (translator.getConfiguration().getAsyncWorkerCount() > 0 && processor.isThreadSafe()) {
            ProtocolLibrary.getProtocolManager().getAsynchronousManager().registerAsyncHandler(listener).start(translator.getConfiguration().getAsyncWorkerCount());
        } else {
            ProtocolLibrary.getProtocolManager().addPacketListener(listener);
        }
    }


    private PacketContainer translateBlockBreak(PacketContainer packet, World world, Player player)
            throws FieldAccessException {
        WrapperPlayServerBlockBreak wrapper = new WrapperPlayServerBlockBreak(packet);
        BlockPosition blockPosition = wrapper.getLocation();
        WrappedBlockData blockData = wrapper.getBlock();
        return translateSingleBlock(packet,world,player,blockPosition,blockData);
    }

    private PacketContainer translateBlockChange(PacketContainer packet, World world, Player player)
            throws FieldAccessException {
        WrapperPlayServerBlockChange wrapper = new WrapperPlayServerBlockChange(packet);
        BlockPosition blockPosition = wrapper.getLocation();
        WrappedBlockData blockData = wrapper.getBlockData();
        return translateSingleBlock(packet,world,player,blockPosition,blockData);
    }

    private PacketContainer translateSingleBlock(PacketContainer packet, World world, Player player, BlockPosition blockPosition, WrappedBlockData wrappedBlockData) {
        int x = blockPosition.getX();
        int y = blockPosition.getY();
        int z = blockPosition.getZ();

        int globalPaletteId;
        try {
            globalPaletteId = NMSReflection.getCombinedId(wrappedBlockData.getHandle());
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return packet;
        }

        if (this.translator.getHiddenGlobalPaletteIds().contains(globalPaletteId) && !translator.getWorldFuscatorGuard().hasRights(player, x, y, z, world)) {
            PacketContainer clonedPacket = packet.shallowClone();
            Object blockData = null;
            try {
                blockData = NMSReflection.getFromId(translator.getPreferedObfuscationGlobalPaletteId());
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
            if (translator.getHiddenGlobalPaletteIds().contains(globalPaletteId) && !translator.getWorldFuscatorGuard().hasRights(player, x, y, z, world)) {
                Object blockData = null;
                try {
                    blockData = NMSReflection.getFromId(translator.getPreferedObfuscationGlobalPaletteId());
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
