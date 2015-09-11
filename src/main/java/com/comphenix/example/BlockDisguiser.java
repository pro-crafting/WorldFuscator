package com.comphenix.example;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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
		  listener = new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.BLOCK_CHANGE, PacketType.Play.Server.MULTI_BLOCK_CHANGE,
				  PacketType.Play.Server.MAP_CHUNK, PacketType.Play.Server.MAP_CHUNK_BULK) {
			
			public void onPacketSending(PacketEvent event) {
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
				
				for (int posX=0;posX<16;posX++) {
					for (int posY=0;posY<16;posY++) {
						for (int posZ=0;posZ<16;posZ++) {
							int offset = blockIndex + posX + posZ * 16 + posY * 256;
							if (data.length > offset) {
								data[offset] = (byte) translateBlockID(world, posX+originX, posY+originY, posZ+originZ, data[offset] & 0xFF, player);
							}
						}
					}
				}
				
			}
		};
	}
	
    private void translateBlockChange(PacketContainer packet, World world, Player player) throws FieldAccessException {
    	WrapperPlayServerBlockChange packetWrapper = new WrapperPlayServerBlockChange(packet);
    	packetWrapper.setBlockType(Material.getMaterial(translateBlockID(world, packetWrapper.getX(), packetWrapper.getY(), packetWrapper.getZ(), packetWrapper.getBlockType().getId(), player)));
    }
    
    private void translateMultiBlockChange(PacketContainer packet, World world, Player player) throws FieldAccessException {
    	WrapperPlayServerMultiBlockChange packetWrapper = new WrapperPlayServerMultiBlockChange(packet);
    	BlockChangeArray array = packetWrapper.getRecordDataArray();
    	int chunkX = packetWrapper.getChunkX();
    	int chunkZ = packetWrapper.getChunkZ();
		System.out.println("chunk: "+chunkX+"; "+chunkZ);
    	for (int i=0; i<array.getSize();i++) {
    		BlockChange change = array.getBlockChange(i);
    		int relativeX = change.getRelativeX();
    		if (relativeX < 0) {
    			relativeX = 15+relativeX;
    		}
    		int relativeZ = change.getRelativeZ();
    		if (relativeZ < 0) {
    			relativeZ = 15+relativeZ;
    		}
    		System.out.println("relative: "+relativeX+"; "+relativeZ);
    		int x = chunkX * 16 + Math.abs(relativeX);
    		int y = change.getAbsoluteY();
    		int z = chunkZ * 16 +  Math.abs(relativeZ);
    		int id = translateBlockID(world, x, y, z, change.getBlockID(), player);
    		change.setBlockID(id);
    	}
    	packetWrapper.setRecordData(array);
    }
    
	private int translateBlockID(World world, int x, int y, int z, int blockID,
			Player player) {

		if (blockID == 29 || blockID == 46 || blockID == 69 || blockID == 70
				|| blockID == 72 || blockID == 76 || blockID == 77
				|| blockID == 96 || blockID == 107 || blockID == 123
				|| blockID == 124 || blockID == 131 || blockID == 143
				|| blockID == 147 || blockID == 148 || blockID == 151
				|| blockID == 152 || blockID == 154 || blockID == 158
				|| blockID == 148 || blockID == 36 || blockID == 75
				|| blockID == 149 || blockID == 150 || blockID == 93
				|| blockID == 94 || blockID == 55) {
			if (this.plugin.hasRights(player, x, y, z, world)) {
				return blockID;
			}
			return 121;
		}
		return blockID;
	}
}