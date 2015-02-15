/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.MonoCities;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Random;

import java.net.UnknownHostException;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

import com.mongodb.*;

public class MonoCities extends JavaPlugin{
	
	private static MongoClient mongo = null;
	private static DB db = null;
	private static DBCollection table = null;

	private static Logger citiesLogger = null;
	
	private static CityPopulator pop = null;

	//used to disable the plugin before it fully loads if it 
	//cannot find any schematic files.
	private boolean disabled = true;

	public void onEnable() {

		//save defaults if they do not exist
		saveDefaultConfig();
		citiesLogger = getLogger();

		String mongoHost = getConfig().getString("mongo host");
		int port = getConfig().getInt("mongo port");
		String databaseName = getConfig().getString("mongo database");
		String tableName = getConfig().getString("mongo table");
		
		try {
			mongo = new MongoClient(mongoHost,port);
		} catch (UnknownHostException e) {
			getLogger().log(Level.SEVERE,"Error connecting to database, bailing out",e);
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		}

		db = mongo.getDB(databaseName);
		table = db.getCollection(tableName);

		disabled = false;
		pop = new CityPopulator(this);
		//if no schematics are found, disabled will be set true.
		//this means we should probably not continue enabling.
		//disabled will only be false if this onEnable function
		//runs and succesfully runs CityPopulator
		//(without CityPopulator disabling the plugin)
		if (disabled) return;

		pop.setLoadDistance(getConfig().getInt("load distance in chunks"));
		int chunkRate = getConfig().getInt("chunk generate rate in ticks");
		
		getServer().getScheduler().scheduleSyncRepeatingTask(this,pop,0,chunkRate);
		log("MonoCities has been enabled");
	}
	
	public void onDisable() {
		
		if (mongo != null) {
			mongo.close();
		}

		
		saveConfig();

		log("MonoCities has been disabled");
		citiesLogger = null;
		disabled = true;
	}
	
	public void regen(Player user) {
		Chunk myChunk = user.getLocation().getChunk();
		String chunkyString = myChunk.getWorld().getName();
		chunkyString += "," + myChunk.getX();
		chunkyString += "," + myChunk.getZ();

		BasicDBObject query = new BasicDBObject();
		query.put("location",chunkyString);

		DBObject item = table.findOne(query);

		String buildingType = (String) item.get("type");
		long seed = (long) item.get("seed");
		Random rand = new Random();
		rand.setSeed(seed);
		pop.placeBuilding(buildingType,myChunk,rand);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("monocities")) {
			if (args[0].equalsIgnoreCase("reload")) {
				this.getServer().getPluginManager().disablePlugin(this);
				this.getServer().getPluginManager().enablePlugin(this);
				return true;
			} else if (args[0].equalsIgnoreCase("unload")) {
				this.getServer().getPluginManager().disablePlugin(this);
				return true;
			} else if (args[0].equalsIgnoreCase("regen")) {
				if (sender instanceof Player) {
					regen((Player)sender);
				} else {
					sender.sendMessage("cannot be used by console");
				}
				return true;
			} else if (args[0].equalsIgnoreCase("load")) {
				reloadConfig();
				return true;
			} else if (args[0].equalsIgnoreCase("save")) {
				saveConfig();
				return true;
			} else if (args[0].equalsIgnoreCase("help")) {
				String help = "MonoCities builds cities over the regular world";
				sender.sendMessage(help);
				
				return true;
			}

		}
		
		return false;
	}


	public static void recordNewBuilding(String name, Chunk myChunk, long seed) {
		String chunkyString = myChunk.getWorld().getName();
		chunkyString += "," + myChunk.getX();
		chunkyString += "," + myChunk.getZ();

		BasicDBObject building = new BasicDBObject();
		building.put("location",chunkyString);
		building.put("seed",seed);
		building.put("type",name);
		table.insert(building);
	}

	public static boolean wasChunkPopulated(Chunk myChunk) {
		String chunkyString = myChunk.getWorld().getName();
		chunkyString += "," + myChunk.getX();
		chunkyString += "," + myChunk.getZ();

		BasicDBObject query = new BasicDBObject();
		query.put("location",chunkyString);

		DBObject chunk = table.findOne(query);
		if (chunk == null) {
			return false;
		} else {
			return true;
		}
	}
	
	//let other objects call our logger
        /**
         * easy method any class in this plugin can use to log information.
         * for consistenty, try to prefix a line with the severity of the message.
         * [ERROR] means the server should probably sotp and have the issue fixed
         * [WARNING] not critical, but not good.
         * [INFO] purely for information reasons (eg: logs for if a player is a lieing git(my personal favorite))
         * @param line  Line to be logged.
         *
         */
	public static void log(String line) {
		citiesLogger.info(line);
	}

	public static Logger getCitiesLogger() {
		return citiesLogger;
	}
}
