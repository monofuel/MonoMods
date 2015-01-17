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

public class TribeProtectListener implements Listener {
	
	public ArrayList<Entity> tntIgnites = new ArrayList<Entity>();
	private JavaPlugin plugin;
	
	public TribeProtectListener(JavaPlugin plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this,plugin);
	}
	
	//TODO
	//inform players when they enter/leave tribe territory
	@EventHandler
	public void getMove(PlayerMoveEvent move) {
		Location from = move.getFrom();
		Location to = move.getTo();
		
		//get faction ownership of to location
		Tribe fromGroup = TribeProtect.getBlockOwnership(from);
		//get ownership of from location
		Tribe toGroup = TribeProtect.getBlockOwnership(to);
		//if !=, tell the user where
		//they are entering.
		if (!fromGroup.equals(toGroup)) {
			if (!toGroup.isValid()) {
				move.getPlayer().sendMessage("You are entering " +
						"the wilderness");
			} else {
				move.getPlayer().sendMessage("You are entering " +
											toGroup.getName() +
											"'s land");
			}
		}
		
	}
	
	@EventHandler
	public void getEmeralds(BlockPlaceEvent event) {
		//check if the block is an emerald
		if (event.getBlock().getType() != Material.EMERALD_BLOCK) return;
		if (event.isCancelled()) return;
		
		String user = event.getPlayer().getName();
		Tribe group = Tribes.getPlayersTribe(user);
		//check if they are in a tribe faction
		if (!group.isValid()) {
			event.getPlayer().sendMessage("You are not in a tribe");
			event.getPlayer().sendMessage("If you were in a tribe, you could place emeralds to claim land");
			return;
		}

		//check if we're in range of another emerald
		Location loc,corner1,corner2,corner3,corner4;
		Tribe group1,group2,group3,group4;
		
		long claimSize = plugin.getConfig().getLong("ClaimSize");
		loc = event.getBlock().getLocation();
		corner1 = loc.clone().add(claimSize,0, claimSize);
		corner2 = loc.clone().add(-claimSize,0,claimSize);
		corner3 = loc.clone().add(claimSize,0,-claimSize);
		corner4 = loc.clone().add(-claimSize,0,-claimSize);
		group1 = TribeProtect.getBlockOwnership(corner1);
		group2 = TribeProtect.getBlockOwnership(corner2);
		group3 = TribeProtect.getBlockOwnership(corner3);
		group4 = TribeProtect.getBlockOwnership(corner4);
		
		if ((group1.isValid() && ! group1.equals(group)) ||
			(group2.isValid() && ! group2.equals(group)) ||
			(group3.isValid() && ! group3.equals(group)) ||
			(group4.isValid() && ! group4.equals(group))) {
			
			event.setCancelled(true);
			event.getPlayer().sendMessage("You're too close to another tribe");
			return;
		}
		
		group.addEmerald(event.getBlock());
		event.getPlayer().sendMessage("You've claimed land for your tribe");
		
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void blockPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) return;
		Tribe group = TribeProtect.getBlockOwnership(event.getBlock().getLocation());
		if (group.isValid()) {
			//TODO verify this works
			if (!Tribes.getPlayersTribe(event.getPlayer().getName()).equals(group)) {
				event.setCancelled(true);
				event.getPlayer().sendMessage("You are not allowed to build here");
			}
		}
		
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void blockPlace(PlayerBucketEmptyEvent event) {
		Tribe group = TribeProtect.getBlockOwnership(event.getBlockClicked().getRelative(event.getBlockFace()).getLocation());
		if (group.isValid()) {
			if (!Tribes.getPlayersTribe(event.getPlayer().getName()).equals(group)) {
				event.setCancelled(true);
				event.getPlayer().sendMessage("You are not allowed to build here");
			}
		}
		
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void blockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) return;
		Tribe group = TribeProtect.getBlockOwnership(event.getBlock().getLocation());
		if (group.isValid()) {
			if (!Tribes.getPlayersTribe(event.getPlayer().getName()).equals(group)) {
				event.setCancelled(true);
				event.getPlayer().sendMessage("You are not allowed to destroy here");
			}
		}
		
	}
	
	
	//catch emerald break events
	@EventHandler
	public void emeraldBreak(BlockBreakEvent event) {
		//check if the block is an emerald
		if (event.getBlock().getType() != Material.EMERALD_BLOCK) return;
		if (event.isCancelled())  return;
		//assume cancelled
		event.setCancelled(true);
		
		Tribe group = TribeProtect.getBlockOwnership(event.getBlock().getLocation());
		
		if (!group.isValid()) {
			event.setCancelled(false);
			return;
		}
		String user = event.getPlayer().getName();
		if (!Tribes.getPlayersTribe(user).equals(group)) {
			event.getPlayer().sendMessage("You are not allowed to break here");
			event.setCancelled(true);
			return;
		}

		Tribes.log("Player " + user + " broke " + group.getName() + "'s emerald at " +
			event.getBlock().getLocation().getX() + "," + event.getBlock().getLocation().getY() + "," + 
			event.getBlock().getLocation().getZ());
		event.setCancelled(false);
		group.delEmerald(event.getBlock());
		
	}
	
	@EventHandler
	public void tntIgnite(ExplosionPrimeEvent event) {
		if (event.getEntityType() != EntityType.PRIMED_TNT) return;
		
		
		tntIgnites.add(event.getEntity());
	}
	
	//TNT
	@EventHandler
	public void blockTNT(EntityExplodeEvent event) {
		List<Block> blockList = event.blockList();
		Block[] array = blockList.toArray(new Block[blockList.size()]);
		Tribe group;
	
		//handle creepers
               if (event.getEntityType().equals(EntityType.CREEPER) ||
			event.getEntityType().equals(EntityType.MINECART_TNT)) {
                       for (Block item : array) {
                               group = TribeProtect.getBlockOwnership(item.getLocation());
                               if (group.isValid()) {
                                       //Tribes.log("blocking creeper");
                                       blockList.remove(item);
                               }
                       }
                       return;
 
               }
		


		for (Entity tnt : tntIgnites.toArray(new Entity[tntIgnites.size()])) {
			if (event.getEntity().equals(tnt)) {
				//Tribes.log("ignite matched");
				for(Block item : array) {
					group = TribeProtect.getBlockOwnership(item.getLocation());
					
					if (group.isValid()) {
						//Tribes.log("blocking tnt");
						blockList.remove(item);
					}
					
				}
				
				return;
			}
		}
		
	}
	
	@EventHandler
	public void blockSpread(BlockSpreadEvent event) {
		Location source = event.getSource().getLocation();
		Tribe sourceGroup = TribeProtect.getBlockOwnership(source);
		Location spread = event.getBlock().getLocation();
		Tribe spreadGroup = TribeProtect.getBlockOwnership(spread);
		
		//if spreadgroup == null, allow
		//if sourcegroup == null, do not allow where spreadgroup != null

		if (!sourceGroup.isValid()) {
			if (!spreadGroup.isValid()) return;
			else event.setCancelled(true);
		} else {
			if (!spreadGroup.isValid()) return;
			else {
				if (sourceGroup != spreadGroup)
					event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void blockFlow(BlockFromToEvent event) {
		Location source = event.getBlock().getLocation();
		Tribe sourceGroup = TribeProtect.getBlockOwnership(source);
		Location spread = event.getToBlock().getLocation();
		Tribe spreadGroup = TribeProtect.getBlockOwnership(spread);
		
		//if spreadgroup == null, allow
		//if sourcegroup == null, do not allow where spreadgroup != null
		if (!sourceGroup.isValid()) {
			if (!spreadGroup.isValid()) return;
			else event.setCancelled(true);
		} else {
			if (!spreadGroup.isValid()) return;
			else {
				if (!sourceGroup.equals(spreadGroup))
					event.setCancelled(true);
			}
		}
	}
	
	//TODO
	//numerous other event checks
}
