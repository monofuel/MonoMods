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
	
	public TribeProtectListener(JavaPlugin plugin) {
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
		if (fromGroup != toGroup) {
			if (toGroup == null) {
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
		
		TribePlayer user = Tribes.getPlayer(event.getPlayer());
		//check if they are in a tribe faction
		if (user == null) {
			event.getPlayer().sendMessage("You are not in a tribe");
			return;
		}
		//check if we're in range of another emerald
		Location loc,corner1,corner2,corner3,corner4;
		Tribe userGroup,group1,group2,group3,group4;
		
		userGroup = user.getTribe();
		if (userGroup == null) {
			event.getPlayer().sendMessage("You are not in a tribe");
			return;
		}
		
		long claimSize = (long) Tribes.getConf().getConf("ClaimSize");
		loc = event.getBlock().getLocation();
		corner1 = loc.clone().add(claimSize,0, claimSize);
		corner2 = loc.clone().add(-claimSize,0,claimSize);
		corner3 = loc.clone().add(claimSize,0,-claimSize);
		corner4 = loc.clone().add(-claimSize,0,-claimSize);
		group1 = TribeProtect.getBlockOwnership(corner1);
		group2 = TribeProtect.getBlockOwnership(corner2);
		group3 = TribeProtect.getBlockOwnership(corner3);
		group4 = TribeProtect.getBlockOwnership(corner4);
		
		if ((group1 != null && group1 != userGroup) ||
			(group2 != null && group2 != userGroup) ||
			(group3 != null && group3 != userGroup) ||
			(group4 != null && group4 != userGroup)) {
			
			event.setCancelled(true);
			event.getPlayer().sendMessage("You're too close to another tribe");
			return;
		}
		
		user.getTribe().addEmerald(event.getBlock());
		event.getPlayer().sendMessage("You've claimed land for your tribe");
		
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void blockPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) return;
		Tribe group = TribeProtect.getBlockOwnership(event.getBlock().getLocation());
		TribePlayer user = Tribes.getPlayer(event.getPlayer());
		if (group != null) {
			if (user == null || user.getTribe() != group) {
				event.setCancelled(true);
				event.getPlayer().sendMessage("You are not allowed to build here");
			}
		}
		
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void blockPlace(PlayerBucketEmptyEvent event) {
		Tribe group = TribeProtect.getBlockOwnership(event.getBlockClicked().getRelative(event.getBlockFace()).getLocation());
		TribePlayer user = Tribes.getPlayer(event.getPlayer());
		if (group != null) {
			if (user == null || user.getTribe() != group) {
				event.setCancelled(true);
				event.getPlayer().sendMessage("You are not allowed to build here");
			}
		}
		
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void blockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) return;
		Tribe group = TribeProtect.getBlockOwnership(event.getBlock().getLocation());
		TribePlayer user = Tribes.getPlayer(event.getPlayer());
		if (group != null) {
			if (user == null || user.getTribe() != group) {
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
		
		Tribe group = TribeProtect.getBlockOwnership(event.getBlock().getLocation());
		
		if (group == null) return;
		if (event.isCancelled()) return;
		TribePlayer user = Tribes.getPlayer(event.getPlayer().getName());
		if (user.getTribe() != group) {
			event.getPlayer().sendMessage("You are not allowed to break here");
			event.setCancelled(true);
			return;
		}
		
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
               if (event.getEntityType().equals(EntityType.CREEPER)) {
                       for (Block item : array) {
                               group = TribeProtect.getBlockOwnership(item.getLocation());
                               if (group != null) {
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
					
					if (group != null) {
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

		if (sourceGroup == null) {
			if (spreadGroup == null) return;
			else event.setCancelled(true);
		} else {
			if (spreadGroup == null) return;
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
		if (sourceGroup == null) {
			if (spreadGroup == null) return;
			else event.setCancelled(true);
		} else {
			if (spreadGroup == null) return;
			else {
				if (sourceGroup != spreadGroup)
					event.setCancelled(true);
			}
		}
	}
	
	//TODO
	//numerous other event checks
}
