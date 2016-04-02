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
import de.pro_crafting.worldfuscator.WorldFuscator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.nio.ByteBuffer;
import java.util.Arrays;

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
				  PacketType.Play.Server.MAP_CHUNK) {
			
			public void onPacketSending(PacketEvent event) {
				//if (ProtocolLibrary.getProtocolManager().getProtocolVersion(event.getPlayer()) > 5) {
				//	return;
				//}

				if (event.getPlayer().hasPermission("worldfuscator.bypass")) {
					return;
				}

				PacketContainer packet = event.getPacket();
				World world = event.getPlayer().getWorld();
				if (event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
					translateBlockChange(packet, world, event.getPlayer());
				} else if (event.getPacketType() == PacketType.Play.Server.MULTI_BLOCK_CHANGE) {
					translateMultiBlockChange(packet, world, event.getPlayer());
				} else if (event.getPacketType() == PacketType.Play.Server.MAP_CHUNK) {
					ChunkPacketProcessor.fromMapPacket(packet, world).process(processor, event.getPlayer(), packet);
				}// else if (event.getPacketType() == PacketType.Play.Server.MAP_CHUNK) {
				//	for (ChunkPacketProcessor chunk : ChunkPacketProcessor.fromMapBulkPacket(packet, world)) {
				//		chunk.process(processor, event.getPlayer());
				//	}
				//}
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

			class State {
				int id;
				int data;

				public State(int id, int data) {
					this.id = id;
					this.data = data;
				}

				@Override
				public String toString() {
					return id+":"+data;
				}
			}

			public int processChunklet(Location origin, ByteBuffer buffer, Player player) {
				World world = origin.getWorld();
				int originX = origin.getBlockX();
				int originY = origin.getBlockY();
				int originZ = origin.getBlockZ();

				int offset = 0;
				System.out.println("startbits: "+Long.toBinaryString(buffer.getLong()));
				buffer.position(buffer.position()-8);
				System.out.println("startposition: "+buffer.position());
				int bitsPerBlock = buffer.get();
				System.out.println("Bits per Block :"+bitsPerBlock);
				int paletteLength = 256;
				State[] palette = new State[1];
				if (bitsPerBlock != 0) {
					paletteLength = deserializeVarInt(buffer);
					System.out.println("length palette: "+paletteLength);
					palette = new State[paletteLength];
					for (int x = 0; x < paletteLength; x++) {
						int state = deserializeVarInt(buffer);
						palette[x] = new State(state >> 4, state & 0xF);
					}
					System.out.println("Palette: "+ Arrays.toString(palette));
				}
				int dataLength = deserializeVarInt(buffer)*8;

				int beforeData = buffer.position();
				System.out.println("beforedata: "+beforeData);

				System.out.println("length data: "+dataLength);
				int blockCount = (dataLength*8)/(bitsPerBlock == 0 ? 13 : bitsPerBlock);
				System.out.println("Block count: "+blockCount);
				long[] blockIndizes = new long[dataLength/8];
				buffer.asLongBuffer().get(blockIndizes);
				FlexibleStorage fS = new FlexibleStorage(bitsPerBlock, blockIndizes);

				System.out.println("fs length: "+fS.getData().length);
				for (int posY=0;posY<16;posY++) {
					for (int posZ = 0; posZ < 16; posZ++) {
						for (int posX = 0; posX < 16; posX++) {
							int index = posX + posZ * 16 + posY * 256;

							int x = originX + posX;
							int y = originY + posY;
							int z = originZ + posZ;

							State blockStateBefore = palette[fS.get(index)];
							int blockIdAfter = plugin.translateBlockID(world, x, y, z, player);

							if (blockStateBefore.id != blockIdAfter) {
								fS.set(index, 0);
							}
						}
					}
				}

				buffer.position(beforeData);
				buffer.asLongBuffer().put(blockIndizes);
				buffer.position(beforeData+dataLength);
				return beforeData+dataLength+2048+2048-2;
			}
		};
	}
	
    private void translateBlockChange(PacketContainer packet, World world, Player player) throws FieldAccessException {
    	WrapperPlayServerBlockChange packetWrapper = new WrapperPlayServerBlockChange(packet);
		int x = packetWrapper.getLocation().getX();
		int y = packetWrapper.getLocation().getY();
		int z = packetWrapper.getLocation().getZ();
		int id = plugin.translateBlockID(world, x, y, z, player);
		packetWrapper.setBlockData(WrappedBlockData.createData(Material.getMaterial(id)));
    }
    
    private void translateMultiBlockChange(PacketContainer packet, World world, Player player) throws FieldAccessException {
    	WrapperPlayServerMultiBlockChange packetWrapper = new WrapperPlayServerMultiBlockChange(packet);
    	MultiBlockChangeInfo[] array = packetWrapper.getRecords();
		for (MultiBlockChangeInfo change : array) {
			int x = change.getAbsoluteX();
			int y = change.getY();
			int z = change.getAbsoluteZ();
			int id = plugin.translateBlockID(world, x, y, z, player);
			change.setData(WrappedBlockData.createData(Material.getMaterial(id)));
		}
    }

	// Aus dem 1.9.1 MC Server (PacketSerializer)
	public static int deserializeVarInt(ByteBuffer buf)
	{
		int i = 0;
		int j = 0;
		for (;;)
		{
			int k = buf.get();

			i |= (k & 0x7F) << j++ * 7;
			if (j > 5) {
				throw new RuntimeException("VarInt too big");
			}
			if ((k & 0x80) != 128) {
				break;
			}
		}
		return i;
	}
}