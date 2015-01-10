/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.Tribes;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class TribeTeleportListener implements Listener {
	
	public TribeTeleportListener(JavaPlugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this,plugin);
	}
	
	//this should fire AFTER the tribe protection canceler
	@EventHandler (priority = EventPriority.LOWEST)
	public void getDiamonds(BlockPlaceEvent event) {
		//check if the block is an emerald
		if (event.getBlock().getType() != Material.DIAMOND_BLOCK) return;
		if (event.isCancelled()) return;
		
		String user = event.getPlayer().getName();
		Tribe userGroup = Tribes.getPlayersTribe(user);
		//check if they are in a tribe faction
		if (userGroup == null) {
			event.getPlayer().sendMessage("You are not in a tribe");
			return;
		}
		//check if we're in our own territory
		Location loc;
		Tribe group;
		
		loc = event.getBlock().getLocation();
		group = TribeProtect.getBlockOwnership(loc);
		
		if (group != null && group != userGroup) {
			
			event.setCancelled(true);
			event.getPlayer().sendMessage("You're not on your own land");
			return;
		}
		
		group.addDiamond(event.getBlock(),event.getPlayer());
		
	}
	
	
	//catch diamond break events
	@EventHandler
	public void emeraldBreak(BlockBreakEvent event) {
		//check if the block is an diamond
		if (event.getBlock().getType() != Material.DIAMOND_BLOCK) return;
		if (event.isCancelled())  return;
		
		Tribe group = TribeProtect.getBlockOwnership(event.getBlock().getLocation());
		
		if (group == null) return;
		String user = event.getPlayer().getName();
		if (Tribes.getPlayersTribe(user) != group) {
			event.getPlayer().sendMessage("You are not allowed to break here");
			event.setCancelled(true);
			return;
		}

		Tribes.log("Player " + user + " broke " + group.getName() + "'s diamond at " +
			event.getBlock().getLocation().getX() + "," + event.getBlock().getLocation().getY() + "," + 
			event.getBlock().getLocation().getZ());
		event.setCancelled(false);
		group.delDiamond(event.getBlock());
		
	}
}
