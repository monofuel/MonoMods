/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.MonoCities;

import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import org.bukkit.block.Block;
import org.bukkit.Material;

public class NBTLoader {


	//first byte is tag type
	//followed by 2 bytes for length of the name
	//all strings are in UTF-8
	
	//appears:
	//one array for blocks
	//one array for data
	
	//blocks:
	//07 00 06 42 6c 6f 63 6b 73
	//byte array : string size 6 : "blocks" : long of how big the array is
	private static byte[] blockID = hexStringToByteArray("070006426c6f636b73");
	
	//data
	//07 00 04 44 61 74 61
	private static byte[] dataID = hexStringToByteArray("07000444617461");
	
	//fetch length, width and height so we
	//know the size. expect 16x16.
	
	//height
	//02 00 06 48 65 69 67 68 74
	//followed by short of height
	private static byte[] heightID = hexStringToByteArray("020006486569676874");
	
	//width
	//02 00 05 57 69 64 74 68
	//followed by short of width
	private static byte[] widthID = hexStringToByteArray("0200055769647468");
	
	//length
	//02 00 06 4c 65 6e 67 74 68
	//followed by short of length
	private static byte[] lengthID = hexStringToByteArray("0200064c656e677468");
	
	private byte[] buffer;
	
	public Schematic loadSchematic(String file) {
		
		try {
			ArrayList<byte[]> superBuf = new ArrayList<byte[]>();
			FileInputStream fStream = new FileInputStream(file);
			GZIPInputStream gzis = new GZIPInputStream(fStream);
			int length = gzis.available();
			
			
			byte[] buf = new byte[1];
			int len;
			int totalLen = 1;
			while ((len = gzis.read(buf)) > 0) {
				totalLen += len;
				superBuf.add(buf);
				buf = new byte[1];
			}
			buffer = new byte[totalLen];
			//MonoCities.log("Allocating " + totalLen + " byte buffer for building");
			int copied = 0;
			for (int i = 0; i < superBuf.size(); i++) {
				for (int j = 0; j < 1; j++) {
					if (copied >= totalLen) break;
					buffer[(i * 1) + j] = superBuf.get(i)[j];
					copied++;
				}
				if (copied >= totalLen) break;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		//MonoCities.log("Byte buffer filled");
		
		short length = fetchLength();
		short width = fetchWidth();
		short height = fetchHeight();
		
		//MonoCities.log("imported object is " + length + "," + width + "," + height);
		
		if (length > 16 || width > 16) {
			MonoCities.log("ERROR: schematic too large");
			return null;
		}
		
		MonoBlock[][][] building = fetchBlock(length,width,height);
		//MonoCities.log("loaded " + (building.length * building[0].length * building[0][0].length) +
		//			" blocks");
		
		Schematic item = new Schematic(length,width,height,building);
		return item;
		
	}
	
	/**
	 * returns the location byte directly after the keyword requested
	 * @param search
	 * @return
	 */
	public int byteSeek(byte[] search) {
		for (int i = 0; i < buffer.length + search.length; i++) {
			for (int j = 0; j < search.length; j++) {
				if (buffer[i+j] != search[j]) break;
				if (j == search.length - 1) return i+j+1;
			}
		}
		MonoCities.log("Could not find keyword in byte buffer");
		return 0;
	}
	
	private MonoBlock[][][] fetchBlock(short length, short width, short height) {
		MonoBlock[][][] building = new MonoBlock[length][width][height];
		int blockOffset = byteSeek(blockID) + 4;
		int offset;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				for (int k = 0; k < length; k++) {
					MonoBlock tmp = new MonoBlock();
					offset = blockOffset;
					offset += k;
					offset += (length * j);
					offset += (length * width * i);
					try {
						tmp.setType(buffer[offset]);
					} catch (ArrayIndexOutOfBoundsException e) {
						tmp.setType((byte) 0);
					}
					//replace trapped chests with regular
					//chests
					if (tmp.getType() == (byte) 146)
						tmp.setType((byte) 54);
					building[k][j][i] = tmp;
					
				}
			}
		}
		blockOffset = byteSeek(dataID) + 4;
		
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < width; j++) {
				for (int k = 0; k < height; k++) {
					MonoBlock tmp = building[i][j][k];
					offset = blockOffset;
					offset += i;
					offset += (length * j);
					offset += (length * width * k);
					try {
						tmp.setData(buffer[offset]);
					} catch (ArrayIndexOutOfBoundsException e) {
						tmp.setData((byte) 0);
					}
					
				}
			}
		}
		/*
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				String line = "";
				for (int k = 0; k < length; k++) {
					line += building[k][j][i].getType() + ",";
				}
				MonoCities.log(line);
			}
			MonoCities.log("");
		}*/
		
		return building;
	}
	
	
	private short fetchLength() {
			int offset = byteSeek(lengthID);
			//MonoCities.log("length offset is " + offset);
			byte[] length = Arrays.copyOfRange(buffer, offset, offset+2);
			return ByteBuffer.wrap(length).getShort();
	}
	
	
	private short fetchHeight() {
		int offset = byteSeek(heightID);
		byte[] height = Arrays.copyOfRange(buffer, offset, offset+2);
		return ByteBuffer.wrap(height).getShort();
	}
	
	private short fetchWidth() {
		int offset = byteSeek(widthID);
		byte[] width = Arrays.copyOfRange(buffer, offset, offset+2);
		return ByteBuffer.wrap(width).getShort();
	}
	
	public static byte[] hexStringToByteArray(String line) {
		int len = line.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i+= 2) {
			data[i/2] = (byte) ((Character.digit(line.charAt(i),16) << 4)
								+ Character.digit(line.charAt(i+1),16));
		}
		return data;
	}
	
	private static String printBuff(byte[] buf) {
		StringBuilder line = new StringBuilder();
		for (int i = 0; i < buf.length; i++) {
			line.append(buf[i]);
			line.append(" ");
		}
		return line.toString();
	}
}
