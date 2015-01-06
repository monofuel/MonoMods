/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.NoWither;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import org.bukkit.event.HandlerList;


public class NoWither extends JavaPlugin{

	
	private static Logger WitherLogger = null;
	private boolean enabled = true;
	public WitherListener blocker = null;

	public void onEnable() {
		WitherLogger = getLogger();
		
		new WitherListener(this);
		
		//set defaults if they do not exist
		saveDefaultConfig();
		enabled = getConfig().getBoolean("wither disabled");
		
		//check if we want wither disabled, and if so, disable
		if(enabled) {
			//create anti-wither listener
			log("wither spawning disabled");
			blocker = new WitherListener(this);
		} else {
			log("wither spawning enabled");
		}
	}
	
	public void onDisable() {
		//saving configs on unload is annoying.
		//config should be saved on config changes.

		if (blocker != null) {
			HandlerList.unregisterAll(this);
		}

		log("NoWither has been disabled");
		WitherLogger = null;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("NoWither") &&
			sender instanceof ConsoleCommandSender ||
			(sender instanceof Player && ((Player) sender).hasPermission("nowither"))) {

			//TODO update this to a java-7 switch by strings case
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
			} else if (args[0].equalsIgnoreCase("save")) {
				saveConfig();
				return true;
			} else if (args[0].equalsIgnoreCase("enable")) {
				getConfig().set("wither disabled",false);
				sender.sendMessage("wither enabled");
				saveConfig();
				return true;
			} else if (args[0].equalsIgnoreCase("disable")) {
				getConfig().set("wither disabled",true);
				sender.sendMessage("wither disabled");
				saveConfig();
				return true;
			} else if (args[0].equalsIgnoreCase("help")) {
				//TODO
				String help = "NoWither is configured from the config.\n" +
					      "/nowither load will reload the config.";
				sender.sendMessage(help);
				
				return true;
			}

		} else if (sender instanceof Player && !((Player) sender).hasPermission("nowither")) {
			sender.sendMessage("you do not have permission to use nowither");
			return true;

		}
		
		return false;
	}
	
	//let other objects call our logger
	/**
	 * easy method any class in this plugin can use to log information.
	 * @param line	Line to be logged.
	 *
	 */
	protected static void log(String line) {
		WitherLogger.info(line);
	}

	/**
	 * easy method any class in this plugin can use to log information.
	 * @param level severity of log
	 * @param line	Line to be logged.
	 *
	 */
	protected static void log(Level level, String line) {
		WitherLogger.log(level,line);
	}
}
