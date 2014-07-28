/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.MonoCities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CityPopulator extends BlockPopulator{


	//TODO: If there are no schematics, shut off the plugin.
	HashMap<String,Schematic> schems = new HashMap<String,Schematic>();

	public CityPopulator(JavaPlugin plugin) {
		File folder = new File("plugins/MonoCities/schematics/");
		File[] listOfFiles = folder.listFiles();
		if (listOfFiles == null) {
			MonoCities.log("creating schematics directory");
			folder.mkdirs();
			return;
		}
		MonoCities.log("found " + listOfFiles.length + " files");
		//If there are no schematics, report this and exit.
		if (listOfFiles.length == 0) {
			MonoCities.log("No Schematics found, Disabling MonoCities");
			Bukkit.getPluginManager().disablePlugin(plugin);
			return;
		}
		//load all the schematics	
		for (File item : listOfFiles) {
			String fileName = item.getName();
			if (!fileName.endsWith(".schematic")) continue;
			//MonoCities.log("loading " + fileName);
			NBTLoader loader = new NBTLoader();
			
			Schematic tmp = loader.loadSchematic("plugins/MonoCities/schematics/" + fileName);
			if (tmp == null) continue;
			tmp.setName(fileName);
			schems.put(fileName,tmp);
		}
		
	}

	//the chunks on each side of this chunk exist
	//chunk corners might not
	@Override
	public void populate(World world, Random rand, Chunk chunk) {
		
		int good = 0;
		//check if good biome
		Biome[] doPop = new Biome[]
				{ 
				Biome.PLAINS,
				Biome.ICE_PLAINS,
				Biome.DESERT
				};
		
		for (Biome item : doPop) {
			if (chunk.getBlock(0, 0, 0).getBiome() == item) good++;
			if (chunk.getBlock(0, 0, 15).getBiome() == item) good++;
			if (chunk.getBlock(15, 0, 15).getBiome() == item) good++;
			if (chunk.getBlock(15, 0, 0).getBiome() == item) good++;
		}
		
		if (good < 4) return;
		
		//check if it's flat
		if (!checkFlat(chunk)) return;
		
		//otherwise, let's place a building.
		//MonoCities.log("found valid chunk at " + chunk.getX() + "," + chunk.getZ());
		
		if (chunk.getX() % 3 == 0 && chunk.getZ() % 3 == 0) {
			//place 4way
			//MonoCities.log("placing 4way");
			placeBuilding("4way.schematic",chunk,rand);
		} else if (chunk.getX() % 3 == 0) {
			//MonoCities.log("placing a straight road");
			placeBuilding("straightroad.schematic",chunk,rand);
		}else if (chunk.getZ() % 3 == 0) {
			//MonoCities.log("placing a rotated road");
			placeBuilding("rightroad.schematic",chunk,rand);
		} else {
			//place random building
			//TODO rotate to curb?
			String name = getRandomBuilding(rand);
			//MonoCities.log("placing a " + name);
			placeBuilding(name,chunk,rand);
		}
		
	}
	
	private String getRandomBuilding(Random rand) {
		int building = Math.abs(rand.nextInt() % schems.size());
		String[] keySet = schems.keySet().toArray(new String[schems.keySet().size()]);
		
		while (schems.get(keySet[building]).getName().equals("4way.schematic") || 
				schems.get(keySet[building]).getName().equals("straightroad.schematic") || 
				schems.get(keySet[building]).getName().equals("rightroad.schematic")){
			building = Math.abs(rand.nextInt() % schems.size());
		}
		return keySet[building];
	}
	
	private boolean checkFlat(Chunk chunk) {
		
		Block[] topCorners = new Block[4];
		topCorners[0] = chunk.getBlock(0, 255, 0);
		topCorners[1] = chunk.getBlock(0,255,15);
		topCorners[2] = chunk.getBlock(15,255,15);
		topCorners[3] = chunk.getBlock(15,255,0);
		
		Block[] corners = new Block[4];
		for (int i = 0; i < 4; i++) {
			corners[i] = getLand(topCorners[i]);
		}
		float avg = 0;
		for (int i = 0; i < 4; i++) {
			avg += corners[i].getY();
		}
		avg /= 4;
		float delta = 0;
		for (int i = 0; i < 4; i++) {
			delta += Math.abs(corners[i].getY() - avg);
		}
		if (delta > 10) return false;
		else return true;
	}
	private float getAverage(Chunk chunk) {
		Block[] topCorners = new Block[4];
		topCorners[0] = chunk.getBlock(0, 255, 0);
		topCorners[1] = chunk.getBlock(0,255,15);
		topCorners[2] = chunk.getBlock(15,255,15);
		topCorners[3] = chunk.getBlock(15,255,0);
		
		Block[] corners = new Block[4];
		for (int i = 0; i < 4; i++) {
			corners[i] = getLand(topCorners[i]);
		}
		float avg = 0;
		for (int i = 0; i < 4; i++) {
			avg += corners[i].getY();
		}
		avg /= 4;
		return avg;
	}
	
	private Block getLand(Block top) {
		while (top.getY() > 0) {
			if (top.getType() != Material.AIR &&
				top.getType() != Material.ICE &&
				top.getType() != Material.WATER) return top;
			top = top.getRelative(BlockFace.DOWN);
		}
		//MonoCities.log("Populating empty chunk!");
		return top;
	}
	
	Material[] chestLoot = new Material[] {
				Material.COAL,
				Material.FLINT_AND_STEEL,
				Material.APPLE,
				Material.BOW,
				Material.STICK,
				Material.MUSHROOM_SOUP,
				Material.BROWN_MUSHROOM,
				Material.RED_MUSHROOM,
				Material.BUCKET,
				Material.EXP_BOTTLE,
				Material.REDSTONE,
				Material.BOOK,
				Material.COAL_ORE,
				Material.TORCH,
				Material.IRON_AXE,
				Material.IRON_SWORD,
				Material.IRON_BOOTS,
				Material.IRON_CHESTPLATE,
				Material.IRON_HELMET,
				Material.IRON_LEGGINGS,
				Material.IRON_PICKAXE,
				Material.GOLD_AXE,
				Material.GOLD_SWORD,
				Material.GOLD_BOOTS,
				Material.GOLD_CHESTPLATE,
				Material.GOLD_HELMET,
				Material.GOLD_LEGGINGS,
				Material.GOLD_PICKAXE,
				Material.IRON_INGOT,
				Material.GOLD_INGOT,
				Material.SAPLING,
				Material.SEEDS,
				Material.LEATHER_HELMET,
				Material.LEATHER_CHESTPLATE,
				Material.LEATHER_LEGGINGS,
				Material.LEATHER_BOOTS,
				Material.DIAMOND,
				Material.OBSIDIAN,
				Material.MELON_SEEDS,
				Material.PUMPKIN_SEEDS,
				Material.SUGAR_CANE,
				Material.CARROT_ITEM,
				Material.COCOA,
				Material.EGG,
				Material.ENDER_PEARL,
				Material.FIREWORK,
				Material.FISHING_ROD,
				Material.INK_SACK,
				Material.LAVA_BUCKET,
				Material.WATER_BUCKET,
				Material.MILK_BUCKET,
				Material.POTATO_ITEM,
				Material.YELLOW_FLOWER,
				Material.RED_ROSE,
				Material.EMERALD,
				Material.EMERALD_BLOCK
				};
	
	private void popChest(Block item,Random rand) {
		//if (item.getType() != Material.CHEST) return;
		
		Chest c = (Chest) item.getState();
		Inventory inv = c.getInventory();
		
		int count = Math.abs(rand.nextInt()) % 3;
		
		
		for (int i = 1; i < count; i++) {
			int chance = Math.abs(rand.nextInt()) % chestLoot.length;
			int amount = Math.abs(rand.nextInt()) % 1;
			amount++;
			ItemStack is = new ItemStack(chestLoot[chance],amount);
			inv.addItem(is);
		}
		//MonoCities.log("populating chest");
		c.update();
		
	}
	
	private void placeBuilding(String building, Chunk chunk,Random rand) {
		placeBuilding(schems.get(building),chunk,rand);
	}
	
	private void placeBuilding(Schematic building, Chunk chunk,Random rand) {
		float avg = getAverage(chunk);
		//place foundation
		//MonoCities.log("placing foundation");
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				for (int k = -2; k < 8; k++) {
					if (avg -k < 0) continue;
					chunk.getBlock(i,(int)(avg - k), j).setType(Material.STONE);
				}
			}
		}
		//MonoCities.log("placed foundation");
		//place schematic
		boolean flip = true;

		
		MonoBlock[][][] blocks = building.getBuilding();
		for (int i = 0; i < building.getHeight(); i++) {
			for (int j = 0; j < building.getLength(); j++) {
				for (int k = 0; k < building.getWidth(); k++) {
					
					//if it is a road, it needs to be sunk into the ground.
					if (building.getName().equals("4way.schematic") ||
							building.getName().equals("straightroad.schematic") ||
							building.getName().equals("rightroad.schematic")) {
						if (avg + i - 6 < 0) continue;
						Block tmp = chunk.getBlock(k,(int)(avg + i - 6),j);
						int type = blocks[k][j][i].getType();
						if (type < 0) type += 256;
						tmp.setTypeId(type);
						tmp.setData(blocks[k][j][i].getData());
						if (type == 54){
							//MonoCities.log("populating road chest");
							popChest(tmp,rand);
						}
						//TODO some of this should be modularized between road/building code
						if (type == 52) { //if it's a spawner
							CreatureSpawner spawner = (CreatureSpawner) tmp.getState();
							spawner.setCreatureTypeByName("Zombie");
							spawner.update();
						}
						
					} else {
						//if (avg + i < 0) continue;
						Block tmp;
						if(flip == true){
							tmp = chunk.getBlock(k,(int)(avg + i + 2),j);
						} else {
							int k2,j2;
							k2 = Math.abs(k-16);
							j2 = Math.abs(j-16);
							tmp = chunk.getBlock(k2,(int)(avg + i + 2),j2);
						}
						int type = blocks[k][j][i].getType();
						if (type < 0) type += 256;
						
						tmp.setTypeId(type);
						tmp.setData(blocks[k][j][i].getData());
						if (type == 54) {
							//MonoCities.log("populating building chest");
							popChest(tmp,rand);
						}
						if (type == 52) { //if it's a spawner
							CreatureSpawner spawner = (CreatureSpawner) tmp.getState();
							spawner.setCreatureTypeByName("Zombie");
							spawner.update();
						}
					}
				}
			}
		}
		//MonoCities.log("placed building");
	}
	

	public void close() {
		// TODO Auto-generated method stub
		
	}

}
