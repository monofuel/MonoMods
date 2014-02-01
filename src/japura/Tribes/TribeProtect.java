package japura.Tribes;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TribeProtect extends BukkitRunnable {

	public TribeProtect(JavaPlugin plugin) {
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
				group.checkEmerald(item);
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
