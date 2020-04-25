package de.pro_crafting.common;

import org.bukkit.Location;
import org.bukkit.World;

public class Point {
	private int x;
	private int y;
	private int z;
	private final String seperator = "|";
	
	public Point(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Point(Location from) {
		this.x = from.getBlockX();
		this.y = from.getBlockY();
		this.z = from.getBlockZ();
	}

	public Point(Point from) {
		this.x = from.getX();
		this.y = from.getY();
		this.z = from.getZ();
	}

	public Point(String loc) {
		String[] splited = loc.split("\\"+seperator);
		this.x = Integer.parseInt(splited[0]);
		this.y = Integer.parseInt(splited[1]);
		this.z = Integer.parseInt(splited[2]);
	}
	
	public int getX() {
		return this.x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return this.y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	public int getZ() {
		return this.z;
	}

	public void setZ(int z) {
		this.z = z;
	}
	
	public Location toLocation(World world) {
		return new Location(world, x, y, z);
	}
	
	public String toString()
	{
		return x+seperator+y+seperator+z;
	}
	
	public void add(int x, int y, int z) {
		this.x += x;
		this.y += y;
		this.z += z;  
	}
	
	public void add(Point value) {
		if (value == null) {
			return;
		}
		add(value.getX(), value.getY(), value.getZ());
	}
	
	public void subtract(int x, int y, int z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
	}
	
	public void subtract(Point value) {
		if (value == null) {
			return;
		}
		subtract(value.getX(), value.getY(), value.getZ());
	}
	
	public void multiply(int value) {
		this.x *= value;
		this.y *= value;
		this.z *= value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.x;
		result = prime * result + this.y;
		result = prime * result + this.z;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point other = (Point) obj;
		if (this.x != other.x)
			return false;
		if (this.y != other.y)
			return false;
		if (this.z != other.z)
			return false;
		return true;
	}
}