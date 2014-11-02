package com.comphenix.example;

import java.io.Serializable;

import org.bukkit.Location;
import org.bukkit.World;

import com.google.common.base.Objects;

class ChunkCoordinate implements Serializable {
    private static final long serialVersionUID = 1L;
	
	private final String worldID;
	private final int chunkX;
	private final int chunkZ;
	
	private ChunkCoordinate(World world, int chunkX, int chunkZ) {
		this.worldID = world.getName();
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
	}
	
	public static ChunkCoordinate fromBlock(World world, int x, int z) {
		return new ChunkCoordinate(world, x >> 4, z >> 4);
	}
	
	public static ChunkCoordinate fromBlock(Location loc) {
		return fromBlock(loc.getWorld(), loc.getBlockX(), loc.getBlockZ());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(worldID, chunkX, chunkZ);
	}
	
	public int getChunkX() {
		return chunkX;
	}
	
	public int getChunkZ() {
		return chunkZ;
	}
	
	public String getWorldID() {
		return worldID;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj instanceof ChunkCoordinate) {
			ChunkCoordinate other = (ChunkCoordinate) obj;
			return worldID == other.worldID && chunkX == other.chunkX && chunkZ == other.chunkZ;
		}
		return true;
	}

	@Override
	public String toString() {
		return "[worldID: " + worldID + ", chunkX: " + chunkX + ", chunkZ: " + chunkZ + "]";
	}
}