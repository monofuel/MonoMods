package japura.MonoLocks;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

public class LockAccessListener implements Listener{

	public LockAccessListener(JavaPlugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this,plugin);
	}
	
	/*@EventHandler(priority = EventPriority.HIGHEST)
	public void getChestAccess(InventoryOpenEvent e) {
		Block block;
		
		InventoryHolder ih = e.getInventory().getHolder();
		
		if (ih instanceof Chest) {
			block = ((Chest) ih).getBlock();
		} else if (ih instanceof DoubleChest) {
			block = ((DoubleChest) ih).getLocation().getBlock();
		} else {
			return;
		}
		
		if (e.getPlayer() instanceof Player) {
			Player user = (Player) e.getPlayer();
			if (MonoLocks.isAllowed(user, block)) {
				e.setCancelled(false);
				return;
			} else {
				e.setCancelled(true);
				user.sendMessage("This chest is locked");
			}
		}
		
	}*/
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void getDoorAccess(PlayerInteractEvent e) {
		Block block;
		Player user = e.getPlayer();
		block = e.getClickedBlock();
		if (user == null || block == null) return;
		
		if (block.getType().equals(Material.IRON_DOOR) ||
				block.getType().equals(Material.WOODEN_DOOR)) {
			
			if (block.getType().equals(Material.WOODEN_DOOR) ||
					block.getType().equals(Material.IRON_DOOR)) {
				Block below = block.getRelative(0, -1, 0);
				if (below.getType().equals(Material.WOODEN_DOOR) ||
						below.getType().equals(Material.IRON_DOOR)){
					block = below;
				}
				
			}
			
			if (MonoLocks.isAllowed(user,block)) {
				e.setCancelled(false);
				
			} else {
				e.setCancelled(true);
				user.sendMessage("this door is locked");
			}
		} else
		
		if (block.getType().equals(Material.CHEST)) {
			
			if (e.getPlayer() instanceof Player) {
				user = (Player) e.getPlayer();
				if (MonoLocks.isAllowed(user, block)) {
					e.setCancelled(false);
					return;
				} else {
					e.setCancelled(true);
					user.sendMessage("This chest is locked");
				}
			}
		}
	}
	
	

}
