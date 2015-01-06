/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.Tribes;

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
		Tribe[] all = Tribes.getTribes();
		for (Tribe group : all) {
			Block[] emeralds = group.getEmeralds();
			for (Block item : emeralds) {
				//STUB
				//group.checkEmerald(item);
			}	
		}
	}
	
	public static Tribe getBlockOwnership(Location loc) {
		Tribe[] all = Tribes.getTribes();
		for (Tribe group : all) {
			if (group.checkLocOwnership(loc)) return group;
		}
		return null;
	}
	
	public void stop() {
		
	}
}
