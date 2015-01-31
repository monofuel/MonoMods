/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.Tribes;

import com.mongodb.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.net.UnknownHostException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ItemStack;

import org.bukkit.util.ChatPaginator;
import org.bukkit.util.ChatPaginator.ChatPage;

public class Tribes extends JavaPlugin{
	

	private static Tribes tribesPlugin = null;

	private static MongoClient mongo = null;
	private static DB db = null;
	private static DBCollection tribeTable = null;
	private static DBCollection emeraldTable = null;
	private static DBCollection diamondTable = null;
	private static DBCollection kitTable = null;
	
	private static TribeProtect protector;
	private static LoginListener loginListener;
	private static TribeDisbandRunner disbander;
	private static TribeTeleportListener teleportListener;

	private static Long claimSize;
	private static boolean yAxisClaim;

	
	public void onEnable() {

		tribesPlugin = this;

		saveDefaultConfig();
		
		String mongoHost = getConfig().getString("mongo host");
		int port = getConfig().getInt("mongo port");
		String databaseName = getConfig().getString("mongo database");
		String tribeTableName = getConfig().getString("mongo tribe table");
		String emeraldTableName = getConfig().getString("mongo emerald table");
		String diamondTableName = getConfig().getString("mongo diamond table");
		String kitTableName = getConfig().getString("mongo kit table");

		try {
			mongo = new MongoClient(mongoHost,port);
		} catch (UnknownHostException e) {
			getLogger().log(Level.SEVERE,"Error connecting to database, bailing out",e);
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		}
		db = mongo.getDB(databaseName);
		tribeTable = db.getCollection(tribeTableName);
		emeraldTable = db.getCollection(emeraldTableName);
		diamondTable = db.getCollection(diamondTableName);
		kitTable = db.getCollection(kitTableName);
		
		//protector checks all emerald blocks
		//and starts listeners
		protector = new TribeProtect(this);
		disbander = new TribeDisbandRunner(this);
		//TODO listener is not actually set to run?
		//there's a method in it for removing diamonds that don't exist anymore
		teleportListener = new TribeTeleportListener(this);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this,protector,2000,2000);

		//trigger events on player login
		loginListener = new LoginListener(this);

		//TODO double check the disband stuff
		if (getConfig().getBoolean("Disband after time")) {
			Bukkit.getScheduler().scheduleSyncRepeatingTask(this,disbander,0,2400);
			log("Tribes will disband after " + 1000 * 60 * 60 * 24 * getConfig().getInt("Days before disband"));
		} else {
			log("Tribes will be lasting forever");
		}
		//set a spawn point
		if (getConfig().getBoolean("Tribe Spawn")) {

			TeleportData teleData = getttp("spawn",new Tribe("safezone"));
			if (teleData == null) {
				log("safezone does not have a teleport named spawn");
			} else {
				World world = teleData.getSpot().getWorld();
				int x = teleData.getSpot().getX();
				int y = teleData.getSpot().getY();
				int z = teleData.getSpot().getZ();
				world.setSpawnLocation(x,y+1,z);
				log("spawn set to safezone's spawn teleport");
			}
			
		} else {
			log("not using tribes to set world spawn");
		}

		//create the safezone tribe if it does not exit
		Tribe group = getTribe("safezone");
		if (!group.isValid()) {
			TribeFactory.createNewTribe("safezone");
			getLogger().info("safezone tribe created");
		}

		log("Tribes has been enabled");
	}
	
	public void onDisable() {
		
		
		mongo.close();
		protector.stop();

		HandlerList.unregisterAll(this);

		log("Tribes has been disabled");
	}


	/**
	 * Perform a check over all tribe data.
	 * verify that everything makes sense,
	 * and report problems.
	 */
	public boolean verifyTribes() {
		boolean result = true;
		DBCursor cursor = getTribeTable().find();
		DBObject group;

		//verify all tribes have a correct name
		while (cursor.hasNext()) {
			group = cursor.next();
			if (group.get("name") == null) {
				log("found a tribe with the name null");
				result = false;
			} else if (group.get("name") == "invalid tribe") {
				log("found an invalid tribe in db");
				result = false;
			}
		}


		//TODO
		//verify that safezone exists
		Tribe safezone = getTribe("safezone");
		if (!safezone.isValid()) {
			log("safezone does not exist!");
		}

		//check that no user is in multiple tribes
		ArrayList<String> users = new ArrayList<String>();
		for (String tribe : getTribeNames()) {
			Tribe myTribe = getTribe(tribe);
			for (String user : myTribe.getAll()) {
				if (users.contains(user)) {
					log("user: " + user + " is in multiple tribes");
				}
				users.add(user);
			}
		}

		return result;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {


		//TODO set up permissions with their actual commands
		if ("tadmin".equalsIgnoreCase(cmd.getName()) &&
		    (sender instanceof ConsoleCommandSender ||
		    (sender instanceof Player && ((Player) sender).hasPermission("tribes.admin")))){
			return tadmin(sender,cmd,label,args);
		}else if ("t".equalsIgnoreCase(cmd.getName()) ||
			  "tribes".equalsIgnoreCase(cmd.getName())) {
			return tcmd(sender,cmd,label,args);
		}else if ("ttp".equalsIgnoreCase(cmd.getName())) {
			return ttp(sender,cmd,label,args);
		}else if ("spawn".equalsIgnoreCase(cmd.getName())) {
			//TODO name of this teleport should be in the config
			return ttp(sender,cmd,label,new String[] {"spawn"});
		} else if ("kit".equalsIgnoreCase(cmd.getName())) {
			return kit(sender,cmd,label,args);
		}
		
		return false;
	}

	public boolean kit(CommandSender sender, Command cmd, String label, String[] args) {

		if (sender instanceof ConsoleCommandSender) {
			sender.sendMessage("silly console, kit is for players");
			return true;
		}else if (sender instanceof Player) {
			Player user = (Player) sender;
			PlayerInventory inv = user.getInventory();
			BasicDBObject query = new BasicDBObject();
			query.put("user",user.getName());

			DBObject userObject = kitTable.findOne(query);
			if (userObject == null) {
				inv.addItem(new ItemStack(Material.EMERALD_BLOCK,3));
				inv.addItem(new ItemStack(Material.DIAMOND_BLOCK,3));
				inv.addItem(new ItemStack(Material.IRON_HELMET,1));
				inv.addItem(new ItemStack(Material.IRON_CHESTPLATE,1));
				inv.addItem(new ItemStack(Material.IRON_LEGGINGS,1));
				inv.addItem(new ItemStack(Material.IRON_BOOTS,1));
				inv.addItem(new ItemStack(Material.IRON_SWORD,1));
				inv.addItem(new ItemStack(Material.IRON_PICKAXE,1));
				inv.addItem(new ItemStack(Material.IRON_PICKAXE,1));
				inv.addItem(new ItemStack(Material.IRON_PICKAXE,1));
				inv.addItem(new ItemStack(Material.TORCH,32));
				inv.addItem(new ItemStack(Material.SAPLING,8));
				inv.addItem(new ItemStack(Material.LOG,16));
				inv.addItem(new ItemStack(Material.BREAD,16));


				kitTable.insert(query);
				sender.sendMessage("the starter kit has been added to your inventory");

			} else {
				sender.sendMessage("you have already recieved your kit");
			}

			return true;
		} else {
			return false;
		}
	}

	public boolean ttp(CommandSender sender, Command cmd, String label, String[] args) {

                if (args.length == 0) return false;

                Tribe group;
                Player player;
		TeleportData teleData;
		String tpName;
		String otherTribeName;
		Tribe otherGroup;

                player = Bukkit.getPlayer(sender.getName());
		//group will be null if player is not in a tribe
		group = getPlayersTribe(player);

		switch (args[0].toLowerCase()) {
			case "help":
				sender.sendMessage("ttp can be used to modify teleport locations or to teleport.\n" +
						   "/ttp [name]\n" + 
						   "/ttp rename [old name] [new name]\n" +
						   "/ttp permit [name] [tribe]\n" +
						   "/ttp deny [name] [tribe]\n" +
						   "/ttp info [name]\n" + 
						   "/ttp list");
				return true;

			case "rename":
				//check if the player is in a tribe
				if (!group.isValid()) {
					sender.sendMessage("You are not in a tribe");
					return true;
				}
				//check if they are the leader of their tribe
				if (!group.getLeader().equals(player.getName())) {
					sender.sendMessage("You are not the leader of your tribe");
					return true;
				}
				//check if the command appears correct
				if (args.length != 3) {
					sender.sendMessage("you must specify an existing teleport and a new name. no spaces!");
					sender.sendMessage("EG /ttp rename oldname newname");
					return true;
				}
				//since the command is correct, parse out the old/new names
				String oldName = args[1];
				String newName = args[2];
				//fetch the old teleport object
				teleData = getttp(oldName,group);
				if (teleData == null) {
					sender.sendMessage("Teleport location " + oldName + " does not exist");
					return true;
				}
				//check if they own the teleporter
				if (!teleData.getOwner().getName().equals(group.getName())) {
					sender.sendMessage("You cannot rename another tribe's teleport");
					return true;
				}
				//check if the teleport already exists
				TeleportData newData = getttp(newName,group);
				if (newData != null && group.getName().equals(newData.getOwner().getName())) {
					sender.sendMessage("Teleport location " + newName + " already exists");
				} else {
					teleData.rename(newName);
					sender.sendMessage(oldName + " was renamed to " + newName + " successfully");
				}
			
				return true;

			case "permit":
				//check if they are in a tribe
				if (!group.isValid()) {
					sender.sendMessage("You are not in a tribe");
					return true;
				}

				//check if they are the leader of their tribe
				if (!group.getLeader().equals(player.getName())) {
					sender.sendMessage("You are not the leader of your tribe");
					return true;
				}
				//check if the command appears correct
				if (args.length != 3) {
					sender.sendMessage("You must specify an existing teleport and the naeme of the tribe to give permission");
					return true;
				}
				tpName = args[1];
				otherTribeName = args[2];
				teleData = getttp(tpName,group);
				if (teleData == null) {
					sender.sendMessage("Teleport location " + tpName + " does not exist");
					return true;
				}
				otherGroup = getTribe(otherTribeName);
				if (!otherGroup.isValid()) {
					sender.sendMessage("The tribe " + otherTribeName + " does not exist");
					return true;
				} else {
					teleData.addAllowed(otherGroup);
					sender.sendMessage("You have granted permission to " + otherTribeName + " to use the teleport location " + tpName);
					return true;
				}
			case "deny":
				//check if the player is in a tribe
				if (!group.isValid()) {
					sender.sendMessage("You are not in a tribe");
					return true;
				}
				//check if they are the leader of their tribe
				if (!group.getLeader().equals(player.getName())) {
					sender.sendMessage("You are not the leader of your tribe");
					return true;
				}

				if (args.length != 3) {
					sender.sendMessage("You must specify an existing teleport and the naeme of the tribe to deny permission");
					return true;
				}
				tpName = args[1];
				otherTribeName = args[2];
				teleData = getttp(tpName,group);
				if (teleData == null) {
					sender.sendMessage("Teleport location " + tpName + " does not exist");
					return true;
				}
				otherGroup = getTribe(otherTribeName);
				if (!otherGroup.isValid()) {
					sender.sendMessage("The tribe " + otherTribeName + " does not exist");
					return true;
				} else {
					teleData.rmAllowed(otherGroup);
					sender.sendMessage("You have removed permission to " + otherTribeName + " to use the teleport location " + tpName);
				}
				return true;
			case "info":
				if (args.length != 2) {
					sender.sendMessage("You must specify the name of a teleport");
					return true;
				}
				//TODO find more elegant way to handle safezone tp's for everyone
				if (!group.isValid()) {
					teleData = getttp(args[1],getTribe("safezone"));
				} else {
					teleData = getttp(args[1],group);
				}
				if (teleData == null) {
					sender.sendMessage("teleport does not exist");
					return true;
				}
				sender.sendMessage(teleData.getName() + " is owned by " + teleData.getOwner().getName());
				String[] allowed = teleData.getAllowed();
				String allowedTribes = allowed[0];
				for (int i = 1; i < allowed.length; i++) {
					allowedTribes += "," + allowed[i];
				}
				sender.sendMessage("The tribes that may use this are: " + allowedTribes);
	
				return true;
			case "list":

				String teleportNames = "";
				if (group.isValid()) {
					teleportNames += "from your tribe: ";
					Block[] diamonds = group.getDiamonds();
					for (Block item : diamonds) {
						teleData = group.getTeleData(item);
						teleportNames += " " + teleData.getName();
					}
					teleportNames += "\n";
				}

				for (String checkGroup : getTribeNames()) {
					if (checkGroup.equals(group.getName())) continue;
					otherGroup = getTribe(checkGroup);
					
					String teleports = "";
					Block[] diamonds = otherGroup.getDiamonds();
					for (Block item : diamonds) {
						teleData = otherGroup.getTeleData(item);
						if (teleData.isAllowed(group)) {
							teleports += " " +  teleData.getName();	
						}
					}
					if (!teleports.equals("")) {
						teleportNames += "from the tribe " + otherGroup.getName() + ": " +
								teleports + "\n";
					}
				}
				int myPage;
				if (args.length < 2) {
					myPage = 1;
				} else {
					try {
						myPage = Integer.parseInt(args[1]);
					} catch (NumberFormatException e) {
						return false;
					}
				}

				ChatPage page = ChatPaginator.paginate(teleportNames,myPage);

				sender.sendMessage("You are allowed to use the following teleports (page " + page.getPageNumber() + " of " + page.getTotalPages() + "): ");

				for (String line : page.getLines()) {
					sender.sendMessage(line);
				}

				return true;
			default:
				//teleport to location and return true
				//TODO find more elegant way to handle safezone tp's for everyone
				if (!group.isValid()) {
					group = getTribe("safezone");
				}
				if (args.length != 1) {
					sender.sendMessage("you must specify an existing teleport.");
					return true;
				}
			
				teleData = getttp(args[0],group);
				if (teleData == null) {
					sender.sendMessage("Teleport location " + args[0] + " does not exist");
					return true;
				}
				teleData.teleportPlayer(player);
				return true;
		}
		//return false;

	}

	public TeleportData getttp(String name, Tribe group) {
		//first search for a teleport in our tribe
		TeleportData teleData;
		Block[] diamonds;
		if (group.isValid()) {
			diamonds = group.getDiamonds();
			for (Block item : diamonds) {
				teleData = group.getTeleData(item);
				if (teleData == null) {
					log("error in looking up a tribe teleport location");
				}
				if (teleData.getName().equalsIgnoreCase(name))
					return teleData;
			}
		}
		//if our tribe does not have this point, see if another tribe does
		//and if they have allowed us to use it
		DBCursor cursor = getTribes();
		Tribe otherGroup;
		while (cursor.hasNext()) {
			otherGroup = getTribe((String) cursor.next().get("name"));
			//don't re-scan the same group
			if (otherGroup.equals(group)) continue;

			diamonds = otherGroup.getDiamonds();
			if (diamonds == null) continue;
			for (Block item : diamonds) {
				teleData = otherGroup.getTeleData(item);
				if (teleData == null) {
					log("found null teleData somehow?");
					return null;
				}
				//allow anyone to use safezone spots
				if (teleData.isAllowed(group) || otherGroup.getName().equalsIgnoreCase("safezone")) {
					if (teleData.getName().equalsIgnoreCase(name))
						return teleData;
				}
					
			}
		}
		return null;

	}

	//should be split into separate commands, probably in another class
	public boolean tadmin(CommandSender sender, Command cmd, String label, String[] args) {
		//TODO needs to be converted to newer format for console/admins
		//and converted to a switch statement
		if (args.length < 1) return false;
		if (args[0].equalsIgnoreCase("reload")) {
			this.getServer().getPluginManager().disablePlugin(this);
			this.getServer().getPluginManager().enablePlugin(this);
			return true;
		} else if (args[0].equalsIgnoreCase("unload")) {
			this.getServer().getPluginManager().disablePlugin(this);
			return true;
		} else if (args[0].equalsIgnoreCase("load")) {
			reloadConfig();
			return true;
		} else if (args[0].equalsIgnoreCase("verify")) {
			if (verifyTribes()) {
				sender.sendMessage("Tribes successfully validated");
			} else {
				sender.sendMessage("Tribes detected errors, review console output");
			}
			return true;
		} else if (args[0].equalsIgnoreCase("save")) {
			saveConfig();
			return true;
		} else if (args[0].equalsIgnoreCase("list")) {
			//TODO stub
			//use paginator n shit
		} else if (args[0].equalsIgnoreCase("put")) {
			if (args.length != 3) {
				sender.sendMessage("/tadmin put player tribe");
				return true;
			}
			String user = args[1];
			String tribeName = args[2];
			Tribe tribe = getTribe(tribeName);
			sender.sendMessage("placing " + user + " into " + tribeName);
			if (!tribe.isValid()) {
				sender.sendMessage("tribe doesn't exist");
			} else {
				tribe.addPlayer(user);
				sender.sendMessage("success");
			}
			return true;
		} else if (args[0].equalsIgnoreCase("remove")) {
			if (args.length != 3) {
				sender.sendMessage("/tadmin remove player tribe");
				return true;
			}
			String user = args[1];
			String tribeName = args[2];
			Tribe tribe = getTribe(tribeName);
			if (!tribe.isValid()) {
				sender.sendMessage("tribe doesn't exist");
			} else {
				
				tribe.delPlayer(user);
				if (user.equals(tribe.getLeader())) {
					tribe.setLeader("");
				}
				sender.sendMessage("successfully removed " + user + " from tribe " + tribeName);
			}
				
			
			return true;
		} else if (args[0].equalsIgnoreCase("leader")) {
			if (args.length != 3) {
				sender.sendMessage("/tadmin leader player tribe");
				return true;
			}
			String user = args[1];
			String tribeName = args[2];
			Tribe tribe = getTribe(tribeName);
			Tribe playersTribe = getPlayersTribe(user);
			if (!tribe.isValid()) {
				sender.sendMessage("tribe doesn't exist");
			} else {
				if (playersTribe.isValid()) {
					sender.sendMessage("removing " + user + " from tribe " + playersTribe.getName());
					if (!tribe.equals(playersTribe) && user.equalsIgnoreCase(playersTribe.getLeader())) {
						sender.sendMessage("setting tribe " + playersTribe.getName() + " leader to invalid leader");
						playersTribe.setLeader("invalid leader");
					}
					playersTribe.delPlayer(user);
					
				}
				sender.sendMessage("placing " + user + " as leader of " + tribeName);
				tribe.setLeader(user);
				sender.sendMessage("success");
			}
				
			
			
			return true;
		}else if (args[0].equalsIgnoreCase("rename")) {
			if (args.length != 3) {
				sender.sendMessage("/tadmin rename oldname newname");
				return true;
			}
			Tribe tribe = getTribe(args[1]);
			if (!tribe.isValid()) {
				sender.sendMessage("tribe doesn't exist");
			}

			Tribe newTribe = getTribe(args[2]);
			if (newTribe.isValid()) {
				sender.sendMessage("tribe " + args[2] + " already exists!");
				return true;
			}
			tribe.setName(args[2]);
			return true;
		} else if (args[0].equalsIgnoreCase("help")) {
			//TODO update help
			String help = "/tadmin reload will reload the config\n" +
						"/tadmin unload will unload the plugin\n" +
						"/tadmin load will load config changes\n" +
						"/tadmin save will save settings out to config";
			sender.sendMessage(help);
			return true;
		}
		return false;
		
	}
	

	//TODO: should be split into separate commands, probably in another class.
	public boolean tcmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) return false;
		String user = sender.getName();
		Tribe group;
		Player player;
		
		player = Bukkit.getPlayer(user);
		group = getPlayersTribe(player);
		
		if (args[0].equalsIgnoreCase("create")) {

			if (group.isValid()) {
				sender.sendMessage("you are already in a tribe. Please leave first");
				if (user.equals(group.getLeader())) {
					sender.sendMessage("You will have to either disband or pass on leadership");
				}
				return true;
			}

			if (args.length < 2){
				sender.sendMessage("Please specify a tribe name");
				return true;
			}
			if (args.length > 2) {
				sender.sendMessage("Spaces are not allowed in a tribe name");
				return true;
			}
			String name = args[1];
			if (getTribe(name).isValid()) {
				sender.sendMessage("Tribe already exists");
				return true;
			}
			
			TribeFactory.createNewTribe(name,sender.getName());
			sender.sendMessage("Tribe " + name + " created");
			return true;
			
		} else if (args[0].equalsIgnoreCase("destroy")) {
			if (args.length < 2) {
				sender.sendMessage("You need to specify your tribe name");
				return true;
			}
			
			group = getTribe(args[1]);
			
			if (!group.isValid()) {
				sender.sendMessage("You are not in a tribe");
				return true;
			} else if (!user.equals(group.getLeader())) {
				sender.sendMessage("You are not leader of that tribe");
				return true;
			}
			
			if (user.equals(group.getLeader())) {
				group.destroy();
				sender.sendMessage("Your tribe has been destroyed");
				return true;
			} else {
				sender.sendMessage("You are not the leader of this tribe");
				return true;
			}
			
		} else if (args[0].equalsIgnoreCase("invite")) {
			if (args.length < 2) {
				sender.sendMessage("You need to specify who to invite");
				return true;
			}
			
			if (!group.isValid()) {
				sender.sendMessage("You are not in a tribe");
				return true;
			}
			String invited = args[1];
			if (group.hasPlayer(invited)) {
				sender.sendMessage("they are already a member of your tribe");
				return true;
			}

			if (user.equals(group.getLeader())) {
				group.invite(invited);
				sender.sendMessage(invited + " invited successfully");

				Player newUser = Bukkit.getPlayer(invited);
				if (newUser != null) {
					newUser.sendMessage("You have been invited to " + group.getName());
					newUser.sendMessage("if you want to join, use /t join " + group.getName());
				}

				return true;
			} else {
				sender.sendMessage("You are not leader of this tribe");
				return true;
			}
			
		} else if (args[0].equalsIgnoreCase("uninvite")) {

			if (args.length < 2) {
				sender.sendMessage("You must specify who to uninvite");
				return true;
			}
			if (!group.isValid()) {
				sender.sendMessage("You are not in a tribe");
				return true;
			}

			if (!user.equals(group.getLeader())) {
				sender.sendMessage("you are not leader of your tribe");
				return true;
			}

			String uninvited = args[1].toLowerCase();
			if (group.isInvited(uninvited)) {
				group.unInvite(uninvited);
				return true;
			} else {
				sender.sendMessage("they are not invited");
				return true;
			}
		} else if (args[0].equalsIgnoreCase("join")) {
			
			if (args.length < 2) {
				sender.sendMessage("You need to specify the tribe to join");
				return true;
			}
			
			if (group.isValid()) {
				if (user.equals(group.getLeader())) {
					sender.sendMessage("You cannot leave without transfering leadership");
					return true;
				}
			}
			Tribe newGroup = getTribe(args[1].toLowerCase());
			if (!newGroup.isValid()) {
				sender.sendMessage("invalid tribe name");
				return true;
			}

			if (newGroup.isInvited(user)) {
				if (group.isValid()) {
					group.delPlayer(user);
				}
				newGroup.addPlayer(user);
				sender.sendMessage("you have joined " + newGroup.getName());
				return true;
			} else {
				sender.sendMessage("you are not invited to this tribe");
				return true;
			}
		} else if (args[0].equalsIgnoreCase("kick")) {
			if (!group.isValid()) {
				sender.sendMessage("You are not in a tribe");
				return true;
			}
			
			if (!user.equals(group.getLeader())) {
				sender.sendMessage("You are not leader of this tribe");
				return true;
			}
			if (args.length < 2) {
				sender.sendMessage("You need to specify the player to kick");
				return true;
			}
			String kicked = args[1];
			if (!group.hasPlayer(args[1])) {
				sender.sendMessage("Player is not in your tribe");
				return true;
			}
			
			group.delPlayer(kicked);
			sender.sendMessage(kicked + " has been kicked");
			Player kickedPlayer = Bukkit.getServer().getPlayer(kicked);
			if (kickedPlayer != null) {
				kickedPlayer.sendMessage("You have been kicked from your tribe!");
			}
			return true;
		
		} else if (args[0].equalsIgnoreCase("leave")) {
			if (!group.isValid()) {
				sender.sendMessage("You are not in a tribe");
				return true;
			}
			
			if (user.equals(group.getLeader())) {
				sender.sendMessage("You cannot leave without transfering leadership");
				return true;
			}
			
			group.delPlayer(user);
			return true;
			
		} else if (args[0].equalsIgnoreCase("giveLeader")) {
			if (args.length < 2) {
				sender.sendMessage("You need to specify who to give leadership to");
				return true;
			}
			String otherPlayer = args[1];
			if (group == null) {
				sender.sendMessage("You are not in a tribe");
				return true;
			}
			if (otherPlayer == null) {
				sender.sendMessage("They are not in your tribe");
				return true;
			}
			if (user.equals(group.getLeader())) {
				group.setLeader(otherPlayer);
				sender.sendMessage("leadership transfered");
				//TODO send the other player a message that leadership is transferred
				return true;
			} else {
				sender.sendMessage("You are not leader of your tribe");
			}
			
		} else if (args[0].equalsIgnoreCase("show")) {
			if (args.length < 2) {
				sender.sendMessage("You need to specify a faction or player to show");
				return true;
			}

			DBCursor cursor = Tribes.getTribes();
			String name;
			while (cursor.hasNext()) {
				name = (String) cursor.next().get("name");
				group = Tribes.getTribe(name);
				if (group.hasPlayer(args[1])) {
					sender.sendMessage(group.toString());
					return true;
				}
			}

			group = getTribe(args[1]);
			if (group != null) {
				sender.sendMessage(group.toString()); 
				return true;
			}
			String otherUser = args[1];
			Tribe otherGroup = getPlayersTribe(otherUser);
			if (otherGroup != null) {
				sender.sendMessage(otherUser + " is in the tribe " + otherGroup.getName());
				return true;
			} else {
				sender.sendMessage("User or tribe does not exist");
				return true;
			}
				
		} else if (args[0].equalsIgnoreCase("help")) {
			return thelp(sender,args);
			
		}
		return false;
	}
	
	public boolean thelp(CommandSender sender, String[] args) {
		int myPage;
		if (args.length < 2) myPage = 1;
		else {
			try {
				myPage = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				myPage = 1;
			}
		}
		String message;
			message = "/kit for a starter kit of gear\n" +
				  "/spawn to teleport to the spawn point, or to your tribe's 'spawn' teleport\n" + 
				  "/ttp help for help with ttp and teleporting\n" +
				  "/t create [tribe] can be used to create a tribe\n" +
				  "/t destroy [yourtribe] can be used to dissolve your tribe\n" +
				  "/t invite [player] to invite them to your tribe\n" +
				  "/t uninvite [player] to remove them from your invitese\n" +
				  "/t join [tribe] to join a tribe you have been invited to\n" +
				  "/t kick [player] to kick someone from your tribe\n" +
				  "/t leave to leave your tribe (you cannot be the leader)\n" +
				  "/t giveLeader [player] to transfer your leadership to another player\n" +
				  "/t show [tribe] show information about a tribe\n" +
				  "/t show [player] show what tribe a player belongs to\n" +
				  "/t help shows this help information";
		ChatPage page = ChatPaginator.paginate(message,myPage);
		sender.sendMessage("Tribes help (page " + page.getPageNumber() + " of " + page.getTotalPages() + "): ");
		for (String line : page.getLines()) {
			sender.sendMessage(line);
		}

		return true;
	}
	
	
	public static void addTribe(Tribe group) {
		/*if (!groups.contains(group))
			groups.add(group);
		*/ //TODO
		Tribes.log("STUB add Tribe method called");
	}
	
	//TODO: move to top of file
	private static HashMap<String,Tribe> tribeCache = new HashMap<String,Tribe>();
	/*
	 * the getTribe function is alot simpler than it used to,
	 * you could really just call a new tribe object instead.
	 * Tribe objects are just a layer above the database,
	 * and the TribeFactory is for the creation of new tribes.
	 *
	 */
	public static Tribe getTribe(String name) {
		Tribe cacheCheck = tribeCache.get(name.toLowerCase());
		if (cacheCheck == null) {
			log("cache misssed, loading " + name);
			cacheCheck = new Tribe(name.toLowerCase());
			tribeCache.put(name.toLowerCase(),cacheCheck);
		}
		return cacheCheck;
	}

	public static void rmTribeCache(String name) {
		tribeCache.remove(name);
	}
	
	public static DBCursor getTribes() {
		DBCursor cursor = tribeTable.find();
		return cursor;
	}
	/*
	public static void addPlayer(TribePlayer user) {
		/*
		if (!users.contains(user))
			users.add(user);
		 //TODO
		 //i think this is safe to delete
	}
	
	public static void delPlayer(TribePlayer user) {
		//users.remove(user);
		//TODO
		 //i think this is safe to delete
		
	}*/

	//TODO move to top of file
	private static HashMap<String,Tribe> playerCache = new HashMap<String,Tribe>();

	public static void invalidatePlayer(String name) {
		log("invalidating " + name + "'s cache");
		playerCache.remove(name.toLowerCase());
	}

	public static Tribe getPlayersTribe(String name) {
		//garbage in garbage out
		if (name == null) return new Tribe("invalid tribe");

		Tribe group = playerCache.get(name.toLowerCase());
		if (group != null) return group;
		log("missed player " + name + " in cache");

		for (String tribeName : getTribeNames()) {
			group = getTribe(tribeName);

			if (group.hasPlayer(name)) {
				log("adding " + name + " back to cache");
				playerCache.put(name.toLowerCase(),group);
				return group;
			}
		}
		return new Tribe("invalid tribe");

	}

	public static Tribe getPlayersTribe(Player user) {
		//garbage in garbage out
		if (user == null) return new Tribe("invalid tribe");
		return getPlayersTribe(user.getName());
		
	}
	
	public static DBCollection getTribeTable() {
		return tribeTable;
	}

	private static String[] tribeNameCache;

	public static void invalidateTribeNames() {
		log("invalidating tribe names");
		tribeNameCache = null;
	}

	public static String[] getTribeNames() {
		if (tribeNameCache == null) {
			log("rebuilding tribe name cache");
			DBCursor cursor = Tribes.getTribes();
			Tribe group;
			tribeNameCache = new String[cursor.count()];
			int index = 0;
			while (cursor.hasNext()) {
				tribeNameCache[index++] = (String) cursor.next().get("name");
			}
		}

		return tribeNameCache;
	}
	
	public static DBCollection getEmeraldTable() {
		return emeraldTable;
	}
	public static DBCollection getDiamondTable() {
		return diamondTable;
	}

	//Messy way to get the instance of tribes plugin
	//for access to config and logger for
	//any and all objects.
	public static Tribes getPlugin() {
		return tribesPlugin;
	}

	//let other objects call our logger
	public static void log(String line) {
		Tribes.getPlugin().getLogger().info(line);
	}

}
