/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.MonoLocks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class LockBreakListener implements Listener{

	public LockBreakListener(JavaPlugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this,plugin);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void protectBreak(BlockBreakEvent e) {
		Block block;
		Player user = e.getPlayer();
		block = e.getBlock();
		if (block.getType().equals(Material.IRON_DOOR) ||
				block.getType().equals(Material.WOODEN_DOOR) ||
				block.getType().equals(Material.WALL_SIGN) ||
				block.getType().equals(Material.CHEST)) {
			
			if (block.getType().equals(Material.WALL_SIGN)) {
				Block chest = MonoLocks.findChest(block);
				Block door = MonoLocks.findDoor(block);
				if ( chest != null) block = chest;
				if ( door != null) block = door;
			}
			//check if they broke the top of a door
			if (block.getType().equals(Material.WOODEN_DOOR) ||
					block.getType().equals(Material.IRON_DOOR)) {
				Block below = block.getRelative(0, -1, 0);
				if (below.getType().equals(Material.WOODEN_DOOR) ||
						below.getType().equals(Material.IRON_DOOR)){
					block = below;
				}
				
			}
			
			if (MonoLocks.isAllowed(user,block)) {
				MonoLocks.log("lock broken");
				e.setCancelled(false);
				return;
				
			} else {
				
				e.setCancelled(true);
				user.sendMessage("this is protected");
			}
		}
		
	}
}
