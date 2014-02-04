package japura.MonoMobs;

//TODO
//boolean config options


import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;


public class MonoMobs extends JavaPlugin{

	private static Logger mobLogger = null;
	private static MonoConf config = null;
	private static ZedCheckRunner zedChecker;

	
	public void onEnable() {
		mobLogger = getLogger();
		
		//create anti-wither listener
		new WitherListener(this);
		
		//load configuration
		MonoConf.init();
		config = new MonoConf("plugins/MonoMobs");
		
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
				config = new MonoConf("plugins/MonoMobs");
				return true;
			} else if (args[0].equalsIgnoreCase("save")) {
				config.close();
				config = new MonoConf("plugins/MonoMobs");
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

