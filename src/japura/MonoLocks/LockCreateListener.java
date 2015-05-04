/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.MonoLocks;


import java.lang.reflect.Array;
import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class LockCreateListener implements Listener{

	/*public LockCreateListener(JavaPlugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this,plugin);
	}*/
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSignChange(SignChangeEvent e) {
		Block sign = e.getBlock();
		
		//test if it isn't already locked
		if (!MonoLocks.isAllowed(e.getPlayer(),e.getBlock())) {
			e.setCancelled(true);
			return;
		}
		
		//check if it is near a chest or a door,
		//and if it is locked.
			
		Block chest = MonoLocks.findChest(sign);
		Block door = MonoLocks.findDoor(sign);
		Sign[] signs = new Sign[0];
		
		if (chest != null) {
			signs = MonoLocks.getSigns(chest);
		} else if (door != null) {
			signs = MonoLocks.getSigns(door);
		}
			
			
		Player[] players = MonoLocks.getAllowed(signs);
		String[] lines = e.getLines();
		if (lines[0].toLowerCase().contains("[private]") && players.length == 0) {
			//this is a lock sign
			
			//if there is only a chest or a door nearby
			if (chest != null ^ door != null) {
				sign.setType(Material.WALL_SIGN);
				BlockFace dir = MonoLocks.getDirection(sign);
				
				switch (dir) {
				case NORTH:
					sign.setData((byte) 3);
					break;
				case SOUTH:
					sign.setData((byte) 2);
					break;
				case EAST:
					sign.setData((byte) 4);
					break;
				case WEST:
					sign.setData((byte) 5);
					break;
				}
				
				//any sign correction code here
				//change the stuff in lines
				if (lines.length < 2) {
					lines = new String[2];
					lines[0] = "[private]";
				}
				
				//force the 2nd line to be the player's name
				//so they can't make a sign they can't break.
				lines[1] = e.getPlayer().getDisplayName();
				
				//TODO: find a safe way to change to a wallsign
				
				Sign signState = (Sign) sign.getState();
				
				for (int i = 0; i < lines.length; i++) {
					signState.setLine(i, lines[i]);
				}
				
				signState.update();
				
				
			}
			
		} else {
			
			//if players is empty, then there must be
			//no other locks on this chest.
			if (players.length == 0) return;
			
			if (!Arrays.asList(players).contains(e.getPlayer())) return;
			sign.setType(Material.WALL_SIGN);
			BlockFace dir = MonoLocks.getDirection(sign);
			
			switch (dir) {
			case NORTH:
				sign.setData((byte) 3);
				break;
			case SOUTH:
				sign.setData((byte) 2);
				break;
			case EAST:
				sign.setData((byte) 4);
				break;
			case WEST:
				sign.setData((byte) 5);
				break;
			}
			
			Sign signState = (Sign) sign.getState();
			
			for (int i = 0; i < lines.length; i++) {
				signState.setLine(i, lines[i]);
			}
			
			signState.update();
			MonoLocks.log(e.getPlayer().getDisplayName() + 
					" Has added a lock");
			
		}
	}
	
}
