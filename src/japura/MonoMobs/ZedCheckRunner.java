package japura.MonoMobs;

import japura.MonoUtil.MonoConf;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.json.simple.JSONObject;

public class ZedCheckRunner extends BukkitRunnable{

	private final JavaPlugin plugin;
	private ConcurrentLinkedQueue<Location> loc; //locations to spawn zed at
	private ConcurrentLinkedQueue<Player> adds; //add a zombie to each player
	//ZedFinder finder = null;
	//private ZedFinder[] finders;
	
	public ZedCheckRunner(JavaPlugin plugin) {
		this.plugin = plugin;
		loc = new ConcurrentLinkedQueue<Location>();
		adds = new ConcurrentLinkedQueue<Player>();
		
		maxLight = (long) MonoMobs.getMonoConfig().getConf("max light to spawn");
		
		int threads = (int) (long) MonoMobs.getMonoConfig().getConf("threads");
		
		//finders = new ZedFinder[threads];
		
		/*for (ZedFinder item : finders) {
			item = new ZedFinder(this,maxLight);
			item.start();
		}*/
		
		
		lastTime = System.currentTimeMillis();
		MonoMobs.log("ZedFinder threads spawned");
		
	}
	
	//convert to ints?
	long zed;
	int zedCount;
	long max;
	long maxPerPlayer;
	long maxDistance;
	long maxHeight;
	long tickTime;
	long tickLength;
	long maxLight;
	MonoConf config;
	long lastTime;
	
	public void run() {
		//MonoMobs.log("doing zedCheck on world");
		//TODO update variables only on reload or load and not every tick
		
		config = MonoMobs.getMonoConfig();

		
		//only spawn at night
		if (Bukkit.getWorld((String) config.getConf("world")).getTime() < 13187 &&
				Bukkit.getWorld((String) config.getConf("world")).getTime() > 22812) return;
	
		maxLight = (long) config.getConf("max light to spawn");
		
		
		//check if zedfinder threads died
		/*for (ZedFinder item : finders) {
			if (item == null || !item.isAlive()) {
				item = new ZedFinder(this,maxLight);
				item.start();
			}
		}*/
		
		//check how many zed there are
		zed = MonoMobs.countZed((String) config.getConf("world"));
		
		//check max # of zed
		max = (long) config.getConf("zombie cap");
		
		if (zed > max) {
			MonoMobs.log("max zed reached");
			return;
		}
		
		//check ideal zed per player
		maxPerPlayer = (long) config.getConf("zed per player");
		
		//check zed distance
		maxDistance = (long) config.getConf("zed distance");
		maxHeight = (long) config.getConf("zed y distance");
		
		tickTime = (long) config.getConf("zed spawn tick offset");
		tickLength = (long) config.getConf("zed spawn tick length");
		
		//check server performance
		//if the time delta is > 50, we're laggin.
		if (System.currentTimeMillis() - lastTime > (tickTime * tickLength)) {
			lastTime = System.currentTimeMillis();
			//TODO
			//trim down mobs
			MonoMobs.log("detecting long ticks, skipping spawning turn");
			
			return;
		} else lastTime = System.currentTimeMillis();
		
		
		//add player to add queue
		List<Player> all = Bukkit.getWorld((String) config.getConf("world")).getPlayers();
		for (Player person : all) {
			//MonoMobs.log("counting zed for " + person.getName());
			zedCount = 0;
			List<Entity> entities = person.getNearbyEntities(maxDistance, maxDistance, maxHeight);
			for (Entity item : entities) {
				if (item instanceof Zombie) {
					zedCount++;
				}
			}
			
			for (;zedCount < maxPerPlayer; zedCount++) {
				adds.add(person);
				
			}
		}
		
		new ZedFinder(this,maxLight).run();
		
		//spawn zeds
		while(!loc.isEmpty()) {
			Location spawnLoc = loc.remove();
			Bukkit.getWorld((String) config.getConf("world")).spawnEntity(spawnLoc, EntityType.ZOMBIE);
			//MonoMobs.log("spawning zed");
		}
		
	}
	
	public void stop() {
		/*for (ZedFinder item : finders) {
			if (item != null && item.isAlive()) {
				item.quit();
			}
		}*/
	}


	private class ZedFinder extends Thread {
		
		Random rand = new Random();
		ZedCheckRunner parent;
		boolean stop = false;
		int maxLight = 0;
		
		public ZedFinder(ZedCheckRunner parent,long maxLight) {
			this.parent = parent;
			this.maxLight = (int) maxLight;
		}
		
		public void run() {
			while(!stop) {
				
				//examine the queue of players
				
				if (parent.adds.poll() == null) {
					//try {
						break;
						//this.sleep(10000);
						//TODO shrink that value?
						//i debug dangerously
						//MonoMobs.log("Zed Finder sleeping");
					//} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					//}
					//continue;
				}
				
				Player person;
				
				try {
					person = parent.adds.remove();
				} catch (NoSuchElementException e) {
					//MonoMobs.log("Zed finder race check caught");
					continue;
				}
				
				//find new places to spawn zed
				Location newLoc = getLocForPlayer(person);
				//add zeds to loc list
				if(newLoc == null){
					//MonoMobs.log("unable to find sutable spawn site");
					continue;
				}
				parent.loc.add(newLoc);
				//MonoMobs.log("adding in new zed");
			}
		}
		
		public Location getLocForPlayer(Player person) {
			float angle = rand.nextFloat();
			int x = (int) ((int) (20 + rand.nextInt(80)) * Math.cos(angle*Math.PI));
			int z = (int) ((int) (20 + rand.nextInt(80)) * Math.sin(angle*Math.PI));
			
			
			Location newBlock = person.getLocation();
			newBlock.add(x, 10, z);
			Location lastBlock = newBlock.clone();
			
			for (int i = 0; i < 20; i++) {

				newBlock.add(0, -1, 0);
				if (lastBlock.getBlock().isEmpty()) {
					if (!newBlock.getBlock().isEmpty()) {
						if (lastBlock.getBlock().getLightLevel() < maxLight) {
							return lastBlock;
						}
						//MonoMobs.log("block too bright");
					}
				}
				
				lastBlock = newBlock.clone();
				
			}
			
			//couldn't find valid spot
			return null;
		}
		
		
		public void quit() {
			stop = true;
		}
	}
	
}
