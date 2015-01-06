/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.MonoLocks;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MonoLocks extends JavaPlugin{
	
	private static Logger templateLogger = null;
	
	public void onEnable() {
		templateLogger = getLogger();

		saveDefaultConfig();
		
		new LockCreateListener(this);
		new LockAccessListener(this);
		new LockBreakListener(this);
		
		log("MonoLocks has been enabled");
		
	}
	
	public void onDisable() {
		
		
		log("MonoLocks has been disabled");
		templateLogger = null;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if ("monolocks".equalsIgnoreCase(cmd.getName()) &&
			(sender instanceof ConsoleCommandSender ||
			(sender.instanceof Player && ((Player) sender).hasPermission("monolocks.admin")))) {
			if (args[0].equalsIgnoreCase("reload")) {
				this.getServer().getPluginManager().disablePlugin(this);
				this.getServer().getPluginManager().enablePlugin(this);
				return true;
			} else if (args[0].equalsIgnoreCase("unload")) {
				this.getServer().getPluginManager().disablePlugin(this);
				return true;
			} else if (args[0].equalsIgnoreCase("load")) {
				//TODO stub
				return true;
			} else if (args[0].equalsIgnoreCase("save")) {
				//TODO stub
				return true;
			} else if (args[0].equalsIgnoreCase("help")) {
				//TODO help stuff
				String help = "Help stuff goes here";
				sender.sendMessage(help);
				
				return true;
			}

		}
		
		return false;
	}
	
	//let other objects call our logger
	public static void log(String line) {
		templateLogger.info(line);
	}
	
	/*
	 * tries to find a chest near a block (aka a sign)
	 * returns null if there is no chest, or if
	 * there are multiple chests and we are confused.
	 */
	public static Block findChest(Block sign) {
		Block chest = null;
		BlockFace[] directions = {BlockFace.NORTH,
								BlockFace.SOUTH,
								BlockFace.EAST,
								BlockFace.WEST};
		for (BlockFace face : directions) {
			if (sign.getRelative(face).getType().equals(Material.CHEST)) {
				if (chest == null)
					chest = sign.getRelative(face);
				else
					return null;
			}
		}

		
		return chest;
	}
	
	public static Block findDoor(Block sign) {
		Block door = null;
		BlockFace[] directions = {BlockFace.NORTH,
								BlockFace.SOUTH,
								BlockFace.EAST,
								BlockFace.WEST};
		for (BlockFace face : directions) {
			if (sign.getRelative(face).getType().equals(Material.WOODEN_DOOR) ||
					sign.getRelative(face).getType().equals(Material.IRON_DOOR)) {
				if (door == null)
					door = sign.getRelative(face);
				else
					return null;
			}
		}

		
		return door;
	}
	
	public static boolean isAllowed(Player player, Block item) {
		if (player == null) {
			MonoLocks.log("null player");
			return false;
		}
		
		Player[] list;
		if ( item.getType().equals(Material.CHEST)) {
			list = MonoLocks.getAllowed(MonoLocks.getSigns(item));
		} else if (item.getType().equals(Material.WOODEN_DOOR) ||
				item.getType().equals(Material.IRON_DOOR)) {
			list = MonoLocks.getAllowed(MonoLocks.getSigns(item));
		} else {
			//possible furnace or such in else statement?
			return true;
			
		}
		
		if (list.length == 0) {
			return true;
		}
		
		for (Player user : list) {
			if (user == null) continue;
			if (user.equals(player)) {
				return true;
			}
		}
		
		
		return false;
	}
	
	/*
	 * check all the blocks around a center
	 * for lock signs. returns a list of signs.
	 */
	public static Sign[] getSigns(Block center) {
		//TODO: check if doublechest, then return more signs.
		ArrayList<Sign> blocks = new ArrayList<Sign>();
		BlockFace[] directions = {BlockFace.NORTH,
				BlockFace.SOUTH,
				BlockFace.EAST,
				BlockFace.WEST};
		
		for (BlockFace face : directions) {
			Block check = center.getRelative(face);
			if(check.getType().equals(Material.WALL_SIGN))
				blocks.add((Sign) check.getState());
		}
		
		//check for if it is a double chest
		for (BlockFace face : directions) {
			Block check = center.getRelative(face);
			if(check.getType().equals(Material.CHEST)) {
				center = check;
				for (BlockFace face2 : directions) {
					check = center.getRelative(face2);
					if(check.getType().equals(Material.WALL_SIGN))
						blocks.add((Sign) check.getState());
				}
			}
		}
		
		
		if (blocks.size() >= 0)
			return blocks.toArray(new Sign[blocks.size()]);
		else
			return new Sign[0];
	}
	
	public static Player[] getAllowed(Sign[] signs) {
		ArrayList<Player> players = new ArrayList<Player>();
		boolean isPrivate = false;
		
		for (Sign cur : signs) {
			for (String line : cur.getLines()) {
				if (line.length() == 0) continue;
				if (line.equalsIgnoreCase("[private]")){
					isPrivate = true;
					continue;
				}
				players.add(Bukkit.getServer().getPlayer(line));
			}
			
		}
		if (isPrivate)
			return players.toArray(new Player[players.size()]);
		else
			return new Player[0];
	}
	
	/*
	 * get the direction the sign should face to 
	 * attach to a sign/chest
	 */
	public static BlockFace getDirection(Block sign) {
		
		BlockFace[] directions = {BlockFace.NORTH,
				BlockFace.SOUTH,
				BlockFace.EAST,
				BlockFace.WEST};
		for (BlockFace face : directions) {
			if (sign.getRelative(face).getType().equals(Material.WOODEN_DOOR) ||
					sign.getRelative(face).getType().equals(Material.IRON_DOOR) ||
					sign.getRelative(face).getType().equals(Material.CHEST))
					return face;
	
		}
		
		return null;
	}
}
