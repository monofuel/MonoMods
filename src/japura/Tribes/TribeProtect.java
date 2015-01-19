/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.Tribes;

import com.mongodb.*;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;

public class TribeProtect extends BukkitRunnable {

	JavaPlugin plugin;

	public TribeProtect(JavaPlugin plugin) {
		this.plugin = plugin;
		new TribeProtectListener(plugin);
		
	}
	
	public void run() {
		updateEmeralds();
	}
	
	public void updateEmeralds() {
		Tribe group;
		for (String name : Tribes.getTribeNames()) {
			group = Tribes.getTribe(name);
			if (group == null) {
				Tribes.log("Emerald updater found tribe with name set to null");
				continue;
			}
			Block[] emeralds = group.getEmeralds();
			for (Block item : emeralds) {
				//TODO:
				//modify this so that it just goes to the emerald
				//table directly rather than work through the tribe class.
				//group.checkEmerald(item);
			}	
		}
	}
	
	public static Tribe getBlockOwnership(Location loc) {
		Tribe group;
		for (String name : Tribes.getTribeNames()) {
			group = Tribes.getTribe(name);
			if (group.checkLocOwnership(loc)) return group;
		}
		return Tribes.getTribe("invalid tribe");
	}
	
	public void stop() {
		
	}
}
