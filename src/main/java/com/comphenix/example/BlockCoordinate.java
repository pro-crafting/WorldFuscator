package com.comphenix.example;

import java.io.Serializable;
import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

class BlockCoordinate implements Serializable {
    private static final long serialVersionUID = 1L;
	
	private final int x;
	private final int y;
	private final int z;
	
	public BlockCoordinate(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public BlockCoordinate(Location loc) {
		this(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(new int[] {x, y, z });
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj instanceof BlockCoordinate) {
			BlockCoordinate other = (BlockCoordinate) obj;
			return x == other.x && y == other.y && z == other.z;
		}
		return true;
	}

	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}
	
	public Block toBlock(World world) {
		return world.getBlockAt(x, y, z);
	}
	
	@Override
	public String toString() {
		return "[x: " + x + ", y: " + y + ", z: " + z + "]";
	}
}
