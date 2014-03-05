/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.MonoCities;

import org.bukkit.block.Block;

public class Schematic {
	String name;
	MonoBlock[][][] building;
	short length,width,height;
	
	public Schematic(short length, short width, short height, MonoBlock[][][] building) {
		this.length = length;
		this.width = width;
		this.height = height;
		this.building = building;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	
	public short getLength() {
		return length;
	}
	
	public short getWidth() {
		return width;
	}
	
	public short getHeight() {
		return height;
	}
	
	public MonoBlock[][][] getBuilding() {
		return building;
	}
}
