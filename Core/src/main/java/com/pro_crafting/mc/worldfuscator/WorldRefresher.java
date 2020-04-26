package com.pro_crafting.mc.worldfuscator;

import com.pro_crafting.mc.worldfuscator.example.ChunkPacketProcessor;
import com.pro_crafting.mc.worldfuscator.example.MapPacketChunkletProcessor;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class WorldRefresher {

    private final WorldFuscator plugin;
    private MapPacketChunkletProcessor processor;
    private Method chunkHandle;
    private Constructor<?> chunkPacketConstructor;


    public WorldRefresher(WorldFuscator plugin) {
        this.plugin = plugin;
        processor = new MapPacketChunkletProcessor(
                this.plugin.getTranslator());

        Class<?> craftChunk = MinecraftReflection.getCraftBukkitClass("CraftChunk");
        try {
            chunkHandle = craftChunk.getDeclaredMethod("getHandle");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        Class<?> chunkPacketClass = MinecraftReflection
                .getMinecraftClass("PacketPlayOutMapChunk");
        chunkPacketConstructor = chunkPacketClass.getDeclaredConstructors()[0];

    }

    public void updateArea(World world, Point min, Point max, Collection<UUID> oldMembers,
                           Collection<UUID> newMembers) {
        if (newMembers.size() == oldMembers.size() &&
                newMembers.containsAll(oldMembers) &&
                oldMembers.containsAll(newMembers)) {
            return;
        }

        Set<UUID> playersToUpdate = new HashSet<>(oldMembers);
        playersToUpdate.addAll(newMembers);

        List<Player> onlinePlayers = new ArrayList<>();

        for (UUID uuid : playersToUpdate) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                onlinePlayers.add(player);
            }
        }

        boolean debugEnabled = plugin.getConfiguration().isDebugEnabled();
        if (debugEnabled) {
            Bukkit.getLogger().info("Old players: " + Arrays.toString(oldMembers.toArray()));
            Bukkit.getLogger().info("New players: " + Arrays.toString(newMembers.toArray()));
            Bukkit.getLogger()
                    .info("Player to update: " + Arrays.toString(playersToUpdate.toArray()));
        }

        Set<Chunk> chunksToUpdate = new HashSet<>();
        Chunk startChunk = min.toLocation(world).getChunk();
        Chunk endChunk = max.toLocation(world).getChunk();
        for (int cx = startChunk.getX(); cx <= endChunk.getX(); cx++) {
            for (int cz = startChunk.getZ(); cz <= endChunk.getZ(); cz++) {
                if (debugEnabled) {
                    Bukkit.getLogger().info("Refreshing Chunk: " + cx + "|" + cz);
                }

                Chunk chunkAt = world.getChunkAt(cx, cz);
                if (chunkAt.isLoaded()) {
                    chunksToUpdate.add(chunkAt);
                }
            }
        }

        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            updateChunks(onlinePlayers, chunksToUpdate);
        }, 20);

    }

    private void updateChunks(Collection<Player> players, Collection<Chunk> chunksToUpdate) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        for (Chunk chunk : chunksToUpdate) {
            try {
                Object nativeChunk = chunkHandle.invoke(chunk);
                Object chunkPacketNms = chunkPacketConstructor.newInstance(nativeChunk, '\uffff');

                PacketContainer packet = new PacketContainer(Server.MAP_CHUNK, chunkPacketNms);

                for (Player player : players) {
                    World world = chunk.getWorld();
                    packet = ChunkPacketProcessor.clone(packet, world);
                    ChunkPacketProcessor
                            .fromMapPacket(packet, world).process(
                            processor, player, packet);
                    protocolManager.sendServerPacket(player, packet, false);
                }
            } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
    }
}
