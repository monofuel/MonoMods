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
		//TODO STUB
		//pull up the diamond database
		//and review that all the diamonds on currently-loaded chunks are indeed diamonds
		//if (!em.getChunk().isLoaded()) return;
		//if (em.getType() != Material.EMERALD_BLOCK) emeralds.remove(em);

	}
}
