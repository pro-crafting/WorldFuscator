package de.pro_crafting.worldfuscator.Core;

import com.comphenix.example.ChunkPacketProcessor;
import com.comphenix.example.MapPacketChunkletProcessor;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import de.pro_crafting.region.Region;
import de.pro_crafting.region.events.RegionDomainChangeEvent;
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
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;


public class WorldFuscatorListener implements Listener {

  private final MapPacketChunkletProcessor processor;
  private final Constructor<?> chunkPacketConstructor;
  private WorldFuscator plugin;
  private Method chunkHandle;

  public WorldFuscatorListener(WorldFuscator plugin) {
    this.plugin = plugin;
    Class<?> craftChunk = MinecraftReflection.getCraftBukkitClass("CraftChunk");
    try {
      chunkHandle = craftChunk.getDeclaredMethod("getHandle");
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    Class<?> chunkPacketClass = MinecraftReflection
        .getMinecraftClass("PacketPlayOutMapChunk");
    chunkPacketConstructor = chunkPacketClass.getDeclaredConstructors()[0];
    processor = new MapPacketChunkletProcessor(
        this.plugin.getTranslater());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void handleDomainChange(RegionDomainChangeEvent event) {
    if (event.getNewPlayers().size() == event.getOldPlayers().size() &&
        event.getNewPlayers().containsAll(event.getOldPlayers()) &&
        event.getOldPlayers().containsAll(event.getNewPlayers())) {
      return;
    }

    Set<UUID> playersToUpdate = new HashSet<>(event.getOldPlayers());
    playersToUpdate.addAll(event.getNewPlayers());

    List<Player> onlinePlayers = new ArrayList<>();

    for (UUID uuid : playersToUpdate) {
      Player player = Bukkit.getPlayer(uuid);
      if (player != null) {
        onlinePlayers.add(player);
      }
    }

    boolean debugEnabled = plugin.getConfiguration().isDebugEnabled();
    if (debugEnabled) {
      Bukkit.getLogger().info("Chunk refresh of region: " + event.getRegion().getId());
      Bukkit.getLogger().info("Old players: " + Arrays.toString(event.getOldPlayers().toArray()));
      Bukkit.getLogger().info("New players: " + Arrays.toString(event.getNewPlayers().toArray()));
      Bukkit.getLogger()
          .info("Player to update: " + Arrays.toString(playersToUpdate.toArray()));
    }

    Set<Chunk> chunksToUpdate = new HashSet<>();
    Region updatedRegion = event.getRegion();
    World world = updatedRegion.getWorld();
    Chunk startChunk = updatedRegion.getMin().toLocation(world).getChunk();
    Chunk endChunk = updatedRegion.getMax().toLocation(world).getChunk();
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
        Object chunkPacketNms = chunkPacketConstructor
            .newInstance(nativeChunk, '\uffff');

        PacketContainer packet = new PacketContainer(Server.MAP_CHUNK, chunkPacketNms);

        for (Player player : players) {
          World world = chunk.getWorld();
          packet = ChunkPacketProcessor.clone(packet, world);
          ChunkPacketProcessor
              .fromMapPacket(packet, world).process(
              processor, player, packet);
          protocolManager.sendServerPacket(player, packet, false);
        }
      } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
        e.printStackTrace();
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void handlePlayerInteract(PlayerInteractEvent event) {
    if (event.getHand() != EquipmentSlot.HAND) {
      return;
    }

    if (!event.hasItem()) {
      return;
    }

    ItemStack item = event.getItem();

    if (item.getType() != Material.BLAZE_ROD) {
      return;
    }

    Player player = event.getPlayer();
    if (!player.hasPermission("worldfuscator.debug.toggle")) {
      return;
    }

    boolean debugEnabled = this.plugin.getConfiguration().isDebugEnabled();
    debugEnabled = !debugEnabled;
    this.plugin.getConfiguration().setDebugEnabled(debugEnabled);

    player.sendMessage("[WorldFuscator] Debug ist nun auf " + debugEnabled);
  }
}
