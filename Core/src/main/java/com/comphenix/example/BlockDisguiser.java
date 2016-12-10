package com.comphenix.example;

import com.comphenix.example.ChunkPacketProcessor.ChunkletProcessor;
import com.comphenix.packetwrapper.WrapperPlayServerBlockChange;
import com.comphenix.packetwrapper.WrapperPlayServerMultiBlockChange;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;

import de.pro_crafting.worldfuscator.Core.WorldFuscator;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Simple class that can be used to alter the apperance of a number of blocks.
 * @author Kristian
 */
public class BlockDisguiser {
	private final MapPacketChunkletProcessor mapPacketChunkletProcessor;
	// The current listener
	private PacketAdapter listener;
	private WorldFuscator plugin;
	
	/**
	 * Construct a new block changer.
	 * @param parent - the owner Plugin.
	 */
	public BlockDisguiser(WorldFuscator parent) {
		this.plugin = parent;
		this.mapPacketChunkletProcessor = new MapPacketChunkletProcessor(this.plugin.getTranslater());
		registerListener(parent);
		ChunkPacketProcessor.dataFolder = parent.getDataFolder();
		ChunkPacketProcessor.isDebugEnabled = parent.getConfiguration().isDebugEnabled();
	}
	
	private void registerListener(Plugin plugin) {
		final ChunkletProcessor processor = new MapPacketChunkletProcessor(this.plugin.getTranslater());
		
		ProtocolLibrary.getProtocolManager().addPacketListener(
		  listener = new PacketAdapter(plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.BLOCK_CHANGE, PacketType.Play.Server.MULTI_BLOCK_CHANGE,
				  PacketType.Play.Server.MAP_CHUNK) {
			
			public void onPacketSending(PacketEvent event) {
				Player player = event.getPlayer();

				if (player.hasPermission("worldfuscator.bypass")) {
					return;
				}

                PacketContainer packet = event.getPacket();

				World world = player.getWorld();
				if (event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
                    packet = packet.shallowClone();
                    translateBlockChange(packet, world, player);
                } else if (event.getPacketType() == PacketType.Play.Server.MULTI_BLOCK_CHANGE) {
                    packet = packet.shallowClone();
                    translateMultiBlockChange(packet, world, player);
                } else if (event.getPacketType() == PacketType.Play.Server.MAP_CHUNK) {
					packet = ChunkPacketProcessor.clone(packet, world);
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

	private void translateBlockChange(PacketContainer packet, World world, Player player) throws FieldAccessException {
		WrapperPlayServerBlockChange packetWrapper = new WrapperPlayServerBlockChange(packet);
		int x = packetWrapper.getLocation().getX();
		int y = packetWrapper.getLocation().getY();
		int z = packetWrapper.getLocation().getZ();
		State blockState = new State(packetWrapper.getBlockData());
		int id = plugin.getTranslater().translateBlockID(world, x, y, z, player, blockState);
		packetWrapper.setBlockData(WrappedBlockData.createData(Material.getMaterial(id), blockState.getData()));
    }
    
    private void translateMultiBlockChange(PacketContainer packet, World world, Player player) throws FieldAccessException {
        WrapperPlayServerMultiBlockChange packetWrapper = new WrapperPlayServerMultiBlockChange(packet);
        MultiBlockChangeInfo[] array = packetWrapper.getRecords();
        for (MultiBlockChangeInfo change : array) {
			int x = change.getAbsoluteX();
			int y = change.getY();
			int z = change.getAbsoluteZ();
			State blockState = new State(change.getData());
			int id = plugin.getTranslater().translateBlockID(world, x, y, z, player, blockState);
			change.setData(WrappedBlockData.createData(Material.getMaterial(id), blockState.getData()));
		}
		packetWrapper.setRecords(array);
    }
}