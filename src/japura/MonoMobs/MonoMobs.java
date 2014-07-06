/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.MonoMobs;

import japura.MonoUtil.MonoConf;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class MonoMobs extends JavaPlugin{

	private static Logger mobLogger = null;
	private static MonoConf config = null;
	private static ZedCheckRunner zedChecker;

	public JSONObject genDefaultConf() {
		JSONObject defaults =  new JSONObject();
		defaults.put("world","world");
		defaults.put("threads",4L);
		defaults.put("zombie cap",2000L);
		defaults.put("zed per player",50L);
		defaults.put("zed distance",100L);
		defaults.put("zed y distance",50L);
		defaults.put("zed spawn tick offset",40L);
		defaults.put("max light to spawn",7L);
		defaults.put("zed spawn tick length",52L);
		//TODO boolean conf options
		defaults.put("wither disabled","true");

		return defaults;
	}
	
	public void onEnable() {
		mobLogger = getLogger();
		
		//create anti-wither listener
		new WitherListener(this);
		
		//load configuration
		config = new MonoConf(this,genDefaultConf());
		
		//check if we want wither disabled, and if so, disable
		//sometimes the boolean gets saved as a string to the config?
		//not sure why, but might as well be prepared for it.
		Object wither_setting = config.getConf("wither disabled");
		
		if( (wither_setting instanceof Boolean && wither_setting == true)
		  || (wither_setting instanceof String && ((String) wither_setting).equalsIgnoreCase("true")) ) {
			//create anti-wither listener
			log("wither disabled");
			new WitherListener(this);
		} else {
			log("wither enabled");
		}
		
		long tickTime = (long) config.getConf("zed spawn tick offset");
		
		zedChecker = new ZedCheckRunner(this);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this,zedChecker,tickTime,tickTime);
		
		log("MonoMobs has been enabled");
		log("MonoMobs is loaded for world: " + (String) config.getConf("world"));
	}
	
	public void onDisable() {
		CreatureSpawnEvent.getHandlerList().unregister(this);
		
		//write config back out to file
		//if there were no errors reading config in
		config.close();
		
		zedChecker.stop();
		
		log("MonoMobs has been disabled");
		mobLogger = null;
	}
	
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("monomobs")) {
			if (args[0].equalsIgnoreCase("countzed")) {
				countZed(sender);
				return true;
			} else if (args[0].equalsIgnoreCase("reload")) {
				this.getServer().getPluginManager().disablePlugin(this);
				this.getServer().getPluginManager().enablePlugin(this);
				return true;
			} else if (args[0].equalsIgnoreCase("unload")) {
				this.getServer().getPluginManager().disablePlugin(this);
				return true;
			} else if (args[0].equalsIgnoreCase("load")) {
				//GARBAGE EVERYWHERE
				config = new MonoConf(this,genDefaultConf());
				return true;
			} else if (args[0].equalsIgnoreCase("save")) {
				config.close();
				config = new MonoConf(this,genDefaultConf());
				return true;
			} else if (args[0].equalsIgnoreCase("help")) {
				String help = "Welcome to MonoMobs! Here are the available MonoMob commands. " +
						"They can be used via /monomobs [command]\n" +
						"CountZed: counts the zed in each world\n" +
						"load: discards current config and loads file\n" +
						"save: saves the config to file\n" +
						"reload: reloads the plugin\n" +
						"unload: unloads the plugin";
				sender.sendMessage(help);
				
				return true;
			}

		}
		
		return false;
	}
	
	private void countZed(CommandSender sender) {
		int count = countZed((String) config.getConf("world"));
		sender.sendMessage("there are " + count + " zombies in the world");
	}
	
	public static int countZed(String world) {
		List<LivingEntity> entities;
		int zed;
		zed = 0;
		entities = Bukkit.getWorld(world).getLivingEntities();
		for (LivingEntity mob : entities) {
			if (mob instanceof Zombie) zed++;
		}
		
		return zed;
		
	}
	
	public static MonoConf getMonoConfig() {
		return config;
	}
	
	//let other objects call our logger
	public static void log(String line) {
		mobLogger.info(line);
	}
	
}

