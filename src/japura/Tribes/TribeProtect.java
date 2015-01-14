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
		DBCursor cursor = Tribes.getTribes();
		Tribe group;
		String name;
		while (cursor.hasNext()) {
			name = (String) cursor.next().get("name");
			group = Tribes.getTribe(name);
			if (group == null) {
				Tribes.log("Emerald updater found tribe with name set to null");
				continue;
			}
			Block[] emeralds = group.getEmeralds();
			for (Block item : emeralds) {
				//STUB
				//group.checkEmerald(item);
			}	
		}
	}
	
	public static Tribe getBlockOwnership(Location loc) {
		DBCursor cursor = Tribes.getTribes();
		Tribe group;
		while (cursor.hasNext()) {
			group = Tribes.getTribe((String) cursor.next().get("name"));
			if (group.checkLocOwnership(loc)) return group;
		}
		return null;
	}
	
	public void stop() {
		
	}
}
