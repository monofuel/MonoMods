/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.Tribes;

import com.mongodb.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import java.net.UnknownHostException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Tribes extends JavaPlugin{
	
	//TODO: do we really need all these static values?

	//TODO: perhaps rename this lowercase t?
	private static Logger TribeLogger = null;
	private static MongoClient mongo = null;
	private static DB db = null;
	private static DBCollection tribeTable = null;
	private static DBCollection emeraldTable = null;
	private static DBCollection diamondTable = null;
	
	private static TribeProtect protector;
	private static LoginListener loginListener;
	private static TribeDisbandRunner disbander;
	private static TribeTeleportListener teleportListener;

	private static Long claimSize;
	private static boolean yAxisClaim;
	
	public void onEnable() {

		TribeLogger = getLogger();
		saveDefaultConfig();

		claimSize = getConfig().getLong("ClaimSize");
		yAxisClaim = getConfig().getBoolean("YAxisClaim");
		
		//TODO add hostname and port to config
		//add database name or prefix to config
		try {
			mongo = new MongoClient("localhost",27017);
		} catch (UnknownHostException e) {
			//TODO handle this error properly
			e.printStackTrace();
		}
		db = mongo.getDB("MonoMods");
		tribeTable = db.getCollection("Tribes");
		tribeTable = db.getCollection("Emeralds");
		tribeTable = db.getCollection("Diamonds");
		
		//protector checks all emerald blocks
		//and starts listeners
		assert protector == null;
		protector = new TribeProtect(this);
		disbander = new TribeDisbandRunner(this);
		teleportListener = new TribeTeleportListener(this);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this,protector,200,200);

		//trigger events on player login
		loginListener = new LoginListener(this);

		//TODO double check the disband stuff
		if (getConfig().getBoolean("Disband after time"))
			Bukkit.getScheduler().scheduleSyncRepeatingTask(this,disbander,0,2400);
		else
			log("Tribes will be lasting forever");
		
		//set a spawn point
		if (getConfig().getBoolean("Tribe Spawn")) {
			
			//TODO: why must i do these crazy casts
			int x = getConfig().getInt("SpawnX");
			int y = getConfig().getInt("SpawnY");
			int z = getConfig().getInt("SpawnZ");
			String worldName = getConfig().getString("World");
			//TODO:should set a config option for this
			World world = Bukkit.getWorld(worldName);
			if (world == null) {
				log("invalid world!");
			} else {
				log("spawn set to " + x + "," + y + "," + z);
				world.setSpawnLocation(x,y,z);
			}
			
		} else {
			log("not using tribes to set world spawn");
		}

		//TODO:teleport listener

		//TODO: autosave runnable

		//create the safezone if it does not exit
		//TODO: find more appropriate place for this
		Tribe group = getTribe("safezone");
		if (group == null) {
			TribeFactory.createNewTribe("safezone");
			log("safezone tribe created");
		}

		log("Tribes has been enabled");
	}
	
	public void onDisable() {
		
		//TODO verify all variables are cleared out
		//config
		//data
		//configLoc
		//groups
		//users
		//protector
		
		//TODO close DB connection?
		saveConfig();

		protector.stop();

		log("Tribes has been disabled");
	}


	/**
	 * Perform a check over all tribe data.
	 * verify that everything makes sense,
	 * and report problems.
	 */
	public boolean verifyTribes() {
		boolean result = true;

		//TODO: update this for mongo
		/*
		//sanity check tribes and their leaders
		for (Tribe group : groups) {
			if (group.getName().equals("safezone")) continue;
			if (group == null) {
				log("somehow group null was created?");
				result = false;
				continue;
			}

			TribePlayer leader = group.getLeader();
			if (leader == null) {
				log("tribe " + group.getName() + "'s leader is null");
				result = false;
				continue;
			}
			if (leader.getTribe() != group) {
				if (leader.getTribe() == null) {
					log("player " + leader.getPlayer() + " is a leader, but believes it leads null");
					result = false;
					continue;
				}
				log("player " + leader.getPlayer() + " does not think it leads " + group.getName());
				result = false;
			}
		}
		log("---- GROUP CHECK COMPLETE ----");
		for (TribePlayer user : users) {
			if (user == null) {
				log("somehow user null was created?");
				result = false;
				continue;
			}

			Tribe group = user.getTribe();
			//if group is null, then the user is not in a tribe.
			if (group == null || group.getName().equals("safezone")) continue;
			if (!group.hasPlayer(user)) {
				log("tribe " + group.getName() + " does not think it has player " + user.getPlayer());
				result = false;
			}
		}
		log("---- USER CHECK COMPLETE ----");
		*/
		log("STUB");
		return result;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (cmd.getName().equalsIgnoreCase("tadmin")) {
			return tadmin(sender,cmd,label,args);
		}else if (cmd.getName().equalsIgnoreCase("t") ||
				  cmd.getName().equalsIgnoreCase("tribes")) {
			return tcmd(sender,cmd,label,args);
		}else if (cmd.getName().equalsIgnoreCase("ttp")) {
			return ttp(sender,cmd,label,args);
		}
		
		return false;
	}

	public boolean ttp(CommandSender sender, Command cmd, String label, String[] args) {
                if (args.length == 0) return false;
                Tribe group;
                Player player;
                TribePlayer tPlayer;

                player = Bukkit.getPlayer(sender.getName());
                tPlayer = getPlayer(player);

		if (args[0].equalsIgnoreCase("help")) {
			sender.sendMessage("ttp can be used to modify teleport locations or to teleport.\n" +
					   "/ttp [name]\n" + 
					   "/ttp rename [old name] [new name]\n" +
					   "/ttp permit [name] [tribe]\n" +
					   "/ttp deny [name] [tribe]\n" +
					   "/ttp info [name]\n" + 
					   "/ttp list");
			return true;

		} else if (args[0].equalsIgnoreCase("rename")) {
			if ((tPlayer == null) || (tPlayer.getTribe() == null)) {
				sender.sendMessage("You are not in a tribe");
				return true;
			}
			group = tPlayer.getTribe();
			if (args.length != 3) {
				sender.sendMessage("you must specify an existing teleport and a new name. no spaces!");
				return true;
			}
			
			TeleportData teleData = getttp(args[1],group);
			if (teleData == null) {
				sender.sendMessage("Teleport location " + args[1] + " does not exist");
				return true;
			}
			if (teleData.getOwner() != group) {
				sender.sendMessage("You cannot rename another tribe's teleport");
				return true;
			}


			if (getttp(args[2],group) != null) {
				sender.sendMessage("Teleport location " + args[2] + " already exists");
			} else {
				teleData.rename(args[2]);
				sender.sendMessage(args[1] + " was renamed to " + args[2] + " successfully");
			}
			
			return true;
		} else if (args[0].equalsIgnoreCase("permit")) {
			if (tPlayer == null || tPlayer.getTribe() == null) {
				sender.sendMessage("You are not in a tribe");
				return true;
			}
			group = tPlayer.getTribe();


			if (tPlayer != group.getLeader()) {
				sender.sendMessage("You are not the leader of your tribe");
				return true;
			}
			if (args.length != 3) {
				sender.sendMessage("You must specify an existing teleport and the naeme of the tribe to give permission");
				return true;
			}
			TeleportData teleData = getttp(args[1],group);
			if (teleData == null) {
				sender.sendMessage("Teleport location " + args[1] + " does not exist");
				return true;
			}
			Tribe otherGroup = getTribe(args[2]);
			if (otherGroup  == null) {
				sender.sendMessage("The tribe " + args[2] + " does not exist");
				return true;
			} else {
				teleData.addAllowed(otherGroup);
				sender.sendMessage("You have granted permission to " + args[2] + " to use the teleport location " + args[1]);
				return true;
			}
		} else if (args[0].equalsIgnoreCase("deny")) {
			if (tPlayer == null || tPlayer.getTribe() == null) {
				sender.sendMessage("You are not in a tribe");
				return true;
			}
			group = tPlayer.getTribe();

			if (tPlayer != group.getLeader()) {
				sender.sendMessage("You are not the leader of your tribe");
				return true;
			}
			if (args.length != 3) {
				sender.sendMessage("You must specify an existing teleport and the naeme of the tribe to deny permission");
				return true;
			}
			TeleportData teleData = getttp(args[1],group);
			if (teleData == null) {
				sender.sendMessage("Teleport location " + args[1] + " does not exist");
				return true;
			}
			Tribe otherGroup = getTribe(args[2]);
			if (otherGroup  == null) {
				sender.sendMessage("The tribe " + args[2] + " does not exist");
				return true;
			} else {
				teleData.rmAllowed(otherGroup);
				sender.sendMessage("You have removed permission to " + args[2] + " to use the teleport location " + args[1]);
			}
			return true;
		} else if (args[0].equalsIgnoreCase("info")) {
			if (args.length != 2) {
				sender.sendMessage("You must specify the name of a teleport");
				return true;
			}
			TeleportData teleData;
			if (tPlayer == null || tPlayer.getTribe() == null) {
				teleData = getttp(args[1],getTribe("safezone"));
			} else {
				group = tPlayer.getTribe();
				teleData = getttp(args[1],group);
			}
			if (teleData == null) {
				sender.sendMessage("teleport does not exist");
				return true;
			}
			sender.sendMessage(teleData.getName() + " is owned by " + teleData.getOwner().getName());
			Tribe[] allowed = teleData.getAllowed();
			String allowedTribes = allowed[0].getName();
			for (int i = 1; i < allowed.length; i++) {
				allowedTribes += "," + allowed[i].getName();
			}
			sender.sendMessage("The tribes that may use this are: " + allowedTribes);
	


			return true;
		} else if (args[0].equalsIgnoreCase("list")) {

			sender.sendMessage("You are allowed to use the following teleports:");
			String teleportNames = "from the tribe safezone: ";
			Tribe safeTribe = getTribe("safezone");
			TeleportData teleData;
			
			
			Block[] diamonds = safeTribe.getDiamonds();
			for (Block item : diamonds) {
				teleData = safeTribe.getTeleData(item);
				if (teleData == null) {
					log("error in looking up a tribe teleport location");
				}
				teleportNames += " " +  teleData.getName();	
			}
			sender.sendMessage(teleportNames);
			sender.sendMessage("command WIP: only shows safezone's teleports");
			//TODO add listing for all allowed teleports from the user

			return true;
		} else {
			//teleport to location and return true
			
			if ((tPlayer == null) || (tPlayer.getTribe() == null)) {
				group = getTribe("safezone");
			} else {
				group = tPlayer.getTribe();
			}
			if (args.length != 1) {
				sender.sendMessage("you must specify an existing teleport.");
				return true;
			}
			
			TeleportData teleData = getttp(args[0],group);
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
		Block[] diamonds = group.getDiamonds();
		for (Block item : diamonds) {
			teleData = group.getTeleData(item);
			if (teleData == null) {
				log("error in looking up a tribe teleport location");
			}
			if (teleData.getName().equalsIgnoreCase(name))
				return teleData;
		}

		//if our tribe does not have this point, see if another tribe does
		//and if they have allowed us to use it
		for (Tribe otherGroup : getTribes()) {
			//don't re-scan the same group
			if (otherGroup == group) continue;

			diamonds = otherGroup.getDiamonds();
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
			StringBuilder list = new StringBuilder();
			
			for (Tribe tribe : getTribes()) {
				list.append(tribe.getName() + ",");
			}
			sender.sendMessage(list.toString());
			
			return true;
		} else if (args[0].equalsIgnoreCase("put")) {
			if (args.length != 3) {
				sender.sendMessage("/tadmin put player tribe");
				return true;
			}
			TribePlayer tPlayer = getPlayer(args[1]);
			Tribe tribe = getTribe(args[2]);
			sender.sendMessage("placing " + args[1] + " into " + args[2]);
			if (tPlayer == null) {
				tPlayer = TribePlayerFactory.createNewPlayer(args[1]);
			}
			if (tribe == null) {
				sender.sendMessage("tribe doesn't exist");
			} else {
				
				tribe.addPlayer(tPlayer);
				sender.sendMessage("success");
			}
				
			
			
			return true;
		} else if (args[0].equalsIgnoreCase("remove")) {
			if (args.length != 3) {
				sender.sendMessage("/tadmin remove player tribe");
				return true;
			}
			TribePlayer tPlayer = getPlayer(args[1]);
			Tribe tribe = getTribe(args[2]);
			if (tPlayer == null) {
				sender.sendMessage("player is not in a tribe");
			} else {
				if (tribe == null) {
					sender.sendMessage("tribe doesn't exist");
				} else {
					
					tribe.delPlayer(tPlayer);
					if (tribe.getLeader() == tPlayer) {
						tribe.setLeader("");
					}
					sender.sendMessage("success");
				}
				
			}
			
			return true;
		} else if (args[0].equalsIgnoreCase("leader")) {
			if (args.length != 3) {
				sender.sendMessage("/tadmin leader player tribe");
				return true;
			}
			TribePlayer tPlayer = getPlayer(args[1]);
			Tribe tribe = getTribe(args[2]);
			sender.sendMessage("placing " + args[1] + " as leader of " + args[2]);
			if (tPlayer == null) {
				tPlayer = TribePlayerFactory.createNewPlayer(args[1]);
			}
			if (tribe == null) {
				sender.sendMessage("tribe doesn't exist");
			} else {
				tribe.addPlayer(tPlayer);
				tribe.setLeader(tPlayer.getPlayer());
				tPlayer.setTribe(tribe);
				sender.sendMessage("success");
			}
				
			
			
			return true;
		}else if (args[0].equalsIgnoreCase("rename")) {
			if (args.length != 3) {
				sender.sendMessage("/tadmin rename oldname newname");
				return true;
			}
			Tribe tribe = getTribe(args[1]);
			if (tribe == null) {
				sender.sendMessage("tribe doesn't exist");
			} else {
				tribe.setName(args[2]);
			}
			return true;
		} else if (args[0].equalsIgnoreCase("help")) {
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
		Tribe group;
		Player player;
		TribePlayer tPlayer;
		
		player = Bukkit.getPlayer(sender.getName());
		tPlayer = getPlayer(player);
		
		if (args[0].equalsIgnoreCase("create")) {
			if (args.length < 2){
				sender.sendMessage("Please specify a tribe name");
				return true;
			}
			if (args.length > 2) {
				sender.sendMessage("Spaces are not allowed in a tribe name");
				return true;
			}
			String name = args[1];
			if (getTribe(name) != null) {
				sender.sendMessage("Tribe already exists");
			}
			
			Player founder = Bukkit.getPlayer(sender.getName());
			TribeFactory.createNewTribe(name,founder);
			sender.sendMessage("Tribe " + name + " created");
			return true;
			
		} else if (args[0].equalsIgnoreCase("destroy")) {
			if (args.length < 2) {
				sender.sendMessage("You need to specify your tribe name");
				return true;
			}
			
			group = getTribe(args[1]);
			
			if (tPlayer == null) {
				sender.sendMessage("You are not in a tribe");
				return true;
			} else if (group == null) {
				sender.sendMessage("You are not leader of that tribe");
				return true;
			}
			
			if (tPlayer.equals(group.getLeader())) {
				TribeFactory.destroyTribe(group);
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
			
			if (tPlayer == null || tPlayer.getTribe() == null) {
				sender.sendMessage("You are not in a tribe");
				return true;
			}
			group = tPlayer.getTribe();
			
			if (group.getLeader().equals(tPlayer)) {
				Player user = Bukkit.getPlayer(args[1]);
				group.invite(user);
				sender.sendMessage(args[1] + " invited successfully");
				return true;
			} else {
				sender.sendMessage("You are not leader of this tribe");
				return true;
			}
			
		} else if (args[0].equalsIgnoreCase("join")) {
			
			if (args.length < 2) {
				sender.sendMessage("You need to specify the tribe to join");
				return true;
			}
			
			if (tPlayer == null) {
				tPlayer = TribePlayerFactory.createNewPlayer(player);
			}
			if (tPlayer.getTribe() != null) {
				if (tPlayer.getTribe().getLeader().equals(tPlayer)) {
					sender.sendMessage("You cannot leave without transfering leadership");
					return true;
				}
				tPlayer.getTribe().delPlayer(tPlayer);
			}
			Tribe newGroup = getTribe(args[1]);
			if (newGroup == null) {
				sender.sendMessage("invalid tribe name");
				return true;
			}
			if (newGroup.isInvited(player)) {
				newGroup.addPlayer(tPlayer);
				sender.sendMessage("you have joined " + newGroup.getName());
				return true;
			} else {
				sender.sendMessage("you are not invited to this tribe");
				return true;
			}
		} else if (args[0].equalsIgnoreCase("kick")) {
			if (tPlayer == null || tPlayer.getTribe() == null) {
				sender.sendMessage("You are not in a tribe");
				return true;
			}
			
			if (!tPlayer.getTribe().getLeader().equals(tPlayer)) {
				sender.sendMessage("You are not leader of this tribe");
				return true;
			}
			if (args.length < 2) {
				sender.sendMessage("You need to specify the player to kick");
				return true;
			}
			TribePlayer kicked = getPlayer(args[1]);
			if (kicked == null) {
				sender.sendMessage("Player does not exist or is not in a tribe");
				return true;
			}
			if (kicked.getTribe() != tPlayer.getTribe()) {
				sender.sendMessage("Player is not in your tribe");
				return true;
			}
			
			tPlayer.getTribe().delPlayer(kicked);
			sender.sendMessage(kicked.getPlayer() + " has been kicked");
			Player kickedPlayer = Bukkit.getServer().getPlayer(kicked.getPlayer());
			if (kickedPlayer != null) {
				kickedPlayer.sendMessage("You have been kicked from your tribe!");
			}
			

		
		} else if (args[0].equalsIgnoreCase("leave")) {
			if (tPlayer == null || tPlayer.getTribe() == null) {
				sender.sendMessage("You are not in a tribe");
				return true;
			}
			
			if (tPlayer.getTribe().getLeader().equals(tPlayer)) {
				sender.sendMessage("You cannot leave without transfering leadership");
				return true;
			}
			
			tPlayer.getTribe().delPlayer(tPlayer);
			return true;
			
			
		} else if (args[0].equalsIgnoreCase("giveLeader")) {
			if (args.length < 2) {
				sender.sendMessage("You need to specify who to give leadership to");
				return true;
			}
			TribePlayer otherPlayer = getPlayer(args[1]);
			if (tPlayer == null || tPlayer.getTribe() == null) {
				sender.sendMessage("You are not in a tribe");
				return true;
			}
			if (otherPlayer == null) {
				sender.sendMessage("They are not in your tribe");
				return true;
			}
			if (tPlayer.getTribe().getLeader().equals(tPlayer)) {
				tPlayer.getTribe().setLeader(otherPlayer.getPlayer());
				sender.sendMessage("leadership transfered");
				return true;
			} else {
				sender.sendMessage("You are not leader of your tribe");
			}
			
		} else if (args[0].equalsIgnoreCase("show")) {
			if (args.length < 2) {
				sender.sendMessage("You need to specify a faction or player to show");
				return true;
			}
			group = getTribe(args[1]);
			if (group != null) {
				sender.sendMessage(group.toString()); 
				return true;
			}
			tPlayer = getPlayer(args[1]);
			if (tPlayer != null) {
				group = tPlayer.getTribe();
				if (group != null) {
					sender.sendMessage(tPlayer.getPlayer() + " is in the tribe " + group.getName());
					return true;
				} else {
					//TODO: this will typically never be reached.
					//players who are not in a tribe will probably not have tribeplayer objects.
					sender.sendMessage(tPlayer.getPlayer() + " is not in a tribe");
					return true;
				}
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
		short page;
		if (args.length < 2) page = 0;
		else {
			try {
				page = Short.parseShort(args[1]);
			} catch (NumberFormatException e) {
				page = 0;
			}
		}
		String message;
		switch (page) {
		case 1:
			message = "Page 1/2 TODO";
			break;
		case 2:
			message = "Page 2/2 TODO";
			break;
		default:
			message = "Page 0/2: You can specify a number when using help to view a page.\n" +
					  "EG: /t help 1 would display page 1.\n" +
					  "/t create [tribe] can be used to create a tribe\n" +
					  "/t destroy [yourtribe] can be used to dissolve your tribe\n" +
					  "/t help shows this help information";
			break;
		}
		sender.sendMessage(message);
		return true;
	}
	
	
	public static void destroyTribe(Tribe group) {
		try {
			log("destroying tribe " + group.getName());
			//groups.remove(group);
			//TODO stub
		} catch (Exception e) {
			log("error destroying tribe " + group.getName());
		}
	}
	
	public static void addTribe(Tribe group) {
		/*if (!groups.contains(group))
			groups.add(group);
		*/ //TODO stub
	}
	
	public static Tribe getTribe(String name) {
		/*
		for (Tribe group : groups) {
			if (group.getName().equalsIgnoreCase(name)) {
				return group;
			}
		}*/
		return null; //TODO STUB
	}
	
	public static Tribe[] getTribes() {
		//TODO stub
		//return groups.toArray(new Tribe[groups.size()]);
		return null;

	}
	
	public static void addPlayer(TribePlayer user) {
		/*
		if (!users.contains(user))
			users.add(user);
		*/ //TODO STUB
	}
	
	public static void delPlayer(TribePlayer user) {
		//users.remove(user);
		//TODO stub
		
	}

	
	public static TribePlayer getPlayer(String name) {
		if (name == null) return null;
		/*
		for (int i = 0; i < users.size(); i++) {
			if(users.get(i).getPlayer().equalsIgnoreCase(name)) return users.get(i);
		}*/ //TODO stub
		return null;

	}
	
	public static TribePlayer getPlayer(Player user) {
		if (user == null) return null;
		return getPlayer(user.getName());
		
	}
	
	public static DBCollection getTribeTable() {
		return tribeTable;
	}
	
	public static DBCollection getEmeraldTable() {
		return emeraldTable;
	}
	public static DBCollection getDiamondTable() {
		return diamondTable;
	}

	public static Long getClaimSize() {
		return claimSize;
	}


	public static boolean getYAxisClaim() {
		return yAxisClaim;
	}
	//let other objects call our logger
	public static void log(String line) {
		TribeLogger.info(line);
	}

}
