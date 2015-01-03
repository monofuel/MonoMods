/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.NoWither;

import org.json.simple.JSONObject;

import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class NoWither extends JavaPlugin{
	
	private static Logger WitherLogger = null;
	private boolean enabled = true;

	public void onEnable() {
		WitherLogger = getLogger();
		
		new WitherListener(this);
		
		//set defaults if they do not exist
		saveDefaultConfig();
		enabled = getConfig().getBoolean("wither disabled");
		
		log("NoWither has been enabled");
		
		//check if we want wither disabled, and if so, disable
		if((enabled)) {
			//create anti-wither listener
			log("wither disabled");
			new WitherListener(this);
		} else {
			log("wither enabled");
		}
	}
	
	public void onDisable() {
		
		
		log("NoWither has been disabled");
		WitherLogger = null;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("NoWither")) {
			if (args[0].equalsIgnoreCase("reload")) {
				this.getServer().getPluginManager().disablePlugin(this);
				this.getServer().getPluginManager().enablePlugin(this);
				return true;
			} else if (args[0].equalsIgnoreCase("unload")) {
				this.getServer().getPluginManager().disablePlugin(this);
				return true;
			} else if (args[0].equalsIgnoreCase("load")) {			
				//TODO
				reloadConfig();
				return true;
			} else if (args[0].equalsIgnoreCase("save")) {
				//TODO
				saveConfig();
				return true;
			} else if (args[0].equalsIgnoreCase("help")) {
				String help = "NoWither is configured from the config.\n" +
					      "/nowither load will reload the config.";
				sender.sendMessage(help);
				
				return true;
			}

		}
		
		return false;
	}
	
	//let other objects call our logger
	/**
	 * easy method any class in this plugin can use to log information.
	 * for consistenty, try to prefix a line with the severity of the message.
	 * [ERROR] means the server should probably sotp and have the issue fixed
	 * [WARNING] not critical, but not good.
	 * [INFO] purely for information reasons (eg: logs for if a player is a lieing git(my personal favorite))
	 * @param line	Line to be logged.
	 *
	 */
	protected static void log(String line) {
		WitherLogger.info(line);
	}
}
