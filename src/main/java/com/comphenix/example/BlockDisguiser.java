package com.comphenix.example;

import com.comphenix.example.ChunkPacketProcessor.ChunkletProcessor;
import com.comphenix.packetwrapper.BlockChangeArray;
import com.comphenix.packetwrapper.BlockChangeArray.BlockChange;
import com.comphenix.packetwrapper.WrapperPlayServerBlockChange;
import com.comphenix.packetwrapper.WrapperPlayServerMultiBlockChange;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import de.pro_crafting.worldfuscator.WorldFuscator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Simple class that can be used to alter the apperance of a number of blocks.
 * @author Kristian
 */
public class BlockDisguiser {
	// The current listener
	private PacketAdapter listener;
	private WorldFuscator plugin;
	
	/**
	 * Construct a new block changer.
	 * @param parent - the owner plugin.
	 */
	public BlockDisguiser(WorldFuscator parent) {
		registerListener(parent);
		this.plugin = parent;
	}
	
	private void registerListener(Plugin plugin) {
		final ChunkletProcessor processor = getChunkletProcessor();
		
		ProtocolLibrary.getProtocolManager().addPacketListener(
		  listener = new PacketAdapter(plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.BLOCK_CHANGE, PacketType.Play.Server.MULTI_BLOCK_CHANGE,
				  PacketType.Play.Server.MAP_CHUNK, PacketType.Play.Server.MAP_CHUNK_BULK) {
			
			public void onPacketSending(PacketEvent event) {
				if (ProtocolLibrary.getProtocolManager().getProtocolVersion(event.getPlayer()) > 5) {
					return;
				}

				PacketContainer packet = event.getPacket();
				World world = event.getPlayer().getWorld();
				if (event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
					translateBlockChange(packet, world, event.getPlayer());
				} else if (event.getPacketType() == PacketType.Play.Server.MULTI_BLOCK_CHANGE) {
					translateMultiBlockChange(packet, world, event.getPlayer());
				} else if (event.getPacketType() == PacketType.Play.Server.MAP_CHUNK) {
					ChunkPacketProcessor.fromMapPacket(packet, world).process(processor, event.getPlayer());
				} else if (event.getPacketType() == PacketType.Play.Server.MAP_CHUNK_BULK) {
					for (ChunkPacketProcessor chunk : ChunkPacketProcessor.fromMapBulkPacket(packet, world)) {
						chunk.process(processor, event.getPlayer());
					}
				}
			}	
		 });
	}
	
	public void close() {
		if (listener != null) {
			ProtocolLibrary.getProtocolManager().removePacketListener(listener);
			listener = null;
		}
	}
	
	private ChunkletProcessor getChunkletProcessor() {
		return new ChunkletProcessor() {
			public void processChunklet(Location origin, byte[] data, int blockIndex, int dataIndex, Player player) {
				World world = origin.getWorld();
				int originX = origin.getBlockX();
				int originY = origin.getBlockY();
				int originZ = origin.getBlockZ();

				for (int posY=0;posY<16;posY++) {
					for (int posZ=0;posZ<16;posZ++) {
						for (int posX=0;posX<16;posX++) {
							int offset = blockIndex + posX + posZ * 16 + posY * 256;
							if (data.length > offset) {
								int absX = originX+posX;
								int absY = originY+posY;
								int absZ = originZ+posZ;

								data[offset] = (byte) plugin.translateBlockID(world, absX, absY, absZ, world.getBlockAt(absX, absY, absZ).getTypeId(), player);
							}
						}
					}
				}
			}
		};
	}
	
    private void translateBlockChange(PacketContainer packet, World world, Player player) throws FieldAccessException {
    	WrapperPlayServerBlockChange packetWrapper = new WrapperPlayServerBlockChange(packet);
    	packetWrapper.setBlockType(Material.getMaterial(plugin.translateBlockID(world, packetWrapper.getX(), packetWrapper.getY(), packetWrapper.getZ(), packetWrapper.getBlockType().getId(), player)));
    }
    
    private void translateMultiBlockChange(PacketContainer packet, World world, Player player) throws FieldAccessException {
    	WrapperPlayServerMultiBlockChange packetWrapper = new WrapperPlayServerMultiBlockChange(packet);
    	BlockChangeArray array = packetWrapper.getRecordDataArray();
    	int chunkX = packetWrapper.getChunkX();
    	int chunkZ = packetWrapper.getChunkZ();
    	for (int i=0; i<array.getSize();i++) {
    		BlockChange change = array.getBlockChange(i);
    		int x = change.getAbsoluteX(chunkX);
    		int y = change.getAbsoluteY();
    		int z = change.getAbsoluteZ(chunkZ);
    		int id = plugin.translateBlockID(world, x, y, z, change.getBlockID(), player);
    		change.setBlockID(id);
    	}
    	packetWrapper.setRecordData(array);
    }
}