/**
 *      author: Monofuel
 *      website: japura.net
 *      this file is distributed under the modified BSD license
 *      that should have been included with it.
 */


package japura.MonoCities;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class MonoCities extends JavaPlugin{
	
	private static Logger citiesLogger = null;
	
	private static CityPopulator pop = null;
	private static WorldInit listener = null;

	//used to disable the plugin before it fully loads if it 
	//cannot find any schematic files.
	private boolean disabled = true;

	public void onEnable() {
		citiesLogger = getLogger();
		
		//save defaults if they do not exist
		saveDefaultConfig();

		disabled = false;
		pop = new CityPopulator(this);
		//if no schematics are found, disabled will be set true.
		//this means we should probably not continue enabling.
		//disabled will only be false if this onEnable function
		//runs and succesfully runs CityPopulator
		//(without CityPopulator disabling the plugin)
		if (disabled) return;
		
		//TODO: read world from config
		World world = Bukkit.getWorld("World");
		if (world == null) {
			listener = new WorldInit(this,pop);
		} else {
			Bukkit.getWorld("World").getPopulators().add(pop);
		}
		
		log("MonoCities has been enabled");
	}
	
	public void onDisable() {
		
		//remove populator
		//do an extra check to make sure we aren't being disabled
		//before the world is initialized
		if (Bukkit.getWorld("World") != null)
			Bukkit.getWorld("World").getPopulators().remove(pop);
		
		//check if we aren't being disabled before fully initializing
		if (listener != null)
			listener.stop();
		
		//If no schematics are found, pop will not have been initialized.
		if (pop != null)
			pop.close();
		
		saveConfig();

		log("MonoCities has been disabled");
		citiesLogger = null;
		disabled = true;
	}
	
	
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("plugintemplate")) {
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
			} else if (args[0].equalsIgnoreCase("help")) {
				String help = "MonoCities builds cities over the regular world";
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
         * @param line  Line to be logged.
         *
         */
	public static void log(String line) {
		citiesLogger.info(line);
	}
}
