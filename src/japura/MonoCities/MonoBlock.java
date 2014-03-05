/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.MonoCities;

public class MonoBlock {
	byte data;
	byte type;
	
	public MonoBlock() {
		
	}
	
	public MonoBlock(byte data, byte type) {
		this.data = data;
		this.type = type;
	}
	
	public void setData(byte data) {
		this.data = data;
	}
	
	public void setType(byte type) {
		this.type = type;
	}
	
	public byte getData() {
		return data;
	}
	
	public byte getType() {
		return type;
	}

}
