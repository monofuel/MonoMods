/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.MonoMobs;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MonoMobs extends JavaPlugin{

	private static Logger mobLogger = null;
	private static ZedCheckRunner zedChecker;

	
	public void onEnable() {
		mobLogger = getLogger();

		saveDefaultConfig();
		
		
		long tickTime = getConfig().getLong("zed spawn tick offset");
		
		for(String worldName : (List<String>) getConfig().getList("worlds")) {
			World world = Bukkit.getWorld(worldName);
			
			zedChecker = new ZedCheckRunner(this,world);
			Bukkit.getScheduler().scheduleSyncRepeatingTask(this,zedChecker,tickTime,tickTime);
			
			log("MonoMobs has been enabled");
			log("MonoMobs is loaded for world: " + worldName);
		}
		
		
		log("MonoMobs has been enabled");
	}
	
	public void onDisable() {
		CreatureSpawnEvent.getHandlerList().unregister(this);
		
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
				reloadConfig();
				return true;
			} else if (args[0].equalsIgnoreCase("save")) {
				saveConfig();
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
		int count = countZed(getConfig().getString("world"));
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
	
	//let other objects call our logger
	public static void log(String line) {
		mobLogger.info(line);
	}
	
}

