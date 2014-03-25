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

public class TribeTeleport extends BukkitRunnable {

	public TribeTeleport(JavaPlugin plugin) {
		new TribeTeleportListener(plugin);
		
	}
	
	public void run() {
		updateDiamonds();
	}
	
	public void updateDiamonds() {
		Tribe[] all = Tribes.getTribes();
		for (Tribe group : all) {
			Block[] diamonds = group.getDiamonds();
			for (Block item : diamonds) {
				group.checkDiamond(item);
			}	
		}
	}
}
